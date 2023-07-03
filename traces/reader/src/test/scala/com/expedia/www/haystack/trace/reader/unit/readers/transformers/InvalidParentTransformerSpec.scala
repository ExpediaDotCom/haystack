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
import com.expedia.www.haystack.trace.reader.readers.transformers.InvalidParentTransformer
import com.expedia.www.haystack.trace.reader.readers.utils.MutableSpanForest
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec

class InvalidParentTransformerSpec extends BaseUnitTestSpec {
  describe("InvalidParentTransformer") {
    it("should mark root as parent for spans with invalid parent ids") {
      Given("trace having spans with invalid parent ids")
      val spans = List(
        Span.newBuilder()
          .setSpanId("a")
          .build(),
        Span.newBuilder()
          .setSpanId("b")
          .setParentSpanId("b")
          .build(),
        Span.newBuilder()
          .setTraceId("traceId")
          .setSpanId("c")
          .setParentSpanId("traceId")
          .build()
      )

      When("invoking transform")
      val transformedSpanTree = new InvalidParentTransformer().transform(MutableSpanForest(spans))
      val transformedSpans = transformedSpanTree.getUnderlyingSpans

      Then("mark root to be parent of spans with invalid parent id")
      transformedSpans.length should be(3)

      val aSpan = transformedSpans.find(_.getSpanId == "a")
      aSpan.get.getParentSpanId should be("")

      val bSpan = transformedSpans.find(_.getSpanId == "b")
      bSpan.get.getParentSpanId should be("a")

      val cSpan = transformedSpans.find(_.getSpanId == "c")
      cSpan.get.getParentSpanId should be("a")

      transformedSpanTree.getAllTrees.size shouldBe 1
    }

    it("should mark root as parent for spans with parent ids that are not in the trace") {
      Given("trace having spans with invalid parent ids")
      val spans = List(
        Span.newBuilder()
          .setSpanId("a")
          .build(),
        Span.newBuilder()
          .setSpanId("b")
          .setParentSpanId("x")
          .build(),
        Span.newBuilder()
          .setSpanId("c")
          .setParentSpanId("b")
          .build()
      )

      When("invoking transform")
      val transformedSpans = new InvalidParentTransformer().transform(MutableSpanForest(spans)).getUnderlyingSpans

      Then("mark root to be parent of spans with invalid parent id")
      transformedSpans.length should be(3)

      val aSpan = transformedSpans.find(_.getSpanId == "a")
      aSpan.get.getParentSpanId should be("")

      val bSpan = transformedSpans.find(_.getSpanId == "b")
      bSpan.get.getParentSpanId should be("a")

      val cSpan = transformedSpans.find(_.getSpanId == "c")
      cSpan.get.getParentSpanId should be("b")
    }
  }
}
