/*
 *
 *     Copyright 2017 Expedia, Inc.
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
package com.expedia.www.haystack.trends.integration


import java.util.Properties
import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.commons.entities.encoders.PeriodReplacementEncoder
import com.expedia.www.haystack.commons.kstreams.serde.SpanSerde
import com.expedia.www.haystack.commons.kstreams.serde.metricdata.MetricTankSerde
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.streams.integration.utils.{EmbeddedKafkaCluster, IntegrationTestUtils}
import org.apache.kafka.streams.{KeyValue, StreamsConfig}
import org.scalatest._

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

object EmbeddedKafka {
  val CLUSTER = new EmbeddedKafkaCluster(1)
  CLUSTER.start()
}

class IntegrationTestSpec extends WordSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {

  protected val PUNCTUATE_INTERVAL_MS = 2000
  protected val PRODUCER_CONFIG = new Properties()
  protected val RESULT_CONSUMER_CONFIG = new Properties()
  protected val STREAMS_CONFIG = new Properties()
  protected val scheduledJobFuture: ScheduledFuture[_] = null
  protected val INPUT_TOPIC = "spans"
  protected val OUTPUT_TOPIC = "metricpoints"
  protected var scheduler: ScheduledExecutorService = _
  protected var APP_ID = "haystack-trends"
  protected val METRIC_TYPE = "gauge"
  protected var CHANGELOG_TOPIC = ""
  protected var KAFKA_ENDPOINT = "192.168.99.100:9092"


  override def beforeAll() {
    scheduler = Executors.newSingleThreadScheduledExecutor()
  }

  override def afterAll(): Unit = {
    scheduler.shutdownNow()
  }

  override def beforeEach() {
    val metricTankSerde = new MetricTankSerde()

    EmbeddedKafka.CLUSTER.createTopic(INPUT_TOPIC)
    EmbeddedKafka.CLUSTER.createTopic(OUTPUT_TOPIC)

    PRODUCER_CONFIG.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, EmbeddedKafka.CLUSTER.bootstrapServers)
    PRODUCER_CONFIG.put(ProducerConfig.ACKS_CONFIG, "all")
    PRODUCER_CONFIG.put(ProducerConfig.RETRIES_CONFIG, "0")
    PRODUCER_CONFIG.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    PRODUCER_CONFIG.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new SpanSerde().serializer().getClass)

    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, EmbeddedKafka.CLUSTER.bootstrapServers)
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.GROUP_ID_CONFIG, APP_ID + "-result-consumer")
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, metricTankSerde.deserializer().getClass)

    STREAMS_CONFIG.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, EmbeddedKafka.CLUSTER.bootstrapServers)
    STREAMS_CONFIG.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID)
    STREAMS_CONFIG.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    STREAMS_CONFIG.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    STREAMS_CONFIG.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "300")
    STREAMS_CONFIG.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams")

    IntegrationTestUtils.purgeLocalStreamsState(STREAMS_CONFIG)

    CHANGELOG_TOPIC = s"$APP_ID-AggregatedMetricPointStore-changelog"
  }

  override def afterEach(): Unit = {
    EmbeddedKafka.CLUSTER.deleteTopic(INPUT_TOPIC)
    EmbeddedKafka.CLUSTER.deleteTopic(OUTPUT_TOPIC)
  }

  protected def produceSpansAsync(produceInterval: FiniteDuration,
                                  spans: List[Span]): Unit = {
    var currentTime = System.currentTimeMillis()
    var idx = 0
    scheduler.scheduleWithFixedDelay(() => {
      if (idx < spans.size) {
        currentTime = currentTime + ((idx * PUNCTUATE_INTERVAL_MS) / (spans.size - 1))
        val span = spans.apply(idx)
        val records = List(new KeyValue[String, Span](span.getTraceId, span)).asJava
        IntegrationTestUtils.produceKeyValuesSynchronouslyWithTimestamp(
          INPUT_TOPIC,
          records,
          PRODUCER_CONFIG,
          currentTime)
      }
      idx = idx + 1
    }, 0, produceInterval.toMillis, TimeUnit.MILLISECONDS)
  }
}
