/*
 *  Copyright 2017 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */

package com.expedia.www.haystack.trace.reader.unit.readers.transformers

import com.expedia.open.tracing.{Log, Span, Tag}
import com.expedia.www.haystack.trace.reader.readers.transformers.PartialSpanTransformer
import com.expedia.www.haystack.trace.reader.readers.utils.TagExtractors._
import com.expedia.www.haystack.trace.reader.readers.utils.{AuxiliaryTags, MutableSpanForest}
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec
import com.expedia.www.haystack.trace.reader.unit.readers.builders.ValidTraceBuilder

import scala.collection.JavaConverters._

class PartialSpanTransformerSpec extends BaseUnitTestSpec with ValidTraceBuilder {

  private def createSpansWithClientAndServer(timestamp: Long) = {
    val traceId = "traceId"
    val partialSpanId = "partialSpanId"
    val parentSpanId = "parentSpanId"
    val tag = Tag.newBuilder().setKey("tag").setVBool(true).build()

    val partialClientSpan = Span.newBuilder()
      .setSpanId(partialSpanId)
      .setParentSpanId(parentSpanId)
      .setTraceId(traceId)
      .setServiceName("clientService")
      .setStartTime(timestamp)
      .setDuration(1000)
      .addTags(tag)
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("cr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("cs").build())
        .build())
      .build()

    val partialServerSpan = Span.newBuilder()
      .setSpanId(partialSpanId)
      .setParentSpanId(parentSpanId)
      .setTraceId(traceId)
      .setServiceName("serverService")
      .setStartTime(timestamp + 20)
      .setDuration(980)
      .addTags(tag)
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("sr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("ss").build())
        .build())
      .build()

    List(partialServerSpan, partialClientSpan)
  }

  private def createMultiplePartialSpans(timestamp: Long) = {
    val traceId = "traceId"
    val partialSpanId = "partialSpanId"
    val parentSpanId = "parentSpanId"
    val tag = Tag.newBuilder().setKey("tag").setVBool(true).build()

    val partialClientSpan = Span.newBuilder()
      .setSpanId(partialSpanId)
      .setParentSpanId(parentSpanId)
      .setTraceId(traceId)
      .setServiceName("clientService")
      .setStartTime(timestamp)
      .setDuration(1000)
      .addTags(tag)
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("cr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("cs").build())
        .build())
      .build()

    val firstPartialServerSpan = Span.newBuilder()
      .setSpanId(partialSpanId)
      .setParentSpanId(parentSpanId)
      .setTraceId(traceId)
      .setServiceName("serverService")
      .setStartTime(timestamp + 20)
      .setDuration(960)
      .addTags(tag)
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("sr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("ss").build())
        .build())
      .build()

    val secondPartialServerSpan = Span.newBuilder()
      .setSpanId(partialSpanId)
      .setParentSpanId(parentSpanId)
      .setTraceId(traceId)
      .setServiceName("serverService")
      .setStartTime(timestamp +  980)
      .setDuration(10)
      .addTags(tag)
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("sr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("ss").build())
        .build())
      .build()

    List(partialClientSpan, secondPartialServerSpan, firstPartialServerSpan)
  }

  private def createNonPartialSpans(timestamp: Long) = {
    val traceId = "traceId"
    val tag = Tag.newBuilder().setKey("tag").setVBool(true).build()

    val span1 = Span.newBuilder()
      .setSpanId("span1")
      .setParentSpanId("x")
      .setTraceId(traceId)
      .setServiceName("span1Service")
      .setStartTime(timestamp)
      .setDuration(1000)
      .addTags(tag)
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("cr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("cs").build())
        .build())
      .build()

    val span2 = Span.newBuilder()
      .setSpanId("span2")
      .setParentSpanId("x")
      .setTraceId(traceId)
      .setServiceName("span2Service")
      .setStartTime(timestamp + 20)
      .setDuration(980)
      .addTags(tag)
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("sr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("ss").build())
        .build())
      .build()

    val span3 = Span.newBuilder()
      .setSpanId("span3")
      .setParentSpanId("x")
      .setTraceId(traceId)
      .setServiceName("span3Service")
      .setStartTime(timestamp +  980)
      .setDuration(10)
      .addTags(tag)
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("sr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(System.currentTimeMillis)
        .addFields(Tag.newBuilder().setKey("event").setVStr("ss").build())
        .build())
      .build()

    List(span1, span2, span3)
  }

  describe("PartialSpanTransformer") {
    it("should merge two partial spans with right event sequencing") {
      Given("trace with partial spans")
      val timestamp = 150000000000l
      val spans = createSpansWithClientAndServer(timestamp)

      When("invoking transform")
      val mergedSpans = new PartialSpanTransformer().transform(MutableSpanForest(spans)).getUnderlyingSpans

      Then("return partial spans merged with server span being primary")
      mergedSpans.length should be(1)
      mergedSpans.head.getStartTime should be(timestamp + 20)
      mergedSpans.head.getTagsCount should be(17)
      mergedSpans.head.getLogsCount should be(4)
      mergedSpans.head.getServiceName should be("serverService")
    }

    it("should merge multiple partial spans with first server span as primary") {
      Given("trace with multiple partial spans")
      val timestamp = 150000000000l
      val spans = createMultiplePartialSpans(timestamp)

      When("invoking transform")
      val mergedSpans = new PartialSpanTransformer().transform(MutableSpanForest(spans)).getUnderlyingSpans

      Then("return partial spans merged with first server span as primary")
      mergedSpans.length should be(1)
      mergedSpans.head.getStartTime should be(timestamp + 20)
      mergedSpans.head.getTagsCount should be(19)
      mergedSpans.head.getLogsCount should be(6)
      mergedSpans.head.getServiceName should be("serverService")
    }

    it("should not merge if there are no partial spans to merge") {
      Given("trace without partial spans")
      val timestamp = 150000000000l
      val spans = createNonPartialSpans(timestamp)

      When("invoking transform")
      val mergedSpans = new PartialSpanTransformer().transform(MutableSpanForest(spans)).getUnderlyingSpans

      Then("return partial spans merged")
      mergedSpans.length should be(3)
    }

    it("should add auxiliary tags") {
      Given("trace with partial spans")
      val spans = buildMultiServiceTrace().getChildSpansList.asScala

      When("invoking transform")
      val mergedSpans = new PartialSpanTransformer().transform(MutableSpanForest(spans)).getUnderlyingSpans

      Then("return partial spans merged with auxiliary tags")
      mergedSpans.size should be(6)
      val bSpan = getSpanById(mergedSpans, "b")
      bSpan.getStartTime should be(startTimestamp + 20)
      bSpan.getServiceName should be("x")

      extractTagLongValue(bSpan, AuxiliaryTags.NETWORK_DELTA) should be(40)
      extractTagStringValue(bSpan, AuxiliaryTags.CLIENT_SERVICE_NAME) should be("w")
      extractTagStringValue(bSpan, AuxiliaryTags.SERVER_SERVICE_NAME) should be("x")
    }
  }
}
