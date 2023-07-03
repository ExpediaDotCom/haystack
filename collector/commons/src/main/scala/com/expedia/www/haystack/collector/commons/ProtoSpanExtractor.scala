/*
 *  Copyright 2018 Expedia, Inc.
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

package com.expedia.www.haystack.collector.commons

import java.nio.charset.Charset
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.collector.commons.ProtoSpanExtractor._
import com.expedia.www.haystack.collector.commons.config.{ExtractorConfiguration, Format}
import com.expedia.www.haystack.collector.commons.record.{KeyValueExtractor, KeyValuePair}
import com.expedia.www.haystack.span.decorators.SpanDecorator
import com.google.protobuf.util.JsonFormat
import org.slf4j.Logger

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object ProtoSpanExtractor {
  private val DaysInYear1970 = 365
  private val January_1_1971_00_00_00_GMT: Instant = Instant.EPOCH.plus(DaysInYear1970, ChronoUnit.DAYS)
  // A common mistake clients often make is to pass in milliseconds instead of microseconds for start time.
  // Insisting that all start times be > January 1 1971 GMT catches this error.
  val SmallestAllowedStartTimeMicros: Long = January_1_1971_00_00_00_GMT.getEpochSecond * 1000000
  val ServiceNameIsRequired = "Service Name is required: span=[%s]"
  val OperationNameIsRequired = "Operation Name is required: serviceName=[%s]"
  val SpanIdIsRequired = "Span ID is required: serviceName=[%s] operationName=[%s]"
  val TraceIdIsRequired = "Trace ID is required: serviceName=[%s] operationName=[%s]"
  val StartTimeIsInvalid = "Start time [%d] is invalid: serviceName=[%s] operationName=[%s]"
  val DurationIsInvalid = "Duration [%d] is invalid: serviceName=[%s] operationName=[%s]"
  val SpanSizeLimitExceeded = "Span Size Limit Exceeded: serviceName=[%s] operationName=[%s] traceId=[%s] spanSize=[%d] probableTags=[%s]"

  val ServiceNameVsTtlAndOperationNames = new ConcurrentHashMap[String, TtlAndOperationNames]
  val OperationNameCountExceededMeterName = "operation.name.count.exceeded"
}

class ProtoSpanExtractor(extractorConfiguration: ExtractorConfiguration,
                         val LOGGER: Logger, spanDecorators: List[SpanDecorator])
  extends KeyValueExtractor with MetricsSupport {

  private val printer = JsonFormat.printer().omittingInsignificantWhitespace()

  private val invalidSpanMeter = metricRegistry.meter("invalid.span")
  private val validSpanMeter = metricRegistry.meter("valid.span")
  private val spanSizeLimitExceededMeter = metricRegistry.meter("sizeLimitExceeded.span")

  override def configure(): Unit = ()

  def validateServiceName(span: Span): Try[Span] = {
    validate(span, span.getServiceName, ServiceNameIsRequired, span.toString)
  }

  def validateOperationName(span: Span): Try[Span] = {
    validate(span, span.getOperationName, OperationNameIsRequired, span.getServiceName)
  }

  def validateSpanId(span: Span): Try[Span] = {
    validate(span, span.getSpanId, SpanIdIsRequired, span.getServiceName, span.getOperationName)
  }

  def validateTraceId(span: Span): Try[Span] = {
    validate(span, span.getTraceId, TraceIdIsRequired, span.getServiceName, span.getOperationName)
  }

  def validateStartTime(span: Span): Try[Span] = {
    validate(span, span.getStartTime, StartTimeIsInvalid, SmallestAllowedStartTimeMicros, span.getServiceName, span.getOperationName)
  }

  def validateDuration(span: Span): Try[Span] = {
    validate(span, span.getDuration, DurationIsInvalid, 0, span.getServiceName, span.getOperationName)
  }

  def validateSpanSize(span: Span): Try[Span] = {
    if (extractorConfiguration.spanValidation.spanMaxSize.enable
      && !extractorConfiguration.spanValidation.spanMaxSize.skipServices.contains(span.getServiceName.toLowerCase)) {
      val spanSize = span.toByteArray.length
      val maxSizeLimit = extractorConfiguration.spanValidation.spanMaxSize.maxSizeLimit
      validate(span, spanSize, SpanSizeLimitExceeded, maxSizeLimit)
    }
    else
      Success(span)
  }

  private def validate(span: Span,
                       valueToValidate: String,
                       msg: String,
                       serviceName: String): Try[Span] = {
    if (Option(valueToValidate).getOrElse("").isEmpty) {
      Failure(new IllegalArgumentException(msg.format(serviceName)))
    } else {
      Success(span)
    }
  }

  private def validate(span: Span,
                       valueToValidate: String,
                       msg: String,
                       serviceName: String,
                       operationName: String): Try[Span] = {
    if (Option(valueToValidate).getOrElse("").isEmpty) {
      Failure(new IllegalArgumentException(msg.format(serviceName, operationName)))
    } else {
      Success(span)
    }
  }

  private def validate(span: Span,
                       valueToValidate: Long,
                       msg: String,
                       smallestValidValue: Long,
                       serviceName: String,
                       operationName: String): Try[Span] = {
    if (valueToValidate < smallestValidValue) {
      Failure(new IllegalArgumentException(msg.format(valueToValidate, serviceName, operationName)))
    } else {
      Success(span)
    }
  }

  private def validate(span: Span,
                       valueToValidate: Int,
                       msg: String,
                       highestValidValue: Int): Try[Span] = {

    if (valueToValidate > highestValidValue) {
      spanSizeLimitExceededMeter.mark()
      LOGGER.debug(msg.format(span.getServiceName, span.getOperationName, span.getTraceId, valueToValidate, getProbableTagsExceedingSizeLimit(span)))
      if (extractorConfiguration.spanValidation.spanMaxSize.logOnly) {
        Success(span)
      } else {
        Success(truncateTags(span))
      }
    }
    else {
      Success(span)
    }
  }

  private def getProbableTagsExceedingSizeLimit(span: Span): String = {
    span.getTagsList.asScala
      .filter(tag => tag.getVStrBytes.size > extractorConfiguration.spanValidation.spanMaxSize.maxSizeLimit)
      .map(_.getKey)
      .mkString(", ")
  }

  private def truncateTags(span: Span): Span = {
    val skippedTags = span.getTagsList.asScala
      .filter(tag => extractorConfiguration.spanValidation.spanMaxSize.skipTags.contains(tag.getKey.toLowerCase))

    val spanBuilder = span.toBuilder
    spanBuilder.clearTags()

    skippedTags.foreach(spanBuilder.addTags)

    val truncateTagKey = extractorConfiguration.spanValidation.spanMaxSize.infoTagKey
    val truncateTagValue = extractorConfiguration.spanValidation.spanMaxSize.infoTagValue
    spanBuilder.addTags(Tag.newBuilder().setKey(truncateTagKey).setVStr(truncateTagValue))

    spanBuilder.build()
  }


  override def extractKeyValuePairs(recordBytes: Array[Byte]): List[KeyValuePair[Array[Byte], Array[Byte]]] = {
    Try(Span.parseFrom(recordBytes))
      .flatMap(span => validateSpanSize(span))
      .flatMap(span => validateServiceName(span))
      .flatMap(span => validateOperationName(span))
      .flatMap(span => validateSpanId(span))
      .flatMap(span => validateTraceId(span))
      .flatMap(span => validateStartTime(span))
      .flatMap(span => validateDuration(span))
    match {
      case Success(span) =>
        validSpanMeter.mark()

        val updatedSpan = decorateSpan(span)
        val kvPair = extractorConfiguration.outputFormat match {
          case Format.JSON => KeyValuePair(updatedSpan.getTraceId.getBytes, printer.print(span).getBytes(Charset.forName("UTF-8")))
          case Format.PROTO => KeyValuePair(updatedSpan.getTraceId.getBytes, updatedSpan.toByteArray)
        }
        List(kvPair)

      case Failure(ex) =>
        invalidSpanMeter.mark()
        ex match {
          case ex: IllegalArgumentException => LOGGER.error(ex.getMessage)
          case _: java.lang.Exception => LOGGER.error("Fail to deserialize the span proto bytes with exception", ex)
        }
        Nil
    }
  }

  private def decorateSpan(span: Span): Span = {
    if (spanDecorators.isEmpty) {
      return span
    }

    var spanBuilder = span.toBuilder
    spanDecorators.foreach(decorator => {
      spanBuilder = decorator.decorate(spanBuilder)
    })
    spanBuilder.build()
  }
}
