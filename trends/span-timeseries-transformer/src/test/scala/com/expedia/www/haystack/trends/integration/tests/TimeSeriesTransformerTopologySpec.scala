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
package com.expedia.www.haystack.trends.integration.tests

import java.util.UUID

import com.expedia.metrics.{MetricData, MetricDefinition}
import com.expedia.open.tracing.Span
import com.expedia.www.haystack.commons.entities.TagKeys
import com.expedia.www.haystack.commons.entities.encoders.PeriodReplacementEncoder
import com.expedia.www.haystack.commons.health.HealthStatusController
import com.expedia.www.haystack.commons.kstreams.app.{StateChangeListener, StreamsFactory, StreamsRunner}
import com.expedia.www.haystack.commons.util.MetricDefinitionKeyGenerator
import com.expedia.www.haystack.trends.config.entities.{KafkaConfiguration, TransformerConfiguration}
import com.expedia.www.haystack.trends.integration.IntegrationTestSpec
import com.expedia.www.haystack.trends.transformer.MetricDataTransformer
import com.expedia.www.haystack.trends.{MetricDataGenerator, Streams}
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.streams.Topology.AutoOffsetReset
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils
import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.apache.kafka.streams.{KeyValue, StreamsConfig}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class TimeSeriesTransformerTopologySpec extends IntegrationTestSpec with MetricDataGenerator {

  "TimeSeries Transformer Topology" should {

    "consume spans from input topic and transform them to metric data list based on available transformers" in {

      Given("a set of spans and kafka specific configurations")
      val traceId = "trace-id-dummy"
      val spanId = "span-id-dummy"
      val duration = 3
      val errorFlag = false
      val spans = generateSpans(traceId, spanId, duration, errorFlag, 10000, 8)
      val kafkaConfig = KafkaConfiguration(new StreamsConfig(STREAMS_CONFIG), OUTPUT_TOPIC, INPUT_TOPIC, AutoOffsetReset.EARLIEST, new WallclockTimestampExtractor, 30000)
      val transformerConfig = TransformerConfiguration(encoder = new PeriodReplacementEncoder, enableMetricPointServiceLevelGeneration = true, List())
      val streams = new Streams(kafkaConfig, transformerConfig)
      val factory = new StreamsFactory(streams, kafkaConfig.streamsConfig, kafkaConfig.consumeTopic)
      val streamsRunner = new StreamsRunner(factory, new StateChangeListener(new HealthStatusController))


      When("spans with duration and error=false are produced in 'input' topic, and kafka-streams topology is started")
      produceSpansAsync(10.millis, spans)
      streamsRunner.start()

      Then("we should write transformed metricPoints to the 'output' topic")
      val metricDataList: List[MetricData] = spans.flatMap(span => generateMetricDataList(span, MetricDataTransformer.allTransformers, new PeriodReplacementEncoder)) // directly call transformers to get metricPoints
      metricDataList.size shouldBe (spans.size * MetricDataTransformer.allTransformers.size * 2) // two times because of service only metric points

      val records: List[KeyValue[String, MetricData]] =
        IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived[String, MetricData](RESULT_CONSUMER_CONFIG, OUTPUT_TOPIC, metricDataList.size, 15000).asScala.toList // get metricPoints from Kafka's output topic
      records.map(record => {
        record.value.getMetricDefinition.getTags.getKv.get(MetricDefinition.MTYPE) shouldEqual METRIC_TYPE
      })

      Then("same metricPoints should be created as that from transformers")

      val metricDataSetTransformer: Set[MetricData] = metricDataList.toSet
      val metricDataSetKafka: Set[MetricData] = records.map(metricDataKv => metricDataKv.value).toSet

      val diffSetMetricPoint: Set[MetricData] = metricDataSetTransformer.diff(metricDataSetKafka)

      metricDataList.size shouldEqual records.size
      diffSetMetricPoint.isEmpty shouldEqual true

      Then("same keys / partition should be created as that from transformers")
      val keySetTransformer: Set[String] = metricDataList.map(metricData => MetricDefinitionKeyGenerator.generateKey(metricData.getMetricDefinition)).toSet
      val keySetKafka: Set[String] = records.map(metricDataKv => metricDataKv.key).toSet

      val diffSetKey: Set[String] = keySetTransformer.diff(keySetKafka)

      keySetTransformer.size shouldEqual keySetKafka.size
      diffSetKey.isEmpty shouldEqual true

      Then("no other intermediate partitions are created after as a result of topology")
      val adminClient: AdminClient = AdminClient.create(STREAMS_CONFIG)
      val topicNames: Iterable[String] = adminClient.listTopics.listings().get().asScala
        .map(topicListing => topicListing.name)

      topicNames.size shouldEqual 2
      topicNames.toSet.contains(INPUT_TOPIC) shouldEqual true
      topicNames.toSet.contains(OUTPUT_TOPIC) shouldEqual true
    }
  }

  private def generateSpans(traceId: String, spanId: String, duration: Int, errorFlag: Boolean, spanIntervalInMs: Long, spanCount: Int): List[Span] = {

    var currentTime = System.currentTimeMillis()
    for (i <- 1 to spanCount) yield {
      currentTime = currentTime + i * spanIntervalInMs

      val span = Span.newBuilder()
        .setTraceId(traceId)
        .setParentSpanId(UUID.randomUUID().toString)
        .setSpanId(spanId)
        .setOperationName("some-op")
        .setStartTime(currentTime)
        .setDuration(duration)
        .setServiceName("some-service")
        .addTags(com.expedia.open.tracing.Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVStr("some-error"))
        .build()
      span
    }
  }.toList
}

