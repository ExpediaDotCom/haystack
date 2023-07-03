/*
 *
 *     Copyright 2017 Expedia, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */
package com.expedia.www.haystack.trends.kstream.processor

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.trends.config.entities.KafkaProduceConfiguration
import com.expedia.www.haystack.trends.kstream.serde.TrendMetricSerde.metricRegistry
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.streams.processor.{AbstractProcessor, Processor, ProcessorContext, ProcessorSupplier}
import org.slf4j.LoggerFactory

class ExternalKafkaProcessorSupplier(kafkaProduceConfig: KafkaProduceConfiguration) extends ProcessorSupplier[String, MetricData] {

  private val LOGGER = LoggerFactory.getLogger(this.getClass)
  private val metricPointExternalKafkaSuccessMeter = metricRegistry.meter("metricpoint.kafka-external.success")
  private val metricPointExternalKafkaFailureMeter = metricRegistry.meter("metricpoint.kafka-external.failure")

  def get: Processor[String, MetricData] = {
    new ExternalKafkaProcessor(kafkaProduceConfig: KafkaProduceConfiguration)
  }

  /**
    * This is the Processor which contains the map of unique trends consumed from the assigned partition and the corresponding trend metric for each trend
    * Each trend is uniquely identified by the metricPoint key - which is a combination of the name and the list of tags. Its backed by a state store which keeps this map and has the
    * ability to restore the map if/when the app restarts or when the assigned kafka partitions change
    *
    * @param kafkaProduceConfig - configuration to create kafka producer
    */
  private class ExternalKafkaProcessor(kafkaProduceConfig: KafkaProduceConfiguration) extends AbstractProcessor[String, MetricData] {

    private val kafkaProducer: KafkaProducer[String, MetricData] = new KafkaProducer[String, MetricData](kafkaProduceConfig.props.get)
    private val kafkaProduceTopic = kafkaProduceConfig.externalKafkaTopic

    @SuppressWarnings(Array("unchecked"))
    override def init(context: ProcessorContext) {
      super.init(context)
    }

    /**
      * tries to fetch the trend metric based on the key, if it exists it updates the trend metric else it tries to create a new trend metric and adds it to the store      *
      *
      * @param key   - key in the kafka record - should be MetricDefinitionKeyGenerator.generateKey(metricData.getMetricDefinition)
      * @param value - metricData
      */
    def process(key: String, value: MetricData): Unit = {

      val kafkaMessage = new ProducerRecord(kafkaProduceTopic,
        key, value)
      kafkaProducer.send(kafkaMessage, new Callback {
        override def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
          if (e != null) {
            LOGGER.error(s"Failed to produce the message to kafka for topic=$kafkaProduceTopic, with reason=", e)
            metricPointExternalKafkaFailureMeter.mark()
          } else {
            metricPointExternalKafkaSuccessMeter.mark()
          }
        }
      })
    }
  }
}





