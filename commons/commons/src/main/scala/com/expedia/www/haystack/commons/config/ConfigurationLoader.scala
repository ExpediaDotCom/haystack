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

import java.io.File

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions, ConfigValueType}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object ConfigurationLoader {

  private val LOGGER = LoggerFactory.getLogger(ConfigurationLoader.getClass)

  private[haystack] val ENV_NAME_PREFIX = "HAYSTACK_PROP_"

  /**
    * Load and return the configuration
    * if overrides_config_path env variable exists, then we load that config file and use base conf as fallback,
    * else we load the config from env variables(prefixed with haystack) and use base conf as fallback
    *
    * @param resourceName name of the resource file to be loaded. Default value is `config/base.conf`
    * @param envNamePrefix env variable prefix to override config values. Default is `HAYSTACK_PROP_`
    *
    * @return an instance of com.typesafe.Config
    */
  def loadConfigFileWithEnvOverrides(resourceName : String = "config/base.conf",
                                     envNamePrefix : String = ENV_NAME_PREFIX) : Config = {

    require(resourceName != null && resourceName.length > 0 , "resourceName is required")
    require(envNamePrefix != null && envNamePrefix.length > 0 , "envNamePrefix is required")

    val baseConfig = ConfigFactory.load(resourceName)

    val keysWithArrayValues = baseConfig.entrySet()
      .asScala
      .filter(_.getValue.valueType() == ConfigValueType.LIST)
      .map(_.getKey)
      .toSet

    val config = sys.env.get("HAYSTACK_OVERRIDES_CONFIG_PATH") match {
      case Some(overrideConfigPath) =>
        val overrideConfig = ConfigFactory.parseFile(new File(overrideConfigPath))
        ConfigFactory
          .parseMap(parsePropertiesFromMap(sys.env, keysWithArrayValues, envNamePrefix).asJava)
          .withFallback(overrideConfig)
          .withFallback(baseConfig)
          .resolve()
      case _ => ConfigFactory
        .parseMap(parsePropertiesFromMap(sys.env, keysWithArrayValues, envNamePrefix).asJava)
        .withFallback(baseConfig)
        .resolve()
    }

    // In key-value pairs that contain 'password' in the key, replace the value with asterisks
    LOGGER.info(config.root()
      .render(ConfigRenderOptions.defaults().setOriginComments(false))
      .replaceAll("(?i)(\\\".*password\\\"\\s*:\\s*)\\\".+\\\"", "$1********"))

    config
  }

  /**
    *  @return new config object with haystack specific environment variables
    */
  private[haystack] def parsePropertiesFromMap(envVars: Map[String, String],
                                               keysWithArrayValues: Set[String],
                                               envNamePrefix: String): Map[String, Object] = {
    envVars.filter {
      case (envName, _) => envName.startsWith(envNamePrefix)
    } map {
      case (envName, envValue) =>
        val key = transformEnvVarName(envName, envNamePrefix)
        if (keysWithArrayValues.contains(key)) (key, transformEnvVarArrayValue(envValue)) else (key, envValue)
    }
  }

  /**
    * converts the env variable to HOCON format
    * for e.g. env variable HAYSTACK_KAFKA_STREAMS_NUM_STREAM_THREADS gets converted to kafka.streams.num.stream.threads
    * @param env environment variable name
    * @return variable name that complies with hocon key
    */
  private def transformEnvVarName(env: String, envNamePrefix: String): String = {
    env.replaceFirst(envNamePrefix, "").toLowerCase.replace("_", ".")
  }

  /**
    * converts the env variable value to iterable object if it starts and ends with '[' and ']' respectively.
    * @param env environment variable value
    * @return string or iterable object
    */
  private def transformEnvVarArrayValue(env: String): java.util.List[String] = {
    if (env.startsWith("[") && env.endsWith("]")) {
      import scala.collection.JavaConverters._
      env.substring(1, env.length - 1).split(',').filter(str => (str != null) && str.nonEmpty).toList.asJava
    } else {
      throw new RuntimeException("config key is of array type, so it should start and end with '[', ']' respectively")
    }
  }
}
