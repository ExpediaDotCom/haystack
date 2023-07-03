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

package com.expedia.www.haystack.commons.config

import com.expedia.www.haystack.commons.unit.UnitTestSpec
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

class ConfigurationLoaderSpec extends UnitTestSpec {
  private val keyName = "traces.key.sequence"

  "ConfigurationLoader.loadConfigFileWithEnvOverrides" should {

    "load a given config file as expected when no environment overrides are present" in {
      Given("a sample HOCON conf file")
      val file = "sample.conf"
      When("loadConfigFileWithEnvOverrides is invoked with no environment variables")
      val config = ConfigurationLoader.loadConfigFileWithEnvOverrides(resourceName = file)
      Then("it should load the configuration entries as expected")
      "influxdb.kube-system.svc" should equal(config.getString("haystack.graphite.host"))
      2003 should equal(config.getInt("haystack.graphite.port"))
    }
  }

  "ConfigurationLoader.parsePropertiesFromMap" should {
    "parses a given map and returns transformed key-value that matches a given prefix" in {
      Given("a sample map with a key-value")
      val data = Map("FOO_HAYSTACK_GRAPHITE_HOST" -> "influxdb.kube-system.svc", "foo.bar" -> "baz")
      When("parsePropertiesFromMap is invoked with matching prefix")
      val config = ConfigurationLoader.parsePropertiesFromMap(data, Set(), "FOO_")
      Then("it should transform the entries that match the prefix as expected")
      Some("influxdb.kube-system.svc") should equal(config.get("haystack.graphite.host"))
      None should be(config.get("foo.bar"))
    }

    "parses a given map with empty array of values and return transformed key-value that matches a given prefix" in {
      Given("a sample map with a key and empty array of values")
      val envVars = Map[String, String](ConfigurationLoader.ENV_NAME_PREFIX + "TRACES_KEY_SEQUENCE" -> "[]")
      When("parsePropertiesFromMap is invoked")
      val config = ConfigFactory.parseMap(ConfigurationLoader.parsePropertiesFromMap(envVars, Set(keyName), ConfigurationLoader.ENV_NAME_PREFIX).asJava)
      Then("it should return an empty list with given key")
      config.getList(keyName).size() shouldBe 0
    }

    "parses a given map with non-empty array of values and return transformed key-value that matches a given prefix" in {
      Given("a sample map with a key and empty array of values")
      val envVars = Map[String, String](ConfigurationLoader.ENV_NAME_PREFIX + "TRACES_KEY_SEQUENCE" -> "[v1]")
      When("parsePropertiesFromMap is invoked")
      val config = ConfigFactory.parseMap(ConfigurationLoader.parsePropertiesFromMap(envVars, Set(keyName), ConfigurationLoader.ENV_NAME_PREFIX).asJava)
      Then("it should return an empty list with given key")
      config.getStringList(keyName).size() shouldBe 1
      config.getStringList(keyName).get(0) shouldBe "v1"
    }

    "should throw runtime exception if env variable doesn't comply array value signature - [..]" in {
      Given("a sample map with a key and non compliant array of values")
      val envVars = Map[String, String](ConfigurationLoader.ENV_NAME_PREFIX + "TRACES_KEY_SEQUENCE" -> "v1")
      When("parsePropertiesFromMap is invoked")
      val exception = intercept[RuntimeException] {
        ConfigurationLoader.parsePropertiesFromMap(envVars, Set(keyName), ConfigurationLoader.ENV_NAME_PREFIX)
      }
      Then("it should throw exception with excepted message")
      exception.getMessage shouldEqual "config key is of array type, so it should start and end with '[', ']' respectively"
    }

    "should load config from env variable with non-empty value" in {
      Given("a sample map with a key and empty array of values")
      val envVars = Map[String, String](
        ConfigurationLoader.ENV_NAME_PREFIX + "TRACES_KEY_SEQUENCE" -> "[v1]",
        ConfigurationLoader.ENV_NAME_PREFIX + "TRACES_KEY2" -> "v2",
        "NON_HAYSTACK_KEY" -> "not_interested")

      When("parsePropertiesFromMap is invoked")
      val config = ConfigFactory.parseMap(ConfigurationLoader.parsePropertiesFromMap(envVars, Set(keyName), ConfigurationLoader.ENV_NAME_PREFIX).asJava)
      Then("it should return an empty list with given key")
      config.getStringList(keyName).size() shouldBe 1
      config.getStringList(keyName).get(0) shouldBe "v1"
      config.getString("traces.key2") shouldBe "v2"
      config.hasPath("non.haystack.key") shouldBe false
    }
  }
}
