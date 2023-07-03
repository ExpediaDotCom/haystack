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
package com.expedia.www.haystack.service.graph.node.finder.config

import com.expedia.www.haystack.TestSpec
import com.expedia.www.haystack.commons.kstreams.SpanTimestampExtractor
import com.typesafe.config.ConfigException

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
    it("should fail creating KafkaConfiguration if no metrics topic is specified") {
      Given("a test configuration file")
      val file = "test/test_no_metrics_topic.conf"
      When("Application configuration is loaded")
      Then("it should throw an exception")
      intercept[ConfigException] {
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
    it("should create KafkaConfiguration as specified") {
      Given("a test configuration file")
      val file = "test/test.conf"
      When("Application configuration is loaded and KafkaConfiguration is obtained")
      val config = new AppConfiguration(file).kafkaConfig
      Then("it should load as expected")
      config.streamsConfig.defaultTimestampExtractor() shouldBe a [SpanTimestampExtractor]
      config.serviceCallTopic should be ("graph-nodes")
      config.accumulatorInterval should be (60000)
      config.metadataConfig.topic should be ("haystack-node-finder-metadata")
      config.metadataConfig.partitionCount should be (6)
      config.metadataConfig.replicationFactor should be (2)
    }
  }
}
