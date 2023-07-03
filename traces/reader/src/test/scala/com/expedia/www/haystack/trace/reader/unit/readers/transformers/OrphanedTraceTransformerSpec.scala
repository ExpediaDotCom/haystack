package com.expedia.www.haystack.trace.reader.unit.readers.transformers

import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.trace.commons.utils.SpanMarkers
import com.expedia.www.haystack.trace.reader.readers.transformers.{OrphanedTraceTransformer, OrphanedTraceTransformerConstants}
import com.expedia.www.haystack.trace.reader.readers.utils.MutableSpanForest
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec

class OrphanedTraceTransformerSpec extends BaseUnitTestSpec {
  describe("OrphanedTraceTransformerTest") {
    it("should return full list of spans if there is a root span already") {
      val span_1 = Span.newBuilder().setTraceId("traceId").setSpanId("traceId").setServiceName("another-service").build()
      val span_2 = Span.newBuilder().setTraceId("traceId").setSpanId("span_2").setParentSpanId(span_1.getSpanId).setServiceName("test-service").build()
      val span_3 = Span.newBuilder().setTraceId("traceId").setSpanId("span_3").setParentSpanId(span_1.getSpanId).setServiceName("another-service").build()

      val transformer = new OrphanedTraceTransformer()
      val spanForest = MutableSpanForest(Seq(span_1, span_2, span_3))
      val spans = transformer.transform(spanForest).getUnderlyingSpans
      spans.size shouldBe 3
      spans should contain(span_1)
      spans should contain(span_2)
      spans should contain(span_3)
    }

    it("should return the full list of spans plus a generated root span if there is no root span already") {
      val span_1 = Span.newBuilder().setTraceId("traceId").setOperationName(SpanMarkers.AUTOGEN_OPERATION_NAME).setServiceName("test-service")
        .setSpanId("traceId").setStartTime(10000).setDuration(10100)
        .addTags(Tag.newBuilder().setKey(SpanMarkers.AUTOGEN_REASON_TAG).setVStr(OrphanedTraceTransformerConstants.AUTO_GEN_REASON))
        .addTags(Tag.newBuilder().setKey(SpanMarkers.AUTOGEN_SPAN_ID_TAG).setVStr("traceId"))
        .addTags(Tag.newBuilder().setKey(SpanMarkers.AUTOGEN_FLAG_TAG).setVBool(true).setType(Tag.TagType.BOOL)).build()
      val span_2 = Span.newBuilder().setTraceId("traceId").setSpanId("span_2").setParentSpanId(span_1.getSpanId).setStartTime(10000).setDuration(10).setServiceName("test-service").build()
      val span_3 = Span.newBuilder().setTraceId("traceId").setSpanId("span_3").setParentSpanId(span_1.getSpanId).setStartTime(20000).setDuration(100).setServiceName("another-service").build()

      val transformer = new OrphanedTraceTransformer()
      val spanForest = MutableSpanForest(Seq(span_2, span_3))
      val spans = transformer.transform(spanForest).getUnderlyingSpans
      spans.size shouldBe 3
      spans should contain(span_2)
      spans should contain(span_3)
      spans should contain(span_1)
    }

    it("should fail if there are multiple different orphaned parent ids") {
      val span_1 = Span.newBuilder().setTraceId("traceId").setSpanId("traceId").setServiceName("another-service").build()
      val span_2 = Span.newBuilder().setTraceId("traceId").setSpanId("span_2").setParentSpanId(span_1.getSpanId).setServiceName("test-service").build()
      val span_3 = Span.newBuilder().setTraceId("traceId").setSpanId("span_3").setParentSpanId(span_1.getSpanId).setServiceName("another-service").build()
      val span_4 = Span.newBuilder().setTraceId("traceId").setSpanId("span_4").setParentSpanId(span_1.getSpanId).setServiceName("another-service").build()
      val span_5 = Span.newBuilder().setTraceId("traceId").setSpanId("span_5").setParentSpanId(span_4.getSpanId).setServiceName("another-service").build()

      val transformer = new OrphanedTraceTransformer()
      val spanForest = MutableSpanForest(Seq(span_2, span_3, span_5))
      val spans = transformer.transform(spanForest).getUnderlyingSpans
      spans.size shouldBe 0
    }

    it("should fail if there is a missing span in between the root span and orphaned span") {
      val span_1 = Span.newBuilder().setTraceId("traceId").setSpanId("traceId").setServiceName("another-service").build()
      val span_4 = Span.newBuilder().setTraceId("traceId").setSpanId("span_4").setParentSpanId(span_1.getSpanId).setServiceName("another-service").build()
      val span_5 = Span.newBuilder().setTraceId("traceId").setSpanId("span_5").setParentSpanId(span_4.getSpanId).setServiceName("another-service").build()

      val transformer = new OrphanedTraceTransformer()
      val spanForest = MutableSpanForest(Seq(span_5))
      val spans = transformer.transform(spanForest).getUnderlyingSpans
      spans.size shouldBe 0
    }
  }
}
