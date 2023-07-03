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

package com.expedia.www.haystack.trace.indexer.integration.clients

import java.util.Properties

import com.expedia.www.haystack.trace.indexer.config.entities.KafkaConfiguration
import com.expedia.www.haystack.trace.indexer.integration.serdes.{SnappyCompressedSpanBufferProtoDeserializer, SpanProtoSerializer}
import com.expedia.www.haystack.trace.indexer.serde.SpanDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster

object KafkaTestClient {
  val KAFKA_CLUSTER = new EmbeddedKafkaCluster(1)
  KAFKA_CLUSTER.start()
}

class KafkaTestClient {
  import KafkaTestClient._

  val INPUT_TOPIC = "spans"
  val OUTPUT_TOPIC = "span-buffer"

  val APP_PRODUCER_CONFIG: Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CLUSTER.bootstrapServers)
    props.put(ProducerConfig.ACKS_CONFIG, "1")
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, "20")
    props.put(ProducerConfig.RETRIES_CONFIG, "0")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer])
    props
  }

  val APP_CONSUMER_CONFIG: Properties = new Properties()

  val TEST_PRODUCER_CONFIG: Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CLUSTER.bootstrapServers)
    props.put(ProducerConfig.ACKS_CONFIG, "1")
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, "20")
    props.put(ProducerConfig.RETRIES_CONFIG, "0")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[SpanProtoSerializer])
    props
  }

  val RESULT_CONSUMER_CONFIG = new Properties()

  def buildConfig = KafkaConfiguration(numStreamThreads = 1,
    pollTimeoutMs = 100,
    APP_CONSUMER_CONFIG, APP_PRODUCER_CONFIG, OUTPUT_TOPIC, INPUT_TOPIC,
    consumerCloseTimeoutInMillis = 3000,
    commitOffsetRetries = 3,
    commitBackoffInMillis = 250,
    maxWakeups = 5, wakeupTimeoutInMillis = 3000)

  def prepare(appId: String): Unit = {
    APP_CONSUMER_CONFIG.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CLUSTER.bootstrapServers)
    APP_CONSUMER_CONFIG.put(ConsumerConfig.GROUP_ID_CONFIG, appId + "-app-consumer")
    APP_CONSUMER_CONFIG.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    APP_CONSUMER_CONFIG.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    APP_CONSUMER_CONFIG.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[SpanDeserializer])
    APP_CONSUMER_CONFIG.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CLUSTER.bootstrapServers)
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.GROUP_ID_CONFIG, appId + "-result-consumer")
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[SnappyCompressedSpanBufferProtoDeserializer])

    deleteTopics(INPUT_TOPIC, OUTPUT_TOPIC)
    KAFKA_CLUSTER.createTopic(INPUT_TOPIC, 2, 1)
    KAFKA_CLUSTER.createTopic(OUTPUT_TOPIC)
  }

  private def deleteTopics(topics: String*): Unit = KAFKA_CLUSTER.deleteTopicsAndWait(topics:_*)
}
