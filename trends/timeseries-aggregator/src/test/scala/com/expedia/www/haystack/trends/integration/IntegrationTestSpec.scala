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

import java.util
import java.util.Properties
import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import com.expedia.www.haystack.commons.entities.Interval
import com.expedia.www.haystack.commons.entities.encoders.PeriodReplacementEncoder
import com.expedia.www.haystack.commons.health.HealthStatusController
import com.expedia.www.haystack.commons.kstreams.app.{StateChangeListener, StreamsFactory, StreamsRunner}
import com.expedia.www.haystack.commons.kstreams.serde.metricdata.{MetricDataSerde, MetricTankSerde}
import com.expedia.www.haystack.commons.util.MetricDefinitionKeyGenerator
import com.expedia.www.haystack.commons.util.MetricDefinitionKeyGenerator._
import com.expedia.www.haystack.trends.config.AppConfiguration
import com.expedia.www.haystack.trends.config.entities.{KafkaConfiguration, KafkaProduceConfiguration, KafkaSinkTopic, StateStoreConfiguration}
import com.expedia.www.haystack.trends.kstream.Streams
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.streams.Topology.AutoOffsetReset
import org.apache.kafka.streams.integration.utils.{EmbeddedKafkaCluster, IntegrationTestUtils}
import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.apache.kafka.streams.{KeyValue, StreamsConfig}
import org.easymock.EasyMock
import org.scalatest._
import org.scalatest.easymock.EasyMockSugar

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class IntegrationTestSpec extends WordSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with EasyMockSugar {

  protected val PUNCTUATE_INTERVAL_SEC = 2000
  protected val PRODUCER_CONFIG = new Properties()
  protected val RESULT_CONSUMER_CONFIG = new Properties()
  protected val STREAMS_CONFIG = new Properties()
  protected val scheduledJobFuture: ScheduledFuture[_] = null
  protected val INPUT_TOPIC = "metric-data-points"
  protected val OUTPUT_TOPIC = "metrics"
  protected val OUTPUT_METRICTANK_TOPIC = "mdm"
  protected var scheduler: ScheduledExecutorService = _
  protected var APP_ID = "haystack-trends"
  protected var CHANGELOG_TOPIC = s"$APP_ID-trend-metric-store-changelog"
  protected var embeddedKafkaCluster: EmbeddedKafkaCluster = _

  override def beforeAll(): Unit = {
    scheduler = Executors.newScheduledThreadPool(1)
  }

  override def afterAll(): Unit = {
    scheduler.shutdownNow()
  }

  override def beforeEach() {
    val metricDataSerde = new MetricDataSerde()

    embeddedKafkaCluster = new EmbeddedKafkaCluster(1)
    embeddedKafkaCluster.start()
    embeddedKafkaCluster.createTopic(INPUT_TOPIC, 1, 1)
    embeddedKafkaCluster.createTopic(OUTPUT_TOPIC, 1, 1)

    PRODUCER_CONFIG.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaCluster.bootstrapServers)
    PRODUCER_CONFIG.put(ProducerConfig.ACKS_CONFIG, "all")
    PRODUCER_CONFIG.put(ProducerConfig.RETRIES_CONFIG, "0")
    PRODUCER_CONFIG.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    PRODUCER_CONFIG.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, metricDataSerde.serializer().getClass)

    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaCluster.bootstrapServers)
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.GROUP_ID_CONFIG, APP_ID + "-result-consumer")
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    RESULT_CONSUMER_CONFIG.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, metricDataSerde.deserializer().getClass)

    STREAMS_CONFIG.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaCluster.bootstrapServers)
    STREAMS_CONFIG.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID)
    STREAMS_CONFIG.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    STREAMS_CONFIG.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    STREAMS_CONFIG.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "1")
    STREAMS_CONFIG.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "300")
    STREAMS_CONFIG.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "1")
    STREAMS_CONFIG.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams")

    IntegrationTestUtils.purgeLocalStreamsState(STREAMS_CONFIG)
  }

  override def afterEach(): Unit = {
    embeddedKafkaCluster.deleteTopics(INPUT_TOPIC, OUTPUT_TOPIC)
  }

  def currentTimeInSecs: Long = {
    System.currentTimeMillis() / 1000l
  }

  protected val stateStoreConfigs = Map("cleanup.policy" -> "compact,delete")


  protected def mockAppConfig: AppConfiguration = {
    val kafkaSinkTopics = List(KafkaSinkTopic("metrics","com.expedia.www.haystack.commons.kstreams.serde.metricdata.MetricDataSerde",true), KafkaSinkTopic("mdm","com.expedia.www.haystack.commons.kstreams.serde.metricdata.MetricTankSerde",true))
    val kafkaConfig = KafkaConfiguration(new StreamsConfig(STREAMS_CONFIG),KafkaProduceConfiguration(kafkaSinkTopics, None, "mdm", false), INPUT_TOPIC, AutoOffsetReset.EARLIEST, new WallclockTimestampExtractor, 30000)
    val projectConfiguration = mock[AppConfiguration]

    expecting {
      projectConfiguration.kafkaConfig.andReturn(kafkaConfig).anyTimes()
      projectConfiguration.stateStoreConfig.andReturn(StateStoreConfiguration(128, enableChangeLogging = true, 60, stateStoreConfigs)).anyTimes()
      projectConfiguration.encoder.andReturn(new PeriodReplacementEncoder).anyTimes()
      projectConfiguration.additionalTags.andReturn(Map("k1"->"v1", "k2"-> "v2")).anyTimes()
    }
    EasyMock.replay(projectConfiguration)
    projectConfiguration
  }

  protected def validateAggregatedMetricPoints(producedRecords: List[KeyValue[String, MetricData]],
                                               expectedOneMinAggregatedPoints: Int,
                                               expectedFiveMinAggregatedPoints: Int,
                                               expectedFifteenMinAggregatedPoints: Int,
                                               expectedOneHourAggregatedPoints: Int): Unit = {

    val oneMinAggMetricPoints = producedRecords.filter(record => getTags(record.value).get("interval").equals(Interval.ONE_MINUTE.toString()))
    val fiveMinAggMetricPoints = producedRecords.filter(record => getTags(record.value).get("interval").equals(Interval.FIVE_MINUTE.toString()))
    val fifteenMinAggMetricPoints = producedRecords.filter(record => getTags(record.value).get("interval").equals(Interval.FIFTEEN_MINUTE.toString()))
    val oneHourAggMetricPoints = producedRecords.filter(record => getTags(record.value).get("interval").equals(Interval.ONE_HOUR.toString()))

    oneMinAggMetricPoints.size shouldEqual expectedOneMinAggregatedPoints
    fiveMinAggMetricPoints.size shouldEqual expectedFiveMinAggregatedPoints
    fifteenMinAggMetricPoints.size shouldEqual expectedFifteenMinAggregatedPoints
    oneHourAggMetricPoints.size shouldEqual expectedOneHourAggregatedPoints
    validateAdditionalTags(List(oneMinAggMetricPoints, fiveMinAggMetricPoints, fifteenMinAggMetricPoints, oneHourAggMetricPoints).flatten)
  }

  protected def validateAdditionalTags(kvPair: List[KeyValue[String, MetricData]]): Unit = {
    val additionalTags = mockAppConfig.additionalTags
    kvPair.foreach(kv => {
      val tags = kv.value.getMetricDefinition.getTags.getKv.asScala
      additionalTags.toSet subsetOf tags.toSet shouldEqual true
    })
  }

  protected def produceMetricPointsAsync(maxMetricPoints: Int,
                                         produceInterval: FiniteDuration,
                                         metricName: String,
                                         totalIntervalInSecs: Long = PUNCTUATE_INTERVAL_SEC
                                        ): Unit = {
    var epochTimeInSecs = 0l
    var idx = 0
    scheduler.scheduleWithFixedDelay(() => {
      if (idx < maxMetricPoints) {
        val metricData = randomMetricData(metricName = metricName, timestamp = epochTimeInSecs)
        val keyValue = List(new KeyValue[String, MetricData](generateKey(metricData.getMetricDefinition), metricData)).asJava
        IntegrationTestUtils.produceKeyValuesSynchronouslyWithTimestamp(
          INPUT_TOPIC,
          keyValue,
          PRODUCER_CONFIG,
          epochTimeInSecs)
        epochTimeInSecs = epochTimeInSecs + (totalIntervalInSecs / (maxMetricPoints - 1))
      }
      idx = idx + 1

    }, 0, produceInterval.toMillis, TimeUnit.MILLISECONDS)
  }

  protected def produceMetricData(metricName: String,
                                  epochTimeInSecs: Long,
                                  produceTimeInSecs: Long
                                 ): Unit = {
    val metricPoint = randomMetricData(metricName = metricName, timestamp = epochTimeInSecs)
    val keyValue = List(new KeyValue[String, MetricData](generateKey(metricPoint.getMetricDefinition), metricPoint)).asJava
    IntegrationTestUtils.produceKeyValuesSynchronouslyWithTimestamp(
      INPUT_TOPIC,
      keyValue,
      PRODUCER_CONFIG,
      produceTimeInSecs)
  }

  def randomMetricData(metricName: String,
                       value: Long = Math.abs(Random.nextInt()),
                       timestamp: Long = currentTimeInSecs): MetricData = {
    getMetricData(metricName, Map[String, String](), value, timestamp)
  }

  protected def createStreamRunner(): StreamsRunner = {
    val appConfig = mockAppConfig
    val streams = new Streams(appConfig)
    val factory = new StreamsFactory(streams, appConfig.kafkaConfig.streamsConfig, appConfig.kafkaConfig.consumeTopic)
    new StreamsRunner(factory, new StateChangeListener(new HealthStatusController))


  }

  protected def getMetricData(metricKey: String, tags: Map[String, String], value: Double, timeStamp: Long): MetricData = {

    val tagsMap = new java.util.LinkedHashMap[String, String] {
      putAll(tags.asJava)
      put(MetricDefinition.MTYPE, "gauge")
      put(MetricDefinition.UNIT, "short")
    }
    val metricDefinition = new MetricDefinition(metricKey, new TagCollection(tagsMap), TagCollection.EMPTY)
    new MetricData(metricDefinition, value, timeStamp)
  }

  protected def containsTag(metricData: MetricData, tagKey: String, tagValue: String): Boolean = {
    val tags = getTags(metricData)
    tags.containsKey(tagKey) && tags.get(tagKey).equalsIgnoreCase(tagValue)
  }

  protected def getTags(metricData: MetricData): util.Map[String, String] = {
    metricData.getMetricDefinition.getTags.getKv
  }
}
