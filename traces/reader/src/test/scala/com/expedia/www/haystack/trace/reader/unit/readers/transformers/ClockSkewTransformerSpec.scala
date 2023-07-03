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
import com.expedia.www.haystack.trace.reader.readers.transformers.ClockSkewTransformer
import com.expedia.www.haystack.trace.reader.readers.utils.MutableSpanForest
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec

class ClockSkewTransformerSpec extends BaseUnitTestSpec {

  private def createTraceWithoutMergedSpans(timestamp: Long) = {
    // creating a trace with this timeline structure-
    // a -> b(-50)  -> e(-100)
    //   -> c(+500)
    //   -> d(-100)

    val traceId = "traceId"

    val spanA = Span.newBuilder()
      .setSpanId("a")
      .setTraceId(traceId)
      .setStartTime(timestamp)
      .setDuration(1000)
      .build()

    val spanB = Span.newBuilder()
      .setSpanId("b")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setStartTime(spanA.getStartTime - 50)
      .setDuration(100)
      .build()

    val spanC = Span.newBuilder()
      .setSpanId("c")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setStartTime(spanA.getStartTime + 500)
      .setDuration(100)
      .build()

    val spanD = Span.newBuilder()
      .setSpanId("d")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setStartTime(spanA.getStartTime - 100)
      .setDuration(100)
      .build()

    val spanE = Span.newBuilder()
      .setSpanId("e")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setStartTime(spanB.getStartTime - 100)
      .setDuration(100)
      .build()

    List(spanA, spanB, spanC, spanD, spanE)
  }

  private def createSpansWithClientAndServer(timestamp: Long) = {
    val traceId = "traceId"
    val skewedSpanId = "spanId"
    val serviceName = "serviceNam"
    val tag = Tag.newBuilder().setKey("tag").setVBool(true).build()
    val log = Log.newBuilder().setTimestamp(System.currentTimeMillis).addFields(tag).build()

    val partialSpan = Span.newBuilder()
      .setSpanId(skewedSpanId)
      .setTraceId(traceId)
      .setServiceName(serviceName)
      .setStartTime(timestamp + 2000)
      .setDuration(1000)
      .addTags(tag)
      .addLogs(Log.newBuilder()
        .setTimestamp(timestamp)
        .addFields(Tag.newBuilder().setKey("event").setVStr("cs").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(timestamp + 1000)
        .addFields(Tag.newBuilder().setKey("event").setVStr("cr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(timestamp + 2000)
        .addFields(Tag.newBuilder().setKey("event").setVStr("sr").build())
        .build())
      .addLogs(Log.newBuilder()
        .setTimestamp(timestamp + 2000 + 400)
        .addFields(Tag.newBuilder().setKey("event").setVStr("ss").build())
        .build())
      .build()

    val aChildSpan = Span.newBuilder()
      .setSpanId("a")
      .setParentSpanId(skewedSpanId)
      .setTraceId(traceId)
      .setServiceName(serviceName)
      .setStartTime(timestamp + 2500)
      .setDuration(400)
      .addTags(tag)
      .build()

    val bChildSpan = Span.newBuilder()
      .setSpanId("b")
      .setParentSpanId(skewedSpanId)
      .setTraceId(traceId)
      .setServiceName(serviceName)
      .setStartTime(timestamp + 2700)
      .setDuration(400)
      .addTags(tag)
      .build()

    val cSpan = Span.newBuilder()
      .setSpanId("c")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("otherService")
      .setStartTime(timestamp + 100)
      .setDuration(400)
      .addTags(tag)
      .build()

    List(aChildSpan, bChildSpan, cSpan, partialSpan)
  }

  describe("ClockSkewTransformer") {
    it("should not change clock skew if there are no merged spans") {
      Given("trace with skewed spans")
      val timestamp = 150000000000l
      val spanForest = MutableSpanForest(createTraceWithoutMergedSpans(timestamp))

      When("invoking transform")
      val transformedSpans = new ClockSkewTransformer().transform(spanForest).getUnderlyingSpans

      Then("return spans without fixing skew")
      transformedSpans.length should be(5)
      transformedSpans.find(_.getSpanId == "a").get.getStartTime should be(timestamp)
      transformedSpans.find(_.getSpanId == "b").get.getStartTime should be(timestamp - 50)
      transformedSpans.find(_.getSpanId == "c").get.getStartTime should be(timestamp + 500)
      transformedSpans.find(_.getSpanId == "d").get.getStartTime should be(timestamp - 100)
      transformedSpans.find(_.getSpanId == "e").get.getStartTime should be(timestamp - 150)
    }

    it("should fix clock skew if there merged spans with skew") {
      Given("trace with skewed spans")
      val timestamp = 150000000000l
      val spanForest = MutableSpanForest(createSpansWithClientAndServer(timestamp))

      When("invoking transform")
      val transformedSpans = new ClockSkewTransformer().transform(spanForest).getUnderlyingSpans

      Then("return spans without fixing skew")
      transformedSpans.length should be(4)
      transformedSpans.find(_.getSpanId == "spanId").get.getStartTime should be(timestamp + 300)
      transformedSpans.find(_.getSpanId == "a").get.getStartTime should be(timestamp + 300 + 500)
      transformedSpans.find(_.getSpanId == "b").get.getStartTime should be(timestamp + 300 + 700)
      transformedSpans.find(_.getSpanId == "c").get.getStartTime should be(timestamp + 100)
    }
  }
}
