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

package com.expedia.www.haystack.trace.indexer.unit

import java.util.concurrent.TimeUnit

import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.open.tracing.{Log, Span, Tag}
import com.expedia.www.haystack.trace.commons.config.entities.{IndexFieldType, WhiteListIndexFields, WhitelistIndexField, WhitelistIndexFieldConfiguration}
import com.expedia.www.haystack.trace.indexer.writers.es.IndexDocumentGenerator
import com.google.protobuf.ByteString
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.{FunSpec, Matchers}

class SpanIndexDocumentGeneratorSpec extends FunSpec with Matchers {
  protected implicit val formats: Formats = DefaultFormats + new EnumNameSerializer(IndexFieldType)

  private val TRACE_ID = "trace_id"
  private val START_TIME_1 = 1529042838469123l
  private val START_TIME_2 = 1529042848469000l

  private val LONG_DURATION = TimeUnit.SECONDS.toMicros(25) + TimeUnit.MICROSECONDS.toMicros(500)

  describe("Span to IndexDocument Generator") {
    it ("should extract serviceName, operationName, duration and create json document for indexing") {
      val generator = new IndexDocumentGenerator(WhitelistIndexFieldConfiguration())

      val span_1 = Span.newBuilder().setTraceId(TRACE_ID)
        .setSpanId("span-1")
        .setServiceName("service1")
        .setOperationName("op1")
        .setStartTime(START_TIME_1)
        .setDuration(610000L)
        .build()
      val span_2 = Span.newBuilder().setTraceId(TRACE_ID)
        .setSpanId("span-2")
        .setServiceName("service1")
        .setOperationName("op1")
        .setStartTime(START_TIME_1)
        .setDuration(500000L)
        .build()
      val span_3 = Span.newBuilder().setTraceId(TRACE_ID)
        .setSpanId("span-3")
        .setServiceName("service2")
        .setDuration(LONG_DURATION)
        .setStartTime(START_TIME_2)
        .setOperationName("op3").build()

      val spanBuffer = SpanBuffer.newBuilder().addChildSpans(span_1).addChildSpans(span_2).addChildSpans(span_3).setTraceId(TRACE_ID).build()
      val doc = generator.createIndexDocument(TRACE_ID, spanBuffer).get
      doc.json shouldBe "{\"traceid\":\"trace_id\",\"rootduration\":0,\"starttime\":1529042838000000,\"spans\":[{\"servicename\":\"service1\",\"starttime\":[1529042838000000],\"duration\":[500000,610000],\"operationname\":\"op1\"},{\"servicename\":\"service2\",\"starttime\":[1529042848000000],\"duration\":[25000000],\"operationname\":\"op3\"}]}"
    }

    it ("should not create an index document if service name is absent") {
      val generator = new IndexDocumentGenerator(WhitelistIndexFieldConfiguration())

      val span_1 = Span.newBuilder().setTraceId(TRACE_ID)
        .setOperationName("op1")
        .build()
      val span_2 = Span.newBuilder().setTraceId(TRACE_ID)
        .setDuration(1000L)
        .setOperationName("op2").build()

      val spanBuffer = SpanBuffer.newBuilder().addChildSpans(span_1).addChildSpans(span_2).setTraceId(TRACE_ID).build()
      val doc = generator.createIndexDocument(TRACE_ID, spanBuffer)
      doc shouldBe None
    }

    it ("should extract tags along with serviceName, operationName and duration and create json document for indexing") {
      val indexableTags = List(
        WhitelistIndexField(name = "role", `type` = IndexFieldType.string),
        WhitelistIndexField(name = "errorCode", `type` = IndexFieldType.long))

      val whitelistConfig = WhitelistIndexFieldConfiguration()
      whitelistConfig.onReload(Serialization.write(WhiteListIndexFields(indexableTags)))
      val generator = new IndexDocumentGenerator(whitelistConfig)

      val tag_1 = Tag.newBuilder().setKey("role").setType(Tag.TagType.STRING).setVStr("haystack").build()
      val tag_2  = Tag.newBuilder().setKey("errorCode").setType(Tag.TagType.LONG).setVLong(3).build()

      val span_1 = Span.newBuilder().setTraceId(TRACE_ID)
        .setServiceName("service1")
        .setSpanId("span-1")
        .setOperationName("op1")
        .setDuration(100L)
        .setStartTime(START_TIME_1)
        .addTags(tag_1)
        .build()
      val span_2 = Span.newBuilder().setTraceId(TRACE_ID)
        .setServiceName("service1")
        .setSpanId("span-2")
        .setOperationName("op2")
        .setDuration(200L)
        .setStartTime(START_TIME_2)
        .addTags(tag_2)
        .addTags(tag_1)
        .build()
      val span_3 = Span.newBuilder().setTraceId(TRACE_ID)
        .setSpanId("span-3")
        .setServiceName("service2")
        .setDuration(1000L)
        .setStartTime(START_TIME_1)
        .addTags(tag_2)
        .setOperationName("op3").build()

      val spanBuffer = SpanBuffer.newBuilder().addChildSpans(span_1).addChildSpans(span_2).addChildSpans(span_3).setTraceId(TRACE_ID).build()
      val doc = generator.createIndexDocument(TRACE_ID, spanBuffer).get
      doc.json shouldBe "{\"traceid\":\"trace_id\",\"rootduration\":0,\"starttime\":1529042838000000,\"spans\":[{\"role\":[\"haystack\"],\"servicename\":\"service1\",\"starttime\":[1529042838000000],\"duration\":[100],\"operationname\":\"op1\"},{\"role\":[\"haystack\"],\"servicename\":\"service1\",\"starttime\":[1529042848000000],\"errorcode\":[3],\"duration\":[200],\"operationname\":\"op2\"},{\"servicename\":\"service2\",\"starttime\":[1529042838000000],\"errorcode\":[3],\"duration\":[1000],\"operationname\":\"op3\"}]}"
    }

    it ("should respect enabled flag of tags create right json document for indexing") {
      val indexableTags = List(
        WhitelistIndexField(name = "role", IndexFieldType.string, enabled = false),
        WhitelistIndexField(name = "errorCode", `type` = IndexFieldType.long))
      val whitelistConfig = WhitelistIndexFieldConfiguration()
      whitelistConfig.onReload(Serialization.write(WhiteListIndexFields(indexableTags)))
      val generator = new IndexDocumentGenerator(whitelistConfig)

      val tag_1 = Tag.newBuilder().setKey("role").setType(Tag.TagType.STRING).setVStr("haystack").build()
      val tag_2  = Tag.newBuilder().setKey("errorCode").setType(Tag.TagType.LONG).setVLong(3).build()

      val span_1 = Span.newBuilder().setTraceId(TRACE_ID)
        .setSpanId("span-1")
        .setServiceName("service1")
        .setOperationName("op1")
        .setDuration(100L)
        .setStartTime(START_TIME_1)
        .addTags(tag_1)
        .build()
      val span_2 = Span.newBuilder().setTraceId(TRACE_ID)
        .setSpanId("span-2")
        .setServiceName("service1")
        .setOperationName("op2")
        .setDuration(200L)
        .setStartTime(START_TIME_2)
        .addTags(tag_2)
        .build()

      val spanBuffer = SpanBuffer.newBuilder().addChildSpans(span_1).addChildSpans(span_2).setTraceId(TRACE_ID).build()
      val doc = generator.createIndexDocument(TRACE_ID, spanBuffer).get
      doc.json shouldBe "{\"traceid\":\"trace_id\",\"rootduration\":0,\"starttime\":1529042838000000,\"spans\":[{\"servicename\":\"service1\",\"starttime\":[1529042838000000],\"duration\":[100],\"operationname\":\"op1\"},{\"servicename\":\"service1\",\"starttime\":[1529042848000000],\"errorcode\":[3],\"duration\":[200],\"operationname\":\"op2\"}]}"
    }

    it ("one more test to verify the tags are indexed") {
      val indexableTags = List(
        WhitelistIndexField(name = "errorCode", `type` = IndexFieldType.long))
      val whitelistConfig = WhitelistIndexFieldConfiguration()
      whitelistConfig.onReload(Serialization.write(WhiteListIndexFields(indexableTags)))
      val generator = new IndexDocumentGenerator(whitelistConfig)

      val tag_1 = Tag.newBuilder().setKey("errorCode").setType(Tag.TagType.LONG).setVLong(5).build()
      val tag_2  = Tag.newBuilder().setKey("errorCode").setType(Tag.TagType.LONG).setVLong(3).build()

      val span_1 = Span.newBuilder().setTraceId(TRACE_ID)
        .setServiceName("service1")
        .setSpanId("span-1")
        .setOperationName("op1")
        .setDuration(100L)
        .addTags(tag_1)
        .build()
      val span_2 = Span.newBuilder().setTraceId(TRACE_ID)
        .setServiceName("service1")
        .setSpanId("span-2")
        .setOperationName("op2")
        .setDuration(200L)
        .addTags(tag_2)
        .build()

      val spanBuffer = SpanBuffer.newBuilder().addChildSpans(span_1).addChildSpans(span_2).setTraceId(TRACE_ID).build()
      val doc = generator.createIndexDocument(TRACE_ID, spanBuffer).get
      doc.json shouldBe "{\"traceid\":\"trace_id\",\"rootduration\":0,\"starttime\":0,\"spans\":[{\"servicename\":\"service1\",\"starttime\":[0],\"errorcode\":[5],\"duration\":[100],\"operationname\":\"op1\"},{\"servicename\":\"service1\",\"starttime\":[0],\"errorcode\":[3],\"duration\":[200],\"operationname\":\"op2\"}]}"
    }

    it ("should extract unique tag values along with serviceName, operationName and duration and create json document for indexing") {
      val indexableTags = List(
        WhitelistIndexField(name = "role", `type` = IndexFieldType.string),
        WhitelistIndexField(name = "errorCode", `type` = IndexFieldType.long))
      val whitelistConfig = WhitelistIndexFieldConfiguration()
      whitelistConfig.onReload(Serialization.write(WhiteListIndexFields(indexableTags)))
      val generator = new IndexDocumentGenerator(whitelistConfig)

      val tag_1 = Tag.newBuilder().setKey("role").setType(Tag.TagType.STRING).setVStr("haystack").build()
      val tag_2  = Tag.newBuilder().setKey("errorCode").setType(Tag.TagType.LONG).setVLong(3).build()

      val span_1 = Span.newBuilder().setTraceId(TRACE_ID)
        .setServiceName("service1")
        .setSpanId("span-1")
        .setOperationName("op1")
        .setDuration(100L)
        .addTags(tag_1)
        .build()
      val span_2 = Span.newBuilder().setTraceId(TRACE_ID)
        .setServiceName("service1")
        .setSpanId("span-2")
        .setOperationName("op2")
        .setDuration(200L)
        .addTags(tag_2)
        .build()

      val spanBuffer = SpanBuffer.newBuilder().addChildSpans(span_1).addChildSpans(span_2).setTraceId(TRACE_ID).build()
      val doc = generator.createIndexDocument(TRACE_ID, spanBuffer).get
      doc.json shouldBe "{\"traceid\":\"trace_id\",\"rootduration\":0,\"starttime\":0,\"spans\":[{\"role\":[\"haystack\"],\"servicename\":\"service1\",\"starttime\":[0],\"duration\":[100],\"operationname\":\"op1\"},{\"servicename\":\"service1\",\"starttime\":[0],\"errorcode\":[3],\"duration\":[200],\"operationname\":\"op2\"}]}"
    }

    it ("should extract tags, log values along with serviceName, operationName and duration and create json document for indexing") {
      val indexableTags = List(
        WhitelistIndexField(name = "role", `type` = IndexFieldType.string),
        WhitelistIndexField(name = "errorCode", `type` = IndexFieldType.long),
        WhitelistIndexField(name = "exception", `type` = IndexFieldType.string))

      val whitelistConfig = WhitelistIndexFieldConfiguration()
      whitelistConfig.onReload(Serialization.write(WhiteListIndexFields(indexableTags)))
      val generator = new IndexDocumentGenerator(whitelistConfig)

      val tag_1 = Tag.newBuilder().setKey("role").setType(Tag.TagType.STRING).setVStr("haystack").build()
      val tag_2  = Tag.newBuilder().setKey("errorCode").setType(Tag.TagType.LONG).setVLong(3).build()
      val log_1 = Log.newBuilder()
        .addFields(Tag.newBuilder().setKey("exception").setType(Tag.TagType.STRING).setVStr("xxx-yy-zzz").build())
        .setTimestamp(100L)
      val log_2 = Log.newBuilder()
        .addFields(Tag.newBuilder().setKey("exception").setType(Tag.TagType.STRING).setVStr("aaa-bb-cccc").build())
        .setTimestamp(200L)

      val span_1 = Span.newBuilder().setTraceId(TRACE_ID)
        .setServiceName("service1")
        .setSpanId("span-1")
        .setOperationName("op1")
        .setDuration(100L)
        .setStartTime(START_TIME_1)
        .addTags(tag_1)
        .build()
      val span_2 = Span.newBuilder().setTraceId("traceId")
        .setServiceName("service1")
        .setSpanId("span-2")
        .setOperationName("op2")
        .setDuration(200L)
        .setStartTime(START_TIME_2)
        .addTags(tag_2)
        .addLogs(log_1)
        .addLogs(log_2)
        .build()

      val spanBuffer = SpanBuffer.newBuilder().addChildSpans(span_1).addChildSpans(span_2).setTraceId(TRACE_ID).build()
      val doc = generator.createIndexDocument(TRACE_ID, spanBuffer).get
      doc.json shouldBe "{\"traceid\":\"trace_id\",\"rootduration\":0,\"starttime\":1529042838000000,\"spans\":[{\"role\":[\"haystack\"],\"servicename\":\"service1\",\"starttime\":[1529042838000000],\"duration\":[100],\"operationname\":\"op1\"},{\"servicename\":\"service1\",\"starttime\":[1529042848000000],\"errorcode\":[3],\"duration\":[200],\"operationname\":\"op2\"}]}"
    }

    it("should transform the tags for all data types like bool, long, double to string type") {
      val indexableTags = List(
        WhitelistIndexField(name = "errorCode", `type` = IndexFieldType.string),
        WhitelistIndexField(name = "isErrored", `type` = IndexFieldType.string),
        WhitelistIndexField(name = "exception", `type` = IndexFieldType.string))
      val whitelistConfig = WhitelistIndexFieldConfiguration()
      whitelistConfig.onReload(Serialization.write(WhiteListIndexFields(indexableTags)))
      val generator = new IndexDocumentGenerator(whitelistConfig)

      val tag_1 = Tag.newBuilder().setKey("isErrored").setType(Tag.TagType.BOOL).setVBool(true).build()
      val tag_2  = Tag.newBuilder().setKey("errorCode").setType(Tag.TagType.LONG).setVLong(500).build()
      val log_1 = Log.newBuilder()
        .addFields(Tag.newBuilder().setKey("exception").setType(Tag.TagType.BINARY).setVBytes(ByteString.copyFromUtf8("xxx-yy-zzz")).build())
        .setTimestamp(100L)
      val span_1 = Span.newBuilder().setTraceId(TRACE_ID)
        .setServiceName("service1")
        .setSpanId("span-1")
        .setOperationName("op1")
        .setDuration(100L)
        .setStartTime(START_TIME_1)
        .addTags(tag_1)
        .addTags(tag_2)
        .addLogs(log_1)
        .build()
      val spanBuffer = SpanBuffer.newBuilder().addChildSpans(span_1).setTraceId(TRACE_ID).build()

      val doc = generator.createIndexDocument(TRACE_ID, spanBuffer)
      doc.get.json shouldEqual "{\"traceid\":\"trace_id\",\"rootduration\":0,\"starttime\":1529042838000000,\"spans\":[{\"servicename\":\"service1\",\"iserrored\":[\"true\"],\"starttime\":[1529042838000000],\"errorcode\":[\"500\"],\"duration\":[100],\"operationname\":\"op1\"}]}"
      doc.get.json.contains("iserrored") shouldBe true
    }
  }
}