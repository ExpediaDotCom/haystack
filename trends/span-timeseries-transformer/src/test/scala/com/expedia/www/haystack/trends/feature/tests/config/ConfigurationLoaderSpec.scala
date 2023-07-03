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

package com.expedia.www.haystack.trends.feature.tests.config

import com.expedia.www.haystack.commons.entities.encoders.PeriodReplacementEncoder
import com.expedia.www.haystack.trends.config.AppConfiguration
import com.expedia.www.haystack.trends.feature.FeatureSpec

class ConfigurationLoaderSpec extends FeatureSpec {

  feature("Configuration loader") {


    scenario("should load the health status config from base.conf") {

      Given("A config file at base config file containing config for health status file path")
      val healthStatusFilePath = "/app/isHealthy"

      When("When the configuration is loaded in app configuration")
      val projectConfig = new AppConfiguration()

      Then("the healthStatusFilePath should be correct")
      projectConfig.healthStatusFilePath shouldEqual healthStatusFilePath
    }

    scenario("should load the metric point enable period replacement config from base.conf") {

      Given("A config file at base config file containing config for enable period replacement")
      val enableMetricPointServiceLevelGeneration = true

      When("When the configuration is loaded in app configuration")
      val projectConfig = new AppConfiguration()

      Then("the encoder should be correct")
      projectConfig.transformerConfiguration.encoder shouldBe an[PeriodReplacementEncoder]
      projectConfig.transformerConfiguration.enableMetricPointServiceLevelGeneration shouldEqual enableMetricPointServiceLevelGeneration
    }

    scenario("should load the kafka config from base.conf") {

      Given("A config file at base config file containing kafka ")

      When("When the configuration is loaded in app configuration")
      val projectConfig = new AppConfiguration()

      Then("It should create the write configuration object based on the file contents")
      val kafkaConfig = projectConfig.kafkaConfig
      kafkaConfig.consumeTopic shouldBe "proto-spans"
    }


    scenario("should override configuration based on environment variable") {


      Given("A config file at base config file containing config for kafka")

      When("When the configuration is loaded in app configuration")
      val projectConfig = new AppConfiguration()

      Then("It should override the configuration object based on the environment variable if it exists")

      val kafkaProduceTopic = sys.env.getOrElse("HAYSTACK_PROP_KAFKA_PRODUCER_TOPIC", "metric-data-points")
      val kafkaConfig = projectConfig.kafkaConfig
      kafkaConfig.produceTopic shouldBe kafkaProduceTopic
    }

  }
}
