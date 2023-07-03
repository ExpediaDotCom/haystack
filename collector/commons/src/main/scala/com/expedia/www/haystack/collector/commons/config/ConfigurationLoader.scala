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

package com.expedia.www.haystack.collector.commons.config

import java.io.File
import java.util
import java.util.Properties

import com.expedia.www.haystack.span.decorators.plugin.config.{Plugin, PluginConfiguration}
import com.typesafe.config._
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerConfig.{KEY_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER_CLASS_CONFIG}
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object ConfigurationLoader {

  private val LOGGER = LoggerFactory.getLogger(ConfigurationLoader.getClass)

  private[haystack] val ENV_NAME_PREFIX = "HAYSTACK_PROP_"

  /**
    * Load and return the configuration
    * if overrides_config_path env variable exists, then we load that config file and use base conf as fallback,
    * else we load the config from env variables(prefixed with haystack) and use base conf as fallback
    *
    * @param resourceName  name of the resource file to be loaded. Default value is `config/base.conf`
    * @param envNamePrefix env variable prefix to override config values. Default is `HAYSTACK_PROP_`
    * @return an instance of com.typesafe.Config
    */
  def loadConfigFileWithEnvOverrides(resourceName: String = "config/base.conf",
                                     envNamePrefix: String = ENV_NAME_PREFIX): Config = {

    require(resourceName != null && resourceName.length > 0, "resourceName is required")
    require(envNamePrefix != null && envNamePrefix.length > 0, "envNamePrefix is required")

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
    * @return new config object with haystack specific environment variables
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
    *
    * @param env environment variable name
    * @return variable name that complies with hocon key
    */
  private def transformEnvVarName(env: String, envNamePrefix: String): String = {
    env.replaceFirst(envNamePrefix, "").toLowerCase.replace("_", ".")
  }

  /**
    * converts the env variable value to iterable object if it starts and ends with '[' and ']' respectively.
    *
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

  def kafkaProducerConfig(config: Config): KafkaProduceConfiguration = {
    val props = new Properties()

    val kafka = config.getConfig("kafka.producer")

    kafka.getConfig("props").entrySet() foreach {
      kv => {
        props.setProperty(kv.getKey, kv.getValue.unwrapped().toString)
      }
    }

    props.put(KEY_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getCanonicalName)
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getCanonicalName)

    val produceTopic = kafka.getString("topic")

    // verify if at least bootstrap server config is set
    require(props.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG).nonEmpty)
    require(produceTopic.nonEmpty)

    KafkaProduceConfiguration(produceTopic, props)
  }

  def extractorConfiguration(config: Config): ExtractorConfiguration = {
    val extractor = config.getConfig("extractor")
    val spanValidation = extractor.getConfig("spans.validation")
    val maxSizeValidationConfig = spanValidation.getConfig("max.size")
    ExtractorConfiguration(
      outputFormat = if (extractor.hasPath("output.format")) Format.withName(extractor.getString("output.format")) else Format.PROTO,
      spanValidation = SpanValidation(SpanMaxSize(
        maxSizeValidationConfig.getBoolean("enable"),
        maxSizeValidationConfig.getBoolean("log.only"),
        maxSizeValidationConfig.getInt("max.size.limit"),
        maxSizeValidationConfig.getString("message.tag.key"),
        maxSizeValidationConfig.getString("message.tag.value"),
        maxSizeValidationConfig.getStringList("skip.tags").map(_.toLowerCase),
        maxSizeValidationConfig.getStringList("skip.services").map(_.toLowerCase))
      ))
  }

  def externalKafkaConfiguration(config: Config): List[ExternalKafkaConfiguration] = {
    if (!config.hasPath("external.kafka")) {
      return List[ExternalKafkaConfiguration]()
    }

    val kafkaProducerConfig: ConfigObject = config.getObject("external.kafka")
    kafkaProducerConfig.unwrapped().map(c => {
      val props = new Properties()
      val cfg = ConfigFactory.parseMap(c._2.asInstanceOf[util.HashMap[String, Object]])
      val topic = cfg.getString("config.topic")
      val tags = cfg.getConfig("tags").entrySet().foldRight(Map[String, String]())((t, tMap) => {
        tMap + (t.getKey -> t.getValue.unwrapped().toString)
      })
      val temp = cfg.getConfig("config.props").entrySet() foreach {
        kv => {
          props.setProperty(kv.getKey, kv.getValue.unwrapped().toString)
        }
      }

      props.put(KEY_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getCanonicalName)
      props.put(VALUE_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getCanonicalName)

      ExternalKafkaConfiguration(tags, KafkaProduceConfiguration(topic, props))
    }).toList
  }

  def additionalTagsConfiguration(config: Config): Map[String, String] = {
    if (!config.hasPath("additionaltags")) {
      return Map[String, String]()
    }
    val additionalTagsConfig = config.getConfig("additionaltags")
    val additionalTags = additionalTagsConfig.entrySet().foldRight(Map[String, String]())((t, tMap) => {
      tMap + (t.getKey -> t.getValue.unwrapped().toString)
    })
    additionalTags
  }

  def pluginConfigurations(config: Config): Plugin = {
    if (!config.hasPath("plugins")) {
      return null
    }
    val directory = config.getString("plugins.directory")
    val pluginConfigurationsList = config.getObject("plugins").unwrapped().filter(c => !"directory".equals(c._1)).map(c => {
      val pluginConfig = ConfigFactory.parseMap(c._2.asInstanceOf[util.HashMap[String, Object]])
      new PluginConfiguration(
        pluginConfig.getString("name"),
        pluginConfig.getConfig("config")
      )
    }).toList
    new Plugin(directory, pluginConfigurationsList)
  }
}
