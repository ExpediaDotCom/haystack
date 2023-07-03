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

package com.expedia.www.haystack.kinesis.span.collector.integration.tests

import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.kinesis.span.collector.config.ProjectConfiguration
import com.expedia.www.haystack.kinesis.span.collector.integration._

import scala.concurrent.duration._

class KinesisSpanCollectorSpec extends IntegrationTestSpec {

  private val StartTimeMicros = System.currentTimeMillis() * 1000
  private val DurationMicros = 42

  "Kinesis span collector" should {

    // this test is primarily to work around issue with Kafka docker image
    // it fails for first put for some reasons
    "connect with kinesis and kafka" in {

      Given("a valid span")
      val spanBytes = Span.newBuilder().setTraceId("traceid").setSpanId("span-id-1").build().toByteArray

      When("the span is sent to kinesis")
      produceRecordsToKinesis(List(spanBytes, spanBytes))

      Then("it should be pushed to kafka")
      readRecordsFromKafka(0, 1.second).headOption
    }

    "read valid spans from kinesis and store individual spans in kafka" in {

      Given("valid spans")
      val span_1 = Span.newBuilder().setTraceId("trace-id-1").setSpanId("span-id-1").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros).build().toByteArray
      val span_2 = Span.newBuilder().setTraceId("trace-id-1").setSpanId("span-id-2").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros).build().toByteArray
      val span_3 = Span.newBuilder().setTraceId("trace-id-2").setSpanId("span-id-3").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros).build().toByteArray
      val span_4 = Span.newBuilder().setTraceId("trace-id-2").setSpanId("span-id-4").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros).build().toByteArray

      When("the span is sent to kinesis")
      produceRecordsToKinesis(List(span_1, span_2, span_3, span_4))

      Then("it should be pushed to kafka with partition key as its trace id")
      val records = readRecordsFromKafka(4, 5.seconds)
      val externalrecords = readRecordsFromExternalKafka(0, 10.seconds)
      externalrecords.size shouldEqual 0
      records.size shouldEqual 4

      val spans = records.map(Span.parseFrom)
      spans.map(_.getTraceId).toSet should contain allOf("trace-id-1", "trace-id-2")
      spans.map(_.getSpanId) should contain allOf("span-id-1", "span-id-2", "span-id-3", "span-id-4")
    }

    "read valid spans from kinesis and store individual spans in kafka and external kafka" in {

      Given("valid spans")
      val tags: List[Tag] = List(
        Tag.newBuilder().setKey("X-HAYSTACK-SPAN-OWNER").setVStr("OWNER1").build(),
        Tag.newBuilder().setKey("X-HAYSTACK-SPAN-SENDER").setVStr("SENDER1").build()
      )
      val span_1 = Span.newBuilder().setTraceId("trace-id-1").setSpanId("span-id-1").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros)
        .addTags(tags(0)).addTags(tags(1)).build().toByteArray
      val span_2 = Span.newBuilder().setTraceId("trace-id-1").setSpanId("span-id-2").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros)
        .addTags(tags(0)).addTags(tags(1)).build().toByteArray
      val span_3 = Span.newBuilder().setTraceId("trace-id-2").setSpanId("span-id-3").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros)
        .addTags(tags(0)).addTags(tags(1)).build().toByteArray
      val span_4 = Span.newBuilder().setTraceId("trace-id-2").setSpanId("span-id-4").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros)
        .addTags(tags(0)).addTags(tags(1)).build().toByteArray

      When("the span is sent to kinesis")
      produceRecordsToKinesis(List(span_1, span_2, span_3, span_4))

      Then("it should be pushed to default kafka and external kafka with partition key as its trace id")
      val records = readRecordsFromKafka(4, 5.seconds)
      val numConsumers = ProjectConfiguration.externalKafkaConfig().size
      val externalrecords = readRecordsFromExternalKafka(4 * numConsumers, (10 * numConsumers).seconds)
      externalrecords.size should equal(4)
      records.size should equal(4)
      val spans = records.map(Span.parseFrom)
      val externalSpans = externalrecords.map(Span.parseFrom)
      numConsumers should equal(1)
      spans.map(_.getTraceId).toSet should contain allOf("trace-id-1", "trace-id-2")
      externalSpans.map(_.getTraceId).toSet should contain allOf("trace-id-1", "trace-id-2")
      spans.map(_.getSpanId) should contain allOf("span-id-1", "span-id-2", "span-id-3", "span-id-4")
      externalSpans.map(_.getSpanId) should contain allOf("span-id-1", "span-id-2", "span-id-3", "span-id-4")
    }

    "load appropriate span decorator plugin using configuration provided " in {

      Given("Jar file for SAMPLE_SPAN_DECORATOR plugin in plugins/decorators directory")
      val span_1 = Span.newBuilder().setTraceId("trace-id-1").setSpanId("span-id-1").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros).build().toByteArray

      When("the app is initialised")
      produceRecordsToKinesis(List(span_1))

      Then("the appropriate span decorator plugin should be loaded using spi")
      val records = readRecordsFromKafka(1, 5.seconds)
      records should not be empty

      val spans = records.map(Span.parseFrom)
      spans.map(_.getTraceId).toSet should contain("trace-id-1")
      spans.map(_.getSpanId) should contain("span-id-1")
      spans(0).getTagsList should contain(Tag.newBuilder().setKey("X-HAYSTACK-PLUGIN-SPAN-DECORATOR").setVStr("SAMPLE-TAG").build())
    }
  }
}
