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

import com.expedia.open.tracing.{Log, Span, Tag}
import com.expedia.www.haystack.trace.commons.utils.SpanMarkers
import com.expedia.www.haystack.trace.reader.readers.transformers.ClientServerEventLogTransformer
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec

import scala.collection.JavaConverters._

class ClientServerEventLogTransformerSpec extends BaseUnitTestSpec {
  describe("client server event log transformer") {
    it("should add event logs in the span using span.kind") {
      val span_1 = Span.newBuilder()
        .setTraceId("trace-id-1")
        .setDuration(100)
        .setStartTime(10000)
        .addTags(Tag.newBuilder().setType(Tag.TagType.STRING).setKey(SpanMarkers.SPAN_KIND_TAG_KEY).setVStr("client"))
        .build()
      val span_2 = Span.newBuilder()
        .setTraceId("trace-id-2")
        .setDuration(200)
        .setStartTime(20000)
        .addTags(Tag.newBuilder().setType(Tag.TagType.STRING).setKey(SpanMarkers.SPAN_KIND_TAG_KEY).setVStr("server"))
        .build()
      val transformer = new ClientServerEventLogTransformer
      val transformedSpans = transformer.transform(Seq(span_1, span_2))
      transformedSpans.length shouldBe 2

      val headSpan = transformedSpans.head
      headSpan.getTraceId shouldBe "trace-id-1"
      headSpan.getDuration shouldBe 100l
      headSpan.getStartTime shouldBe 10000l
      headSpan.getTagsList.asScala.find(tag => tag.getKey == SpanMarkers.SPAN_KIND_TAG_KEY).get.getVStr shouldBe "client"
      headSpan.getLogsList.size() shouldBe 2
      headSpan.getLogs(0).getTimestamp shouldBe 10000l
      headSpan.getLogs(1).getTimestamp shouldBe 10100l
      headSpan.getLogs(0).getFieldsList.asScala.count(tag => tag.getKey == SpanMarkers.LOG_EVENT_TAG_KEY && tag.getVStr == SpanMarkers.CLIENT_SEND_EVENT) shouldBe 1
      headSpan.getLogs(1).getFieldsList.asScala.count(tag => tag.getKey == SpanMarkers.LOG_EVENT_TAG_KEY && tag.getVStr == SpanMarkers.CLIENT_RECV_EVENT) shouldBe 1

      val lastSpan = transformedSpans(1)
      lastSpan.getTraceId shouldBe "trace-id-2"
      lastSpan.getDuration shouldBe 200
      lastSpan.getStartTime shouldBe 20000
      lastSpan.getTagsList.asScala.find(tag => tag.getKey == SpanMarkers.SPAN_KIND_TAG_KEY).get.getVStr shouldBe "server"
      lastSpan.getLogsList.size() shouldBe 2
      lastSpan.getLogs(0).getTimestamp shouldBe 20000
      lastSpan.getLogs(1).getTimestamp shouldBe 20200l
      lastSpan.getLogs(0).getFieldsList.asScala.count(tag => tag.getKey == SpanMarkers.LOG_EVENT_TAG_KEY && tag.getVStr == SpanMarkers.SERVER_RECV_EVENT) shouldBe 1
      lastSpan.getLogs(1).getFieldsList.asScala.count(tag => tag.getKey == SpanMarkers.LOG_EVENT_TAG_KEY && tag.getVStr == SpanMarkers.SERVER_SEND_EVENT) shouldBe 1
    }

    it("should not add anything if span.kind is absent") {
      val span = Span.newBuilder()
        .setTraceId("trace-id-1")
        .setDuration(100)
        .setStartTime(10000)
        .build()
      val transformedSpans = new ClientServerEventLogTransformer().transform(Seq(span))
      transformedSpans.size shouldBe 1
      transformedSpans.head shouldEqual span
    }

    it("should not add log tags if they are already present") {
      val span = Span.newBuilder()
        .setTraceId("trace-id-1")
        .setDuration(100)
        .setStartTime(10000)
        .addTags(Tag.newBuilder().setType(Tag.TagType.STRING).setKey(SpanMarkers.SPAN_KIND_TAG_KEY).setVStr("client"))
        .addLogs(Log.newBuilder().setTimestamp(10000).addFields(Tag.newBuilder().setKey("event").setVStr("cr")))
        .addLogs(Log.newBuilder().setTimestamp(10100).addFields(Tag.newBuilder().setKey("event").setVStr("cs")))
        .build()
      val transformedSpans = new ClientServerEventLogTransformer().transform(Seq(span))
      transformedSpans.size shouldBe 1
      transformedSpans.head shouldEqual span
    }
  }
}
