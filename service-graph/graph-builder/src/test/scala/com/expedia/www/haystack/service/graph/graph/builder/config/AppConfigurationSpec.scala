/*
 *
 *     Copyright 2018 Expedia, Inc.
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
package com.expedia.www.haystack.service.graph.graph.builder.config

import com.expedia.www.haystack.service.graph.graph.builder.TestSpec
import com.expedia.www.haystack.service.graph.graph.builder.config.entities.CustomRocksDBConfig
import com.typesafe.config.ConfigException
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.rocksdb.{BlockBasedTableConfig, Options}

import scala.collection.JavaConverters._

class AppConfigurationSpec extends TestSpec {
  describe("loading application configuration") {
    it("should fail creating KafkaConfiguration if no application id is specified") {
      Given("a test configuration file")
      val file = "test/test_no_app_id.conf"

      When("Application configuration is loaded")

      Then("it should throw an exception")
      intercept[IllegalArgumentException] {
        new AppConfiguration(file).kafkaConfig
      }
    }

    it("should fail creating KafkaConfiguration if no bootstrap is specified") {
      Given("a test configuration file")
      val file = "test/test_no_bootstrap.conf"

      When("Application configuration is loaded")

      Then("it should throw an exception")
      intercept[IllegalArgumentException] {
        new AppConfiguration(file).kafkaConfig
      }
    }

    it("should fail creating KafkaConfiguration if no consumer is specified") {
      Given("a test configuration file")
      val file = "test/test_no_consumer.conf"

      When("Application configuration is loaded")

      Then("it should throw an exception")
      intercept[ConfigException] {
        new AppConfiguration(file).kafkaConfig
      }
    }

    it("should fail creating KafkaConfiguration if no producer is specified") {
      Given("a test configuration file")
      val file = "test/test_no_producer.conf"

      When("Application configuration is loaded")

      Then("it should throw an exception")
      intercept[ConfigException] {
        new AppConfiguration(file).kafkaConfig
      }
    }

    it("should create KafkaConfiguration and ServiceConfiguration as specified") {
      Given("a test configuration file")
      val file = "test/test.conf"

      When("Application configuration is loaded and KafkaConfiguration is obtained")
      val config = new AppConfiguration(file)

      Then("it should load as expected")
      config.kafkaConfig.streamsConfig.defaultTimestampExtractor() shouldBe a [WallclockTimestampExtractor]
      config.kafkaConfig.consumerTopic should be ("graph-nodes")
      config.serviceConfig.http.port should be (8080)
      config.serviceConfig.threads.max should be(5)
      config.serviceConfig.client.connectionTimeout should be(1000)
      val rocksDbOptions = new Options()
      new CustomRocksDBConfig().setConfig("", rocksDbOptions, Map[String, AnyRef]().asJava)
      val blockConfig = rocksDbOptions.tableFormatConfig().asInstanceOf[BlockBasedTableConfig]
      blockConfig.blockCacheSize() shouldBe 16777216l
      blockConfig.blockSize() shouldBe 16384l
      blockConfig.cacheIndexAndFilterBlocks() shouldBe true
      rocksDbOptions.maxWriteBufferNumber() shouldBe 2
      config.kafkaConfig.streamsConfig.values().get(StreamsConfig.APPLICATION_SERVER_CONFIG).toString shouldBe "localhost:8080"
    }

    it("should allow for the application server to be set in the config file") {
      Given("a test configuration file")
      val file = "test/test_application_server_set.conf"

      When("Application configuration is loaded and KafkaConfiguration is obtained")
      val config = new AppConfiguration(file)

      Then("it should load the application.server expected")
      config.kafkaConfig.streamsConfig.values().get(StreamsConfig.APPLICATION_SERVER_CONFIG).toString shouldBe "127.0.0.1:1002"
    }
  }
}
