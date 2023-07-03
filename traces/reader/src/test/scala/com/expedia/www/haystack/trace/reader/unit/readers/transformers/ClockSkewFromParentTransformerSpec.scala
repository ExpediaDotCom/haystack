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

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.trace.commons.utils.SpanUtils
import com.expedia.www.haystack.trace.reader.readers.transformers.ClockSkewFromParentTransformer
import com.expedia.www.haystack.trace.reader.readers.utils.{MutableSpanForest, SpanTree}
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec

class ClockSkewFromParentTransformerSpec extends BaseUnitTestSpec {
  describe("ClockSkewFromParentTransformerSpec") {
    it("should not make any adjustments if parent/child spans are properly aligned") {
      val span_1 = createSpan("", "span_1", 100L, 200L)
      val span_2 = createSpan(span_1.getSpanId, "span_2", 125L, 175L)

      val transformer = new ClockSkewFromParentTransformer()
      val spans = transformer.transform(MutableSpanForest(List(span_1, span_2)))
      validateSpans(spans, 2)
    }

    it("should shift child span's startTime if the endTime exceeds the parent span") {
      val span_1 = createSpan("", "span_1", 100L, 200L)
      val span_2 = createSpan(span_1.getSpanId, "span_2", 175L, 225L)

      val transformer = new ClockSkewFromParentTransformer()
      val spans = transformer.transform(MutableSpanForest(List(span_1, span_2)))
      validateSpans(spans, 2)
    }

    it("should shift child span's startTime if the startTime precedes the parent span") {
      val span_1 = createSpan("", "span_1", 100L, 200L)
      val span_2 = createSpan(span_1.getSpanId, "span_2", 75L, 125L)

      val transformer = new ClockSkewFromParentTransformer()
      val spans = transformer.transform(MutableSpanForest(List(span_1, span_2)))
      validateSpans(spans, 2)
    }

    it("should shift both the startTime and the endTime if the child span is completely outside of the parent spans timeframe") {
      val span_1 = createSpan("", "span_1", 100L, 200L)
      val span_2 = createSpan(span_1.getSpanId, "span_2", 275L, 325L)

      val transformer = new ClockSkewFromParentTransformer()
      val spans = transformer.transform(MutableSpanForest(List(span_1, span_2)))
      validateSpans(spans, 2)
    }

    it("should shift multiple children correctly") {
      val span_1 = createSpan("", "span_1", 100L, 200L)
      val span_2 = createSpan(span_1.getSpanId, "span_2", 275L, 325L)
      val span_3 = createSpan(span_2.getSpanId, "span_3", 375, 400L)

      val transformer = new ClockSkewFromParentTransformer()
      val spans = transformer.transform(MutableSpanForest(List(span_1, span_2, span_3)))
      validateSpans(spans, 3)
    }

    it("should handle a single span with no shift") {
      val span_1 = createSpan("", "span_1", 100L, 200L)

      val transformer = new ClockSkewFromParentTransformer()
      val spans = transformer.transform(MutableSpanForest(List(span_1)))
      spans.getUnderlyingSpans.size shouldBe 1
      spans.getUnderlyingSpans.head.getStartTime shouldBe 100L
      spans.getUnderlyingSpans.head.getDuration shouldBe 100L
    }
  }

  def validateSpans(spans: MutableSpanForest, size: Int): Unit = {
    spans.getUnderlyingSpans.size shouldBe size
    spans.getAllTrees.foreach(spanTree => validateSpanTree(spanTree))
  }

  def validateSpanTree(spanTree: SpanTree): Unit = {
    spanTree.children.foreach(child => {
      spanTree.span.getStartTime should be <= child.span.getStartTime
      SpanUtils.getEndTime(child.span) should be <= SpanUtils.getEndTime(spanTree.span)
    })
    spanTree.children.foreach(child => validateSpanTree(child))
  }

  def createSpan(parentId: String, spanId: String, startTime: Long, endTime: Long): Span = {
    Span.newBuilder().setTraceId("traceId").setParentSpanId(parentId).setSpanId(spanId).setStartTime(startTime).setDuration(endTime - startTime).setServiceName("another-service").build()
  }
}
