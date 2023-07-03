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

package com.expedia.www.haystack.kinesis.span.collector.unit.tests

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.expedia.www.haystack.collector.commons.config.ExternalKafkaConfiguration
import com.expedia.www.haystack.kinesis.span.collector.config.ProjectConfiguration
import com.expedia.www.haystack.span.decorators.plugin.config.Plugin
import org.apache.kafka.clients.producer.ProducerConfig
import org.scalatest.{FunSpec, Matchers}

class ConfigurationLoaderSpec extends FunSpec with Matchers {

  val project = ProjectConfiguration

  describe("Configuration com.expedia.www.haystack.span.loader") {
    it("should load the kinesis config from base.conf") {
      val kinesis = project.kinesisConsumerConfig()
      kinesis.metricsLevel shouldEqual MetricsLevel.NONE
      kinesis.awsRegion shouldEqual "us-west-2"
      kinesis.appGroupName shouldEqual "haystack-kinesis-proto-span-collector"
      kinesis.checkpointRetries shouldBe 50
      kinesis.dynamoTableName shouldBe None
      kinesis.checkpointInterval.toMillis shouldBe 15000L
      kinesis.streamPosition shouldEqual InitialPositionInStream.LATEST
      kinesis.streamName shouldEqual "haystack-proto-spans"
      kinesis.maxRecordsToRead shouldBe 2000
      kinesis.metricsBufferTime.toMillis shouldBe 10000
      kinesis.shardSyncInterval.toMillis shouldBe 30000
      kinesis.kinesisEndpoint.isEmpty shouldBe true
      kinesis.dynamoEndpoint.isEmpty shouldBe true
      kinesis.taskBackoffTime.toMillis shouldBe 200
    }

    it("should load the kafka config only from base.conf") {
      val kafka = project.kafkaProducerConfig()
      kafka.topic shouldEqual "proto-spans"
      kafka.props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG) shouldEqual "kafkasvc:9092"
    }

    it("should load the extractor config only from base.conf") {
      val extractorConfig = project.extractorConfiguration()
      extractorConfig.outputFormat.toString shouldEqual "proto"
      extractorConfig.spanValidation.spanMaxSize.maxSizeLimit shouldEqual 5000
      extractorConfig.spanValidation.spanMaxSize.enable shouldEqual false
      extractorConfig.spanValidation.spanMaxSize.skipTags.contains("error") shouldEqual true
      extractorConfig.spanValidation.spanMaxSize.skipServices.size shouldEqual 0

    }

    it("should load the external kafka config from the base.conf") {
      val externalKafka: List[ExternalKafkaConfiguration] = project.externalKafkaConfig()
      externalKafka.head.tags("X-HAYSTACK-SPAN-OWNER") shouldEqual "OWNER1"
      externalKafka.head.tags("X-HAYSTACK-SPAN-SENDER") shouldEqual "SENDER1"
      externalKafka.head.kafkaProduceConfiguration.topic shouldEqual "external-proto-spans"
      externalKafka.head.kafkaProduceConfiguration.props.getProperty("bootstrap.servers") shouldEqual "kafkasvc:9092"
    }

    it("should load the plugins config from the base.conf") {
      val plugin: Plugin = project.pluginConfiguration()
      plugin.getDirectory shouldEqual "plugins/decorators"
      plugin.getPluginConfigurationList.get(0).getName shouldEqual "SAMPLE_SPAN_DECORATOR"
      plugin.getPluginConfigurationList.get(0).getConfig.getString("tag.key") shouldEqual "X-HAYSTACK-PLUGIN-SPAN-DECORATOR"
    }

    it("should load the health status file") {
      project.healthStatusFile() shouldEqual Some("/app/isHealthy")
    }
  }
}
