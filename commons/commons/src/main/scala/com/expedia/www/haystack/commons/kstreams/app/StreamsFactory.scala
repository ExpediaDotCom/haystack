package com.expedia.www.haystack.commons.kstreams.app

import java.util.Properties
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}

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

import org.slf4j.LoggerFactory

import scala.util.Try

/**
  * Factory class to create a KafkaStreams instance and wrap it as a simple service {@see ManagedKafkaStreams}
  *
  * Optionally this class can check the presence of consuming topic
  *
  * @param topologySupplier  A supplier that creates and returns a Kafka Stream Topology
  * @param streamsConfig Configuration instance for KafkaStreams
  * @param consumerTopic Optional consuming topic name
  */
class StreamsFactory(topologySupplier: Supplier[Topology], streamsConfig: StreamsConfig, consumerTopic: String) {

  require(topologySupplier != null, "streamsBuilder is required")
  require(streamsConfig != null, "streamsConfig is required")

  val consumerTopicName = Option(consumerTopic)

  def this(streamsSupplier: Supplier[Topology], streamsConfig: StreamsConfig) = this(streamsSupplier, streamsConfig, null)

  private val LOGGER = LoggerFactory.getLogger(classOf[StreamsFactory])

  /**
    * creates a new instance of KafkaStreams application wrapped as a {@link ManagedService} instance
    * @param listener instance of StateChangeListener that observes KafkaStreams state changes
    * @return instance of ManagedService
    */
  def create(listener: StateChangeListener): ManagedService = {
    checkConsumerTopic()

    val streams = new KafkaStreams(topologySupplier.get(), streamsConfig)
    streams.setStateListener(listener)
    streams.setUncaughtExceptionHandler(listener)
    streams.cleanUp()

    val timeOut = Option(streamsConfig.getInt(StreamsConfig.REQUEST_TIMEOUT_MS_CONFIG)) match {
      case Some(v) if v > 0 =>  v / 1000
      case _ => 5
    }

    new ManagedKafkaStreams(streams, timeOut)
  }

  private def checkConsumerTopic(): Unit = {
    if (consumerTopicName.nonEmpty) {
      val topicName = consumerTopicName.get
      LOGGER.info(s"checking for the consumer topic $topicName")
      val adminClient = AdminClient.create(getBootstrapProperties)
      try {
        val present = adminClient.listTopics().names().get().contains(topicName)
        if (!present) {
          throw new TopicNotPresentException(topicName,
            s"Topic '$topicName' is configured as a consumer and it is not present")
        }
      }
      finally {
        Try(adminClient.close(5, TimeUnit.SECONDS))
      }
    }
  }

  private def getBootstrapProperties: Properties = {
    val properties = new Properties()
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
      streamsConfig.getList(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG))
    properties
  }

  /**
    * Custom RuntimeException that represents a required Kafka topic not being present
    * @param topic Name of the topic that is missing
    * @param message Message
    */
  class TopicNotPresentException(topic: String, message: String) extends RuntimeException(message) {
    def getTopic : String = topic
  }
}

