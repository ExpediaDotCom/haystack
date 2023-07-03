package com.expedia.www.haystack.collector.commons.unit

import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.collector.commons.ProtoSpanExtractor
import com.expedia.www.haystack.collector.commons.ProtoSpanExtractor._
import com.expedia.www.haystack.collector.commons.config.{ExtractorConfiguration, Format, SpanMaxSize, SpanValidation}
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}
import org.slf4j.Logger

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import scala.collection.mutable.ArrayBuffer

class ProtoSpanExtractorSpec extends FunSpec with Matchers with MockitoSugar {

  private val EmptyString = ""
  private val NullString = null
  private val SpanId = "span ID"
  private val TraceId = "trace ID"
  private val ServiceName1 = "service name 1"
  private val ServiceName2 = "service name 2"
  private val OperationName1 = "operation name 1"
  private val OperationName2 = "operation name 2"
  private val StartTime = System.currentTimeMillis() * 1000
  private val Duration = 42
  private val Negative = -42
  private val SampleErrorTag = Tag.newBuilder().setKey("error").setVBool(true).build()
  private val SpanSizeLimit = 800
  private val SkipTagTruncationServiceName = "skip_tag_truncation_service"

  describe("Protobuf Span Extractor") {
    val mockLogger = mock[Logger]

    val spanSizeValidationConfig = SpanValidation(SpanMaxSize(enable = true, logOnly = false, SpanSizeLimit, "X-HAYSTACK-SPAN-INFO", "Tags Truncated", Seq("error"), Seq(SkipTagTruncationServiceName)))
    val protoSpanExtractor = new ProtoSpanExtractor(ExtractorConfiguration(Format.PROTO, spanSizeValidationConfig), mockLogger, List())

    val largestInvalidStartTime = SmallestAllowedStartTimeMicros - 1


    // @formatter:off
    val nullSpanIdSpan = createSpan(NullString, TraceId, ServiceName1, OperationName1, StartTime, Duration, createTags(1))
    val emptySpanIdSpan = createSpan(EmptyString, TraceId, ServiceName2, OperationName1, StartTime, Duration, createTags(1))
    val nullTraceIdSpan = createSpan(SpanId, NullString, ServiceName1, OperationName1, StartTime, Duration, createTags(1))
    val emptyTraceIdSpan = createSpan(SpanId, EmptyString, ServiceName2, OperationName1, StartTime, Duration, createTags(1))
    val nullServiceNameSpan = createSpan(SpanId, TraceId, NullString, OperationName1, StartTime, Duration, createTags(1))
    val emptyServiceNameSpan = createSpan(SpanId, TraceId, EmptyString, OperationName2, StartTime, Duration, createTags(1))
    val nullOperationNameSpan = createSpan(SpanId, TraceId, ServiceName1, NullString, StartTime, Duration, createTags(1))
    val emptyOperationNameSpan = createSpan(SpanId, TraceId, ServiceName2, EmptyString, StartTime, Duration, createTags(1))
    val tooSmallStartTimeSpan = createSpan(SpanId, TraceId, ServiceName1, OperationName1, largestInvalidStartTime, Duration, createTags(1))
    val negativeStartTimeSpan = createSpan(SpanId, TraceId, ServiceName2, OperationName1, Negative, Duration, createTags(1))
    val tooSmallDurationSpan = createSpan(SpanId, TraceId, ServiceName1, OperationName1, StartTime, Negative, createTags(1))
    val largeSizeSpan = createSpan(SpanId, TraceId, ServiceName1, OperationName1, StartTime, Duration, createTags(50))
    val largeSizeSpanWithSkippedService
    = createSpan(SpanId, TraceId, SkipTagTruncationServiceName, OperationName1, StartTime, Duration, createTags(50))
    val spanMap = ListMap(
      "NullSpanId" -> (nullSpanIdSpan, SpanIdIsRequired.format(ServiceName1, OperationName1)),
      "EmptySpanId" -> (emptySpanIdSpan, SpanIdIsRequired.format(ServiceName2, OperationName1)),
      "NullTraceId" -> (nullTraceIdSpan, TraceIdIsRequired.format(ServiceName1, OperationName1)),
      "EmptyTraceId" -> (emptyTraceIdSpan, TraceIdIsRequired.format(ServiceName2, OperationName1)),
      "NullServiceName" -> (nullServiceNameSpan, ServiceNameIsRequired.format(nullServiceNameSpan.toString)),
      "EmptyServiceName" -> (emptyServiceNameSpan, ServiceNameIsRequired.format(emptyServiceNameSpan.toString)),
      "NullOperationName" -> (nullOperationNameSpan, OperationNameIsRequired.format(ServiceName1)),
      "EmptyOperationName" -> (emptyOperationNameSpan, OperationNameIsRequired.format(ServiceName2)),
      "TooSmallStartTime" -> (tooSmallStartTimeSpan, StartTimeIsInvalid.format(largestInvalidStartTime, ServiceName1, OperationName1)),
      "NegativeStartTime" -> (negativeStartTimeSpan, StartTimeIsInvalid.format(Negative, ServiceName2, OperationName1)),
      "TooSmallDuration" -> (tooSmallDurationSpan, DurationIsInvalid.format(Negative, ServiceName1, OperationName1))
    )
    // @formatter:on
    it("should fail validation for spans with invalid data") {
      spanMap.foreach(sp => {
        val kvPairs = protoSpanExtractor.extractKeyValuePairs(sp._2._1.toByteArray)
        withClue(sp._1) {
          kvPairs shouldBe Nil
          verify(mockLogger).error(sp._2._2)
        }
      })
      Mockito.verifyNoMoreInteractions(mockLogger)
    }

    it("should truncate tags to reduce span when span size exceeded") {
      val kvPairs = protoSpanExtractor.extractKeyValuePairs(largeSizeSpan.toByteArray)
      kvPairs.foreach { kv =>
        val spanRecordBytes = kv.value
        val span = Span.parseFrom(spanRecordBytes)
        assert(span.getTagsList.asScala.exists(tag => tag.getKey.equalsIgnoreCase("error")))
        span.getTagsCount shouldBe 2
        assert(spanRecordBytes.length < SpanSizeLimit)
      }
    }

    it("shouldn't truncate tags to for skipped service even when span size exceeds limit") {
      val kvPairs = protoSpanExtractor.extractKeyValuePairs(largeSizeSpanWithSkippedService.toByteArray)
      kvPairs.foreach { kv =>
        val spanRecordBytes = kv.value
        val span = Span.parseFrom(spanRecordBytes)

        spanRecordBytes.length > SpanSizeLimit shouldEqual true
        span.getTagsList.size shouldEqual 50
        span.getTagsList.asScala.exists(tag => tag.getKey.equalsIgnoreCase("error")) shouldEqual true

      }
    }
  }

  private def createTags(maxTagsLimit: Int): Array[Tag] = {
    val tags = ArrayBuffer[Tag]()
    // adding Error Tag by default
    tags += SampleErrorTag
    for (i <- 0 until (maxTagsLimit - 1)) { // creating one less tag since error tag is already added above
      tags += Tag.newBuilder().setKey("key" + i).setVStr("value" + i).build()
    }
    tags.toArray
  }

  private def createSpan(spanId: String,
                         traceId: String,
                         serviceName: String,
                         operationName: String,
                         startTimeMicros: Long,
                         durationMicros: Long,
                         tags: Seq[Tag]) = {
    val builder = Span.newBuilder()
    if (spanId != null) {
      builder.setSpanId(spanId)
    }
    if (traceId != null) {
      builder.setTraceId(traceId)
    }
    if (serviceName != null) {
      builder.setServiceName(serviceName)
    }
    if (operationName != null) {
      builder.setOperationName(operationName)
    }
    if (tags.nonEmpty) {
      tags.foreach(tag => builder.addTags(tag))
    }
    builder.setStartTime(startTimeMicros)
    builder.setDuration(durationMicros)
    builder.build()
  }
}
