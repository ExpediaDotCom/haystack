/*
 *  Copyright 2018 Expedia, Inc.
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


import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.trace.commons.utils.SpanMarkers
import com.expedia.www.haystack.trace.reader.readers.transformers.ServerClientSpanMergeTransformer
import com.expedia.www.haystack.trace.reader.readers.utils.{AuxiliaryTags, MutableSpanForest}
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec
import com.expedia.www.haystack.trace.reader.unit.readers.builders.ValidTraceBuilder

import scala.collection.JavaConverters._

class ServerClientSpanMergeTransformerSpec extends BaseUnitTestSpec with ValidTraceBuilder {

  private def createProducerAndConsumerSpanKinds(): List[Span] = {
    val traceId = "traceId"

    val timestamp = System.currentTimeMillis() * 1000

    val producerSpan = Span.newBuilder()
      .setSpanId("sa")
      .setTraceId(traceId)
      .setServiceName("aSvc")
      .addTags(Tag.newBuilder().setKey("span.kind").setVStr("producer"))
      .setStartTime(timestamp + 100)
      .setDuration(1000)
      .build()

    val consumerASpan = Span.newBuilder()
      .setSpanId("sb1")
      .setParentSpanId("sa")
      .setTraceId(traceId)
      .setServiceName("bSvc")
      .addTags(Tag.newBuilder().setKey("span.kind").setVStr("consumer"))
      .setStartTime(timestamp + 400)
      .setDuration(1000)
      .build()

    val consumerBSpan = Span.newBuilder()
      .setSpanId("sb2")
      .setParentSpanId("sb1")
      .setTraceId(traceId)
      .setServiceName("bSvc")
      .addTags(Tag.newBuilder().setKey("span.kind").setVStr("producer"))
      .setStartTime(timestamp + 1000)
      .setDuration(1000)
      .build()
    List(producerSpan, consumerASpan, consumerBSpan)
  }

  private def createSpansWithClientAndServer(): List[Span] = {
    val traceId = "traceId"

    val timestamp = System.currentTimeMillis() * 1000

    val serverSpanA = Span.newBuilder()
      .setSpanId("sa")
      .setTraceId(traceId)
      .setServiceName("aSvc")
      .setStartTime(timestamp + 100)
      .setDuration(1000)
      .build()

    val clientSpanA = Span.newBuilder()
      .setSpanId("ca")
      .setParentSpanId("sa")
      .setTraceId(traceId)
      .setServiceName("aSvc")
      .setStartTime(timestamp + 100)
      .setDuration(1000)
      .build()

    val serverSpanB = Span.newBuilder()
      .setSpanId("sb")
      .setParentSpanId("ca")
      .setServiceName("bSvc")
      .setTraceId(traceId)
      .setStartTime(timestamp + 200)
      .setDuration(100)
      .build()

    val clientSpanB_1 = Span.newBuilder()
      .setSpanId("cb1")
      .setParentSpanId("sb")
      .setServiceName("bSvc")
      .setTraceId(traceId)
      .setStartTime(timestamp + 300)
      .setDuration(100)
      .build()

    val clientSpanB_2 = Span.newBuilder()
      .setSpanId("cb2")
      .setParentSpanId("sb")
      .setServiceName("bSvc")
      .setStartTime(timestamp + 400)
      .setTraceId(traceId)
      .setDuration(100)
      .build()

    val serverSpanC_1 = Span.newBuilder()
      .setSpanId("sc1")
      .setParentSpanId("cb1")
      .setServiceName("cSvc")
      .setTraceId(traceId)
      .setStartTime(timestamp + 500)
      .setDuration(100)
      .build()

    val serverSpanC_2 = Span.newBuilder()
      .setSpanId("sc2")
      .setParentSpanId("cb2")
      .setServiceName("cSvc")
      .setTraceId(traceId)
      .setStartTime(timestamp + 600)
      .setDuration(100)
      .build()

    val serverSpanC_3 = Span.newBuilder()
      .setSpanId("sc3")
      .setParentSpanId("p1")
      .setServiceName("cSvc")
      .addTags(Tag.newBuilder().setKey(SpanMarkers.SPAN_KIND_TAG_KEY).setVStr(SpanMarkers.SERVER_SPAN_KIND))
      .setTraceId(traceId)
      .setStartTime(timestamp + 600)
      .setDuration(100)
      .build()

    val serverSpanD_1 = Span.newBuilder()
      .setSpanId("sd1")
      .setParentSpanId("sc3")
      .setServiceName("dSvc")
      .addTags(Tag.newBuilder().setKey(SpanMarkers.SPAN_KIND_TAG_KEY).setVStr(SpanMarkers.SERVER_SPAN_KIND))
      .setTraceId(traceId)
      .setStartTime(timestamp + 600)
      .setDuration(100)
      .build()

    val serverSpanD_2 = Span.newBuilder()
      .setSpanId("sd2")
      .setParentSpanId("sc3")
      .setServiceName("dSvc")
      .addTags(Tag.newBuilder().setKey(SpanMarkers.SPAN_KIND_TAG_KEY).setVStr(SpanMarkers.CLIENT_SPAN_KIND))
      .setTraceId(traceId)
      .setStartTime(timestamp + 600)
      .setDuration(100)
      .build()

    val serverSpanE_1 = Span.newBuilder()
      .setSpanId("se1")
      .setParentSpanId("sd2")
      .setServiceName("eSvc")
      .addTags(Tag.newBuilder().setKey(SpanMarkers.SPAN_KIND_TAG_KEY).setVStr(SpanMarkers.SERVER_SPAN_KIND))
      .setTraceId(traceId)
      .setStartTime(timestamp + 600)
      .setDuration(100)
      .build()

    List(serverSpanA, clientSpanA, serverSpanB, clientSpanB_1, clientSpanB_2, serverSpanC_1, serverSpanC_2, serverSpanC_3, serverSpanD_1, serverSpanD_2, serverSpanE_1)
  }

  describe("ServerClientSpanMergeTransformer") {
    it("should merge the server client spans") {
      Given("a sequence of spans of a given trace")
      val spans = createSpansWithClientAndServer()

      When("invoking transform")
      val mergedSpans =
        new ServerClientSpanMergeTransformer().transform(MutableSpanForest(spans))

      val underlyingSpans = mergedSpans.getUnderlyingSpans

      Then("return partial spans merged with server span being primary")
      underlyingSpans.length should be(7)
      underlyingSpans.foreach(span => span.getTraceId shouldBe traceId)
      underlyingSpans.head.getSpanId shouldBe "sa"
      underlyingSpans.head.getParentSpanId shouldBe ""

      underlyingSpans.apply(1).getSpanId shouldBe "sb"
      underlyingSpans.apply(1).getParentSpanId shouldBe "sa"
      underlyingSpans.apply(1).getServiceName shouldBe "bSvc"
      getTag(underlyingSpans.apply(1), AuxiliaryTags.IS_MERGED_SPAN).getVBool shouldBe true
      underlyingSpans.apply(1).getLogsCount shouldBe 4

      underlyingSpans.apply(2).getSpanId shouldBe "sc1"
      underlyingSpans.apply(2).getParentSpanId shouldBe "sb"
      underlyingSpans.apply(2).getServiceName shouldBe "cSvc"
      getTag(underlyingSpans.apply(2), AuxiliaryTags.IS_MERGED_SPAN).getVBool shouldBe true
      underlyingSpans.apply(2).getLogsCount shouldBe 4

      underlyingSpans.apply(3).getSpanId shouldBe "sc2"
      underlyingSpans.apply(3).getParentSpanId shouldBe "sb"
      underlyingSpans.apply(3).getServiceName shouldBe "cSvc"
      getTag(underlyingSpans.apply(3), AuxiliaryTags.IS_MERGED_SPAN).getVBool shouldBe true
      underlyingSpans.apply(3).getLogsCount shouldBe 4

      underlyingSpans.apply(4).getSpanId shouldBe "sc3"
      getTag(underlyingSpans.apply(4), AuxiliaryTags.IS_MERGED_SPAN) shouldBe null

      underlyingSpans.apply(5).getSpanId shouldBe "sd1"
      getTag(underlyingSpans.apply(5), AuxiliaryTags.IS_MERGED_SPAN) shouldBe null

      underlyingSpans.apply(6).getSpanId shouldBe "se1"
      underlyingSpans.apply(6).getServiceName shouldBe "eSvc"
      getTag(underlyingSpans.apply(6), AuxiliaryTags.IS_MERGED_SPAN).getVBool shouldBe true
      getTag(underlyingSpans.apply(6), AuxiliaryTags.CLIENT_SERVICE_NAME).getVStr shouldBe "dSvc"
      getTag(underlyingSpans.apply(6), AuxiliaryTags.CLIENT_SPAN_ID).getVStr shouldBe "sd2"
      getTag(underlyingSpans.apply(6), AuxiliaryTags.CLIENT_OPERATION_NAME).getVStr shouldBe empty

      mergedSpans.countTrees shouldBe 2
      val spanTree = mergedSpans.getAllTrees.head
      spanTree.span shouldBe underlyingSpans.head
      spanTree.children.size shouldBe 1
      spanTree.children.head.children.size shouldBe 2
      spanTree.children.head.span shouldBe underlyingSpans.apply(1)
      spanTree.children.head.children.map(_.span) should contain allOf(underlyingSpans.apply(2), underlyingSpans.apply(3))
      spanTree.children.head.children.foreach(tree => tree.children.size shouldBe 0)
    }

    it ("should not merge producer and consumer parent-child spans") {
      Given("a sequence of spans of a given trace")
      val spans = createProducerAndConsumerSpanKinds()

      When("invoking transform")
      val mergedSpans =
        new ServerClientSpanMergeTransformer().transform(MutableSpanForest(spans))

      val underlyingSpans = mergedSpans.getUnderlyingSpans
      underlyingSpans.size shouldBe 3
      underlyingSpans.foreach(sp => getTag(sp, AuxiliaryTags.IS_MERGED_SPAN) shouldBe null)
    }
  }



  private def getTag(span: Span, tagKey: String): Tag = {
    span.getTagsList.asScala.find(tag => tag.getKey.equals(tagKey)).orNull
  }
}