/*
 *  Copyright 2019, Expedia Group.
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

package com.expedia.www.haystack.trace.indexer.unit

import com.expedia.www.haystack.trace.commons.packer.PackerType
import com.expedia.www.haystack.trace.indexer.config.ProjectConfiguration
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.scalatest.{FunSpec, Matchers}

class ConfigurationLoaderSpec extends FunSpec with Matchers {

  val project = new ProjectConfiguration()
  describe("Configuration loader") {

    it("should load the health status config from base.conf") {
      project.healthStatusFilePath shouldEqual "/app/isHealthy"
    }

    it("should load the span buffer config only from base.conf") {
      val config = project.spanAccumulateConfig
      config.pollIntervalMillis shouldBe 2000L
      config.maxEntriesAllStores shouldBe 20000
      config.bufferingWindowMillis shouldBe 10000L
      config.packerType shouldEqual PackerType.SNAPPY
    }

    it("should load the kafka config from base.conf and one stream property from env variable") {
      val kafkaConfig = project.kafkaConfig
      kafkaConfig.produceTopic shouldBe "span-buffer"
      kafkaConfig.consumeTopic shouldBe "spans"
      kafkaConfig.numStreamThreads shouldBe 2
      kafkaConfig.commitOffsetRetries shouldBe 3
      kafkaConfig.commitBackoffInMillis shouldBe 200

      kafkaConfig.maxWakeups shouldBe 5
      kafkaConfig.wakeupTimeoutInMillis shouldBe 5000

      kafkaConfig.consumerProps.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG) shouldBe "kafkasvc:9092"
      kafkaConfig.consumerProps.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG) shouldBe "earliest"
      kafkaConfig.consumerProps.getProperty(ConsumerConfig.GROUP_ID_CONFIG) shouldBe "haystack-trace-indexer"
      kafkaConfig.consumerProps.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG) shouldBe "false"
      kafkaConfig.consumerProps.getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG) shouldBe "org.apache.kafka.common.serialization.StringDeserializer"
      kafkaConfig.consumerProps.getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG) shouldBe "com.expedia.www.haystack.trace.indexer.serde.SpanDeserializer"

      kafkaConfig.consumerCloseTimeoutInMillis shouldBe 30000

      kafkaConfig.producerProps.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG) shouldBe "kafkasvc:9092"
      kafkaConfig.producerProps.getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG) shouldBe "org.apache.kafka.common.serialization.ByteArraySerializer"
      kafkaConfig.producerProps.getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG) shouldBe "org.apache.kafka.common.serialization.StringSerializer"
    }

    it("should load the service metadata config from base.conf") {
      val config = project.serviceMetadataWriteConfig
      config.flushIntervalInSec shouldBe 60
      config.flushOnMaxOperationCount shouldBe 10000
      config.esEndpoint shouldBe "http://elasticsearch:9200"
      config.maxInFlightBulkRequests shouldBe 10
      config.maxDocsInBulk shouldBe 100
      config.maxBulkDocSizeInBytes shouldBe 1000000
      config.indexTemplateJson shouldBe Some("some_template_json")
      config.consistencyLevel shouldBe "one"
      config.readTimeoutMillis shouldBe 5000
      config.connectionTimeoutMillis shouldBe 10000
      config.indexName shouldBe "service-metadata"
      config.indexType shouldBe "metadata"
      config.retryConfig.maxRetries shouldBe 10
      config.retryConfig.backOffInMillis shouldBe 100
      config.retryConfig.backoffFactor shouldBe 2
    }

    it("should load the trace backend config from base.conf and few properties overridden from env variable") {
      val backendConfiguration = project.backendConfig

      backendConfiguration.maxInFlightRequests shouldBe 100
    }

    it("should load the elastic search config from base.conf and one property overridden from env variable") {
      val elastic = project.elasticSearchConfig
      elastic.endpoint shouldBe "http://elasticsearch:9200"
      elastic.maxInFlightBulkRequests shouldBe 10
      elastic.maxDocsInBulk shouldBe 100
      elastic.maxBulkDocSizeInBytes shouldBe 1000000
      elastic.indexTemplateJson shouldBe Some("some_template_json")
      elastic.consistencyLevel shouldBe "one"
      elastic.readTimeoutMillis shouldBe 5000
      elastic.connectionTimeoutMillis shouldBe 10000
      elastic.indexNamePrefix shouldBe "haystack-test"
      elastic.indexType shouldBe "spans"
      elastic.retryConfig.maxRetries shouldBe 10
      elastic.retryConfig.backOffInMillis shouldBe 1000
      elastic.retryConfig.backoffFactor shouldBe 2
      elastic.indexHourBucket shouldBe 6
      elastic.maxConnectionsPerRoute shouldBe 10

      elastic.awsRequestSigningConfiguration.enabled shouldEqual false
      elastic.awsRequestSigningConfiguration.region shouldEqual "us-west-2"
      elastic.awsRequestSigningConfiguration.awsServiceName shouldEqual "es"
      elastic.awsRequestSigningConfiguration.accessKey shouldBe None
      elastic.awsRequestSigningConfiguration.secretKey shouldBe None
    }
  }
}
