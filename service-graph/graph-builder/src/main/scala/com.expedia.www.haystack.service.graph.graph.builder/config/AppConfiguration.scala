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

import java.util.Properties

import com.expedia.www.haystack.commons.config.ConfigurationLoader
import com.expedia.www.haystack.service.graph.graph.builder.config.entities._
import com.typesafe.config.Config
import org.apache.commons.lang3.StringUtils
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology.AutoOffsetReset

import scala.collection.JavaConverters._

/**
  * This class reads the configuration from the given resource name using {@link ConfigurationLoader ConfigurationLoader}
  *
  * @param resourceName name of the resource file to load
  */
class AppConfiguration(resourceName: String) {

  require(StringUtils.isNotBlank(resourceName))

  private val config = ConfigurationLoader.loadConfigFileWithEnvOverrides(resourceName = this.resourceName)

  /**
    * default constructor. Loads config from resource name to "app.conf"
    */
  def this() = this("app.conf")

  /**
    * Location of the health status file
    */
  val healthStatusFilePath: String = config.getString("health.status.path")

  /**
    * Instance of {@link KafkaConfiguration KafkaConfiguration} to be used by the kstreams application
    */
  lazy val kafkaConfig: KafkaConfiguration = {

    // verify if the applicationId and bootstrap server config are non empty
    def verifyRequiredProps(props: Properties): Unit = {
      require(StringUtils.isNotBlank(props.getProperty(StreamsConfig.APPLICATION_ID_CONFIG)))
      require(StringUtils.isNotBlank(props.getProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)))
      require(StringUtils.isNotBlank(props.getProperty(StreamsConfig.APPLICATION_SERVER_CONFIG)))
    }

    def addProps(config: Config, props: Properties, prefix: (String) => String = identity): Unit = {
      config.entrySet().asScala.foreach(kv => {
        val propKeyName = prefix(kv.getKey)
        props.setProperty(propKeyName, kv.getValue.unwrapped().toString)
      })
    }

    val kafka = config.getConfig("kafka")
    val streamsConfig = kafka.getConfig("streams")
    val consumerConfig = kafka.getConfig("consumer")
    val producerConfig = kafka.getConfig("producer")

    // add stream specific properties
    val streamProps = new Properties
    addProps(streamsConfig, streamProps)
    // add stream application server config
    if (StringUtils.isBlank(streamProps.getProperty(StreamsConfig.APPLICATION_SERVER_CONFIG))) {
      streamProps.setProperty(StreamsConfig.APPLICATION_SERVER_CONFIG, s"${config.getString("service.host")}:${config.getInt("service.http.port")}")
    }

    if (kafka.hasPath("rocksdb")) {
      CustomRocksDBConfig.setRocksDbConfig(kafka.getConfig("rocksdb"))
      streamProps.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, classOf[CustomRocksDBConfig])
    }

    // validate props
    verifyRequiredProps(streamProps)

    // offset reset for kstream
    val autoOffsetReset =
      if (streamsConfig.hasPath("auto.offset.reset")) {
        AutoOffsetReset.valueOf(streamsConfig.getString("auto.offset.reset").toUpperCase)
      } else {
        AutoOffsetReset.LATEST
      }
    
    val aggregation = kafka.getConfig("aggregate")
    val aggregationWindowSec = aggregation.getInt("window.sec")
    val aggregationRetentionDays = aggregation.getInt("retention.days")

    KafkaConfiguration(new StreamsConfig(streamProps),
      consumerConfig.getString("topic"),
      producerConfig.getString("topic"),
      autoOffsetReset,
      kafka.getLong("close.timeout.ms"),
      aggregationWindowSec,
      aggregationRetentionDays
    )
  }

  /**
    * Instance of {@link ServiceConfiguration} to be used by servlet container
    */
  lazy val serviceConfig: ServiceConfiguration = {
    val service = config.getConfig("service")
    val threads = service.getConfig("threads")
    val http = service.getConfig("http")
    val client = service.getConfig("client")

    ServiceConfiguration(
      service.getString("host"),
      ServiceThreadsConfiguration(
        threads.getInt("min"),
        threads.getInt("max"),
        threads.getInt("idle.timeout")
      ),
      ServiceHttpConfiguration(
        http.getInt("port"),
        http.getLong("idle.timeout")
      ),
      ServiceClientConfiguration(
        client.getInt("connection.timeout"),
        client.getInt("socket.timeout")
      )
    )
  }
}
