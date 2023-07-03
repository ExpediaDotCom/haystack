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

package com.expedia.www.haystack.kinesis.span.collector.config

import java.util.concurrent.TimeUnit

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.expedia.www.haystack.collector.commons.config.{ConfigurationLoader, ExternalKafkaConfiguration, ExtractorConfiguration, KafkaProduceConfiguration}
import com.expedia.www.haystack.kinesis.span.collector.config.entities.KinesisConsumerConfiguration
import com.expedia.www.haystack.span.decorators.plugin.config.Plugin

import scala.concurrent.duration._

object ProjectConfiguration {

  private val config = ConfigurationLoader.loadConfigFileWithEnvOverrides()

  def healthStatusFile(): Option[String] = if (config.hasPath("health.status.path")) Some(config.getString("health.status.path")) else None

  def kafkaProducerConfig(): KafkaProduceConfiguration = ConfigurationLoader.kafkaProducerConfig(config)

  def extractorConfiguration(): ExtractorConfiguration = ConfigurationLoader.extractorConfiguration(config)

  def kinesisConsumerConfig(): KinesisConsumerConfiguration = {
    val kinesis = config.getConfig("kinesis")
    val stsRoleArn = if (kinesis.hasPath("sts.role.arn")) Some(kinesis.getString("sts.role.arn")) else None

    KinesisConsumerConfiguration(
      awsRegion = kinesis.getString("aws.region"),
      stsRoleArn = stsRoleArn,
      appGroupName = kinesis.getString("app.group.name"),
      streamName = kinesis.getString("stream.name"),
      streamPosition = InitialPositionInStream.valueOf(kinesis.getString("stream.position")),
      kinesis.getDuration("checkpoint.interval.ms", TimeUnit.MILLISECONDS).millis,
      kinesis.getInt("checkpoint.retries"),
      kinesis.getDuration("checkpoint.retry.interval.ms", TimeUnit.MILLISECONDS).millis,
      kinesisEndpoint = if (kinesis.hasPath("endpoint")) Some(kinesis.getString("endpoint")) else None,
      dynamoEndpoint = if (kinesis.hasPath("dynamodb.endpoint")) Some(kinesis.getString("dynamodb.endpoint")) else None,
      dynamoTableName = if (kinesis.hasPath("dynamodb.table")) Some(kinesis.getString("dynamodb.table")) else None,
      maxRecordsToRead = kinesis.getInt("max.records.read"),
      idleTimeBetweenReads = kinesis.getDuration("idle.time.between.reads.ms", TimeUnit.MILLISECONDS).millis,
      shardSyncInterval = kinesis.getDuration("shard.sync.interval.ms", TimeUnit.MILLISECONDS).millis,
      metricsLevel = MetricsLevel.fromName(kinesis.getString("metrics.level")),
      metricsBufferTime = kinesis.getDuration("metrics.buffer.time.ms", TimeUnit.MILLISECONDS).millis,
      taskBackoffTime = kinesis.getDuration("task.backoff.ms", TimeUnit.MILLISECONDS).millis)
  }

  def externalKafkaConfig(): List[ExternalKafkaConfiguration] = ConfigurationLoader.externalKafkaConfiguration(config)

  def additionalTagConfig(): Map[String, String] = ConfigurationLoader.additionalTagsConfiguration(config)

  def pluginConfiguration(): Plugin = ConfigurationLoader.pluginConfigurations(config)
}
