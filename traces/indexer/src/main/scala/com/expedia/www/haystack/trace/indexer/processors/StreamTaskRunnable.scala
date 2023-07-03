/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.www.haystack.trace.indexer.processors

import java.util
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.trace.indexer.config.entities.KafkaConfiguration
import com.expedia.www.haystack.trace.indexer.processors.StreamTaskState.StreamTaskState
import com.expedia.www.haystack.trace.indexer.processors.supplier.StreamProcessorSupplier
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

class StreamTaskRunnable(taskId: Int, kafkaConfig: KafkaConfiguration, processorSupplier: StreamProcessorSupplier[String, Span])
  extends Runnable with AutoCloseable {

  private val LOGGER = LoggerFactory.getLogger(classOf[StreamTaskRunnable])

  /**
    * consumer rebalance listener
    */
  private class RebalanceListener extends ConsumerRebalanceListener {

    /**
      * close the running processors for the revoked partitions
      *
      * @param revokedPartitions revoked partitions
      */
    override def onPartitionsRevoked(revokedPartitions: util.Collection[TopicPartition]): Unit = {
      LOGGER.info("Partitions {} revoked at the beginning of consumer rebalance for taskId={}", revokedPartitions, taskId)

      revokedPartitions.asScala.foreach(
        p => {
          val processor = processors.remove(p)
          if (processor != null) processor.close()
        })
    }

    /**
      * create processors for newly assigned partitions
      *
      * @param assignedPartitions newly assigned partitions
      */
    override def onPartitionsAssigned(assignedPartitions: util.Collection[TopicPartition]): Unit = {
      LOGGER.info("Partitions {} assigned at the beginning of consumer rebalance for taskId={}", assignedPartitions, taskId)

      assignedPartitions.asScala foreach {
        partition => {
          val processor = processorSupplier.get()
          val previousProcessor = processors.putIfAbsent(partition, processor)
          if (previousProcessor == null) processor.init()
        }
      }
    }
  }

  @volatile
  private var state = StreamTaskState.NOT_RUNNING
  private var wakeups: Int = 0

  private val shutdownRequested = new AtomicBoolean(false)
  private val wakeupScheduler = Executors.newScheduledThreadPool(1)
  private val listeners = mutable.ListBuffer[StateListener]()
  private val processors = new ConcurrentHashMap[TopicPartition, StreamProcessor[String, Span]]()

  private val consumer = {
    val props = new Properties()
    kafkaConfig.consumerProps.entrySet().asScala.foreach(entry => props.put(entry.getKey, entry.getValue))
    props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, taskId.toString)
    new KafkaConsumer[String, Span](props)
  }

  private val rebalanceListener = new RebalanceListener

  consumer.subscribe(util.Arrays.asList(kafkaConfig.consumeTopic), rebalanceListener)

  /**
    * Execute the stream processors
    *
    */
  override def run(): Unit = {
    LOGGER.info("Starting stream processing thread with id={}", taskId)
    try {
      updateStateAndNotify(StreamTaskState.RUNNING)
      runLoop()
    } catch {
      case ie: InterruptedException =>
        LOGGER.error(s"This stream task with taskId=$taskId has been interrupted", ie)
      case ex: Exception =>
        if (!shutdownRequested.get()) updateStateAndNotify(StreamTaskState.FAILED)
        // may be logging the exception again for kafka specific exceptions, but it is ok.
        LOGGER.error(s"Stream application faced an exception during processing for taskId=$taskId: ", ex)
    }
    finally {
      consumer.close(kafkaConfig.consumerCloseTimeoutInMillis, TimeUnit.MILLISECONDS)
      updateStateAndNotify(StreamTaskState.CLOSED)
    }
  }

  /**
    * invoke the processor per partition for the records that are read from kafka.
    * Update the offsets (if any) that need to be committed in the committableOffsets map
    *
    * @param partition          kafka partition
    * @param partitionRecords   records of the given kafka partition
    * @param committableOffsets offsets that need to be committed for the given topic partition
    */
  private def invokeProcessor(partition: Int,
                              partitionRecords: Iterable[ConsumerRecord[String, Span]],
                              committableOffsets: util.HashMap[TopicPartition, OffsetAndMetadata]): Unit = {
    val topicPartition = new TopicPartition(kafkaConfig.consumeTopic, partition)
    val processor = processors.get(topicPartition)

    if (processor != null) {
      processor.process(partitionRecords) match {
        case Some(offsetMetadata) => committableOffsets.put(topicPartition, offsetMetadata)
        case _ => /* the processor has nothing to commit for now */
      }
    }
  }

  /**
    * run the consumer loop till the shutdown is requested or any exception is thrown
    */
  private def runLoop(): Unit = {
    while (!shutdownRequested.get()) {
      poll() match {
        case Some(records) if records != null && !records.isEmpty && !processors.isEmpty =>
          val committableOffsets = new util.HashMap[TopicPartition, OffsetAndMetadata]()
          val groupedByPartition = records.asScala.groupBy(_.partition())

          groupedByPartition foreach {
            case (partition, partitionRecords) => invokeProcessor(partition, partitionRecords, committableOffsets)
          }

          // commit offsets
          commit(committableOffsets)
        // if no records are returned in poll, then do nothing
        case _ =>
      }
    }
  }

  /**
    * before requesting consumer.poll(), schedule a wakeup call as poll() may hang due to network errors in kafka
    * if the poll() doesnt return after a timeout, then wakeup the consumer.
    *
    * @return consumer records from kafka
    */
  private def poll(): Option[ConsumerRecords[String, Span]] = {

    def scheduleWakeup() = wakeupScheduler.schedule(new Runnable {
      override def run(): Unit = consumer.wakeup()
    }, kafkaConfig.wakeupTimeoutInMillis, TimeUnit.MILLISECONDS)

    def handleWakeup(we: WakeupException): Unit = {
      // if in shutdown phase, then do not swallow the exception, throw it to upstream
      if (shutdownRequested.get()) throw we

      wakeups = wakeups + 1
      if (wakeups == kafkaConfig.maxWakeups) {
        LOGGER.error(s"WakeupException limit exceeded, throwing up wakeup exception for taskId=$taskId.", we)
        throw we
      } else {
        LOGGER.error(s"Consumer poll took more than ${kafkaConfig.wakeupTimeoutInMillis} ms for taskId=$taskId, wakeup attempt=$wakeups!. Will try poll again!")
      }
    }

    val wakeupCall = scheduleWakeup()

    try {
      val records: ConsumerRecords[String, Span] = consumer.poll(kafkaConfig.pollTimeoutMs)
      wakeups = 0
      Some(records)
    } catch {
      case we: WakeupException =>
        handleWakeup(we)
        None
    } finally {
      Try(wakeupCall.cancel(true))
    }
  }

  /**
    * commit the offset to kafka with a retry logic
    *
    * @param offsets      map of offsets for each topic partition
    * @param retryAttempt current retry attempt
    */
  @tailrec
  private def commit(offsets: util.HashMap[TopicPartition, OffsetAndMetadata], retryAttempt: Int = 0): Unit = {
    try {
      if (!offsets.isEmpty && retryAttempt <= kafkaConfig.commitOffsetRetries) {
        consumer.commitSync(offsets)
      }
    } catch {
      case _: CommitFailedException =>
        Thread.sleep(kafkaConfig.commitBackoffInMillis)
        // retry offset again
        commit(offsets, retryAttempt + 1)
      case ex: Exception =>
        LOGGER.error("Fail to commit the offsets with exception", ex)
    }
  }

  private def updateStateAndNotify(newState: StreamTaskState) = {
    if (state != newState) {
      state = newState

      // invoke listeners for any state change
      listeners foreach (listener => listener.onTaskStateChange(state))
    }
  }

  /**
    * close the runnable. If still in running state, then wakeup the consumer
    */
  override def close(): Unit = {
    Try {
      LOGGER.info(s"Close has been requested for taskId=$taskId")
      shutdownRequested.set(true)
      if (isStillRunning) consumer.wakeup()
      wakeupScheduler.shutdown()
    }
  }

  /**
    * if consumer is still in running state
    *
    * @return
    */
  def isStillRunning: Boolean = state == StreamTaskState.RUNNING

  /**
    * set the state change listener
    *
    * @param listener state change listener
    */
  def setStateListener(listener: StateListener): Unit = listeners += listener
}
