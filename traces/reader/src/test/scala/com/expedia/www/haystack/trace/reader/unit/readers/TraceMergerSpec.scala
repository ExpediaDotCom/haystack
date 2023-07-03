/*
 *  Copyright 2019 Expedia, Inc.
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

package com.expedia.www.haystack.trace.reader.unit.readers

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.api.Trace
import com.expedia.www.haystack.trace.reader.readers.utils.TraceMerger
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec

class TraceMergerSpec extends BaseUnitTestSpec {
  describe("Trace Merger") {
    it("should merge the traces by traceId") {
      val trace_1 = buildTrace("t1", "s1", "svc1", "op1")
      val trace_2 = buildTrace("t2", "s2", "svc2", "op1")
      val trace_3 = buildTrace("t1", "s3", "svc3", "op1")
      val trace_4 = buildTrace("t2", "s4", "svc4", "op1")
      val trace_5 = buildTrace("t3", "s5", "svc5", "op1")

      val mergedTraces = TraceMerger.merge(Seq(trace_1, trace_2, trace_3, trace_4, trace_5))
      mergedTraces.size shouldBe 3
      val t1 = mergedTraces.find(_.getTraceId == "t1").head
      t1.getChildSpansCount shouldBe 2
      t1.getChildSpans(0).getTraceId shouldBe "t1"
      t1.getChildSpans(0).getSpanId shouldBe "s1"
      t1.getChildSpans(1).getTraceId shouldBe "t1"
      t1.getChildSpans(1).getSpanId shouldBe "s3"
      t1.getChildSpans(0).getServiceName shouldBe "svc1"
      t1.getChildSpans(1).getServiceName shouldBe "svc3"

      val t2 = mergedTraces.find(_.getTraceId == "t2").head
      t2.getChildSpansCount shouldBe 2
      t2.getChildSpans(0).getSpanId shouldBe "s2"
      t2.getChildSpans(1).getSpanId shouldBe "s4"

      mergedTraces.find(_.getTraceId == "t3").head.getChildSpansCount shouldBe 1
    }
  }

  private def buildTrace(traceId: String, spanId: String, serviceName: String, operationName: String): Trace = {
    Trace.newBuilder().setTraceId(traceId).addChildSpans(
      Span.newBuilder()
        .setSpanId(spanId)
        .setTraceId(traceId)
        .setServiceName(serviceName)
        .setOperationName(operationName)).build()
  }
}
