package com.expedia.www.haystack.trace.reader.unit.readers.transformers

import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.trace.reader.readers.transformers.InfrastructureTagTransformer
import com.expedia.www.haystack.trace.reader.readers.utils.AuxiliaryTags
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec

import scala.collection.JavaConverters._

class InfrastructureTagTransformerSpec extends BaseUnitTestSpec {

  private val infraTags = Seq(
      Tag.newBuilder().setKey(AuxiliaryTags.INFRASTRUCTURE_PROVIDER).setVStr("aws").setType(Tag.TagType.STRING).build(),
      Tag.newBuilder().setKey(AuxiliaryTags.INFRASTRUCTURE_LOCATION).setVStr("us-west-2").setType(Tag.TagType.STRING).build()
  )

  private val randomTags = Seq(Tag.newBuilder().setKey("error").setVBool(false).setType(Tag.TagType.BOOL).build())


  describe("infrastructure tag transformer") {
    it("should add missing infrastructure tags in a service spans if any of it contains so") {
      // first tag in the sequence contains infrastructure tags, following span doesn't contain from the same service doesn't contain.
      val svc1_span1 = Span.newBuilder().setTraceId("traceId").setSpanId("span_1").setServiceName("service_1").addAllTags(randomTags.asJava).addAllTags(infraTags.asJava).build()
      val svc1_span2 = Span.newBuilder().setTraceId("traceId").setSpanId("span_2").setParentSpanId("span_1").setServiceName("service_1").build()

      // none of the tags from this service contains infrastructure information
      val svc2_span_1 = Span.newBuilder().setTraceId("traceId").setSpanId("span_3").setServiceName("service_2").addAllTags(randomTags.asJava).build()
      val svc2_span_2 = Span.newBuilder().setTraceId("traceId").setSpanId("span_4").setParentSpanId("span_3").setServiceName("service_2").build()


      // first tag in the sequence doesn't contain infrastructure tags, following span from the same service contains.
      val svc3_span1 = Span.newBuilder().setTraceId("traceId").setSpanId("span_5").setServiceName("service_3").build()
      val svc3_span2 = Span.newBuilder().setTraceId("traceId").setSpanId("span_6").setParentSpanId("span_5").setServiceName("service_3").addAllTags(randomTags.asJava).addAllTags(infraTags.asJava).build()

      val transformedSpans = new InfrastructureTagTransformer()
        .transform(Seq(svc1_span1, svc1_span2, svc2_span_1, svc2_span_2, svc3_span1, svc3_span2))

      transformedSpans.size shouldBe 6
      transformedSpans.find(_.getSpanId == "span_1").get.getTagsList should contain allElementsOf infraTags
      transformedSpans.find(_.getSpanId == "span_1").get.getTagsList should contain allElementsOf randomTags

      transformedSpans.find(_.getSpanId == "span_2").get.getTagsCount shouldBe infraTags.size
      transformedSpans.find(_.getSpanId == "span_2").get.getTagsList should contain allElementsOf infraTags

      transformedSpans.find(_.getSpanId == "span_3").get.getTagsCount shouldBe 1
      transformedSpans.find(_.getSpanId == "span_3").get.getTagsList should contain allElementsOf randomTags
      transformedSpans.find(_.getSpanId == "span_4").get.getTagsCount shouldBe 0

      transformedSpans.find(_.getSpanId == "span_5").get.getTagsCount shouldBe infraTags.size
      transformedSpans.find(_.getSpanId == "span_5").get.getTagsList should contain allElementsOf infraTags
      transformedSpans.find(_.getSpanId == "span_6").get.getTagsList should contain allElementsOf infraTags
      transformedSpans.find(_.getSpanId == "span_6").get.getTagsList should contain allElementsOf randomTags

    }
  }
}
