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

package com.expedia.www.haystack.service.graph.graph.builder.stream

import java.util.Properties
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

import com.expedia.www.haystack.commons.health.HealthStatusController
import com.expedia.www.haystack.commons.kstreams.app.StateChangeListener
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.slf4j.LoggerFactory

import scala.util.Try

/**
  * Factory class to create a KafkaStreams instance and wrap it as a simple service {@see ManagedKafkaStreams}
  *
  * Optionally this class can check the presence of consuming topic
  *
  * @param topologySupplier A supplier that creates and returns a Kafka Stream Topology
  * @param healthController health controller
  * @param streamsConfig    Configuration instance for KafkaStreams
  * @param consumerTopic    Optional consuming topic name
  */
//noinspection ScalaDocInlinedTag,ScalaDocParserErrorInspection
class StreamSupplier(topologySupplier: Supplier[Topology],
                     healthController: HealthStatusController,
                     streamsConfig: StreamsConfig,
                     consumerTopic: String,
                     var adminClient: AdminClient = null) extends Supplier[KafkaStreams] {

  require(topologySupplier != null, "streamsBuilder is required")
  require(healthController != null, "healthStatusController is required")
  require(streamsConfig != null, "streamsConfig is required")
  require(consumerTopic != null && !consumerTopic.isEmpty, "consumerTopic is required")
  if(adminClient == null) {
    adminClient = AdminClient.create(getBootstrapProperties)
  }

  private val LOGGER = LoggerFactory.getLogger(classOf[StreamSupplier])

  /**
    * creates a new instance of KafkaStreams application wrapped as a {@link ManagedService} instance
    *
    * @return instance of ManagedService
    */
  override def get(): KafkaStreams = {
    checkConsumerTopic()

    val listener = new StateChangeListener(healthController)
    val streams = new KafkaStreams(topologySupplier.get(), streamsConfig)
    streams.setStateListener(listener)
    streams.setUncaughtExceptionHandler(listener)
    streams.cleanUp()

    streams
  }

  private def checkConsumerTopic(): Unit = {
    LOGGER.info(s"checking for the consumer topic $consumerTopic")
    try {
      val present = adminClient.listTopics().names().get().contains(consumerTopic)
      if (!present) {
        throw new TopicNotPresentException(consumerTopic,
          s"Topic '$consumerTopic' is configured as a consumer and it is not present")
      }
    }
    finally {
      Try(adminClient.close(5, TimeUnit.SECONDS))
    }
  }

  private def getBootstrapProperties: Properties = {
    val properties = new Properties()
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, streamsConfig.getList(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG))
    properties
  }

  /**
    * Custom RuntimeException that represents a required Kafka topic not being present
    *
    * @param topic   Name of the topic that is missing
    * @param message Message
    */
  class TopicNotPresentException(topic: String, message: String) extends RuntimeException(message) {
    def getTopic: String = topic
  }
}

