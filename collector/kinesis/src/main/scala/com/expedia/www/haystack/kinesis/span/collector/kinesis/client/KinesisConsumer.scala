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

package com.expedia.www.haystack.kinesis.span.collector.kinesis.client

import java.util.UUID

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.internal.securitytoken.{RoleInfo, STSProfileCredentialsServiceProvider}
import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{KinesisClientLibConfiguration, Worker}
import com.expedia.www.haystack.collector.commons.health.HealthController
import com.expedia.www.haystack.collector.commons.record.KeyValueExtractor
import com.expedia.www.haystack.collector.commons.sink.RecordSink
import com.expedia.www.haystack.kinesis.span.collector.config.entities.KinesisConsumerConfiguration
import com.expedia.www.haystack.kinesis.span.collector.kinesis.RecordProcessor
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class KinesisConsumer(config: KinesisConsumerConfiguration,
                      keyValueExtractor: KeyValueExtractor,
                      sink: RecordSink) extends AutoCloseable {
  private val LOGGER = LoggerFactory.getLogger(classOf[KinesisConsumer])

  private var worker: Worker = _

  // this is a blocking call
  def startWorker(): Unit = {
    worker = buildWorker(createProcessorFactory())

    LOGGER.info("Starting the kinesis worker now.")

    // mark collector as healthy
    HealthController.setHealthy()

    // the run method will block this thread, process loop will start now..
    worker.run()
  }

  private def createProcessorFactory() = {
    new IRecordProcessorFactory {
      override def createProcessor() = new RecordProcessor(config, keyValueExtractor, sink)
    }
  }

  /**
    * build single kinesis consumer worker. This worker creates the processors for shards
    *
    * @param processorFactory factory to create processor
    * @return
    */
  private def buildWorker(processorFactory: IRecordProcessorFactory): Worker = {
    val region = Regions.fromName(config.awsRegion)

    val workerId = UUID.randomUUID.toString


    val kinesisCredsProvider = config.stsRoleArn match {
      case Some(arn) if StringUtils.isNotEmpty(arn) => new STSProfileCredentialsServiceProvider(new RoleInfo().withRoleArn(arn).withRoleSessionName(config.appGroupName))
      case _ => DefaultAWSCredentialsProviderChain.getInstance
    }

    val kinesisClientConfig = new KinesisClientLibConfiguration(
      config.appGroupName,
      config.streamName,
      kinesisCredsProvider,
      DefaultAWSCredentialsProviderChain.getInstance,
      DefaultAWSCredentialsProviderChain.getInstance,
      workerId)

    kinesisClientConfig
      .withMaxRecords(config.maxRecordsToRead)
      .withIdleTimeBetweenReadsInMillis(config.idleTimeBetweenReads.toMillis)
      .withShardSyncIntervalMillis(config.shardSyncInterval.toMillis)
      .withInitialPositionInStream(config.streamPosition)
      .withMetricsLevel(config.metricsLevel)
      .withMetricsBufferTimeMillis(config.metricsBufferTime.toMillis)
      .withRegionName(region.getName)
      .withTableName(config.dynamoTableName.getOrElse(config.appGroupName))
      .withTaskBackoffTimeMillis(config.taskBackoffTime.toMillis)

    config.dynamoEndpoint.map(kinesisClientConfig.withDynamoDBEndpoint)
    config.kinesisEndpoint.map(kinesisClientConfig.withKinesisEndpoint)

    new Worker.Builder()
      .config(kinesisClientConfig)
      .recordProcessorFactory(processorFactory)
      .build()
  }

  /**
    * close the kinesis worker. The shutdown will also cleanup resources allocated by the worker
    */
  override def close(): Unit = worker.shutdown()
}
