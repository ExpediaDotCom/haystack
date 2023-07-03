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
import com.expedia.www.haystack.trace.reader.readers.transformers.SortSpanTransformer
import com.expedia.www.haystack.trace.reader.readers.utils.MutableSpanForest
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec

class SortSpanTransformerSpec extends BaseUnitTestSpec {

  def createSpans(timestamp: Long): List[Span] = {
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
      .setStartTime(timestamp + 50)
      .setDuration(100)
      .build()

    val spanC = Span.newBuilder()
      .setSpanId("c")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setStartTime(timestamp + 100)
      .setDuration(100)
      .build()

    val spanD = Span.newBuilder()
      .setSpanId("d")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setStartTime(timestamp + 200)
      .setDuration(100)
      .build()

    val spanE = Span.newBuilder()
      .setSpanId("e")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setStartTime(timestamp + 300)
      .setDuration(100)
      .build()

    List(spanE, spanB, spanC, spanA, spanD)
  }

  describe("SortSpanTransformer") {
    it("should sort spans in natural order") {
      Given("trace with spans")
      val timestamp = 150000000000l
      val spans = createSpans(timestamp)

      When("invoking transform")
      val transformedSpans = new SortSpanTransformer().transform(MutableSpanForest(spans)).getUnderlyingSpans

      Then("return spans in sorted order")
      transformedSpans.length should be(5)
      transformedSpans.head.getSpanId should be("a")
      transformedSpans(1).getSpanId should be("b")
      transformedSpans(2).getSpanId should be("c")
      transformedSpans(3).getSpanId should be("d")
      transformedSpans(4).getSpanId should be("e")
    }
  }
}
