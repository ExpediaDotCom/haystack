package com.expedia.www.haystack.trace.reader.unit.readers.builders

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.api.Trace

import scala.collection.JavaConverters._

// helper to create various types of traces for unit testing
trait MultiServerSpanTraceBuilder extends TraceBuilder {
  /**
    * trace with multiple root spans
    *
    * ..................................................... x
    *   a |============================|
    *   b |----------------------------|
    * ..................................................... y
    *   b |==========| b |========|
    *
    */
  def buildMultiServerSpanForAClientSpanTrace(): Trace = {
    val aSpan = Span.newBuilder()
      .setSpanId("a")
      .setTraceId(traceId)
      .setServiceName("x")
      .setStartTime(startTimestamp)
      .setDuration(1000)
      .addAllLogs(createServerSpanTags(startTimestamp, startTimestamp + 1000).asJavaCollection)
      .build()

    val bSpan = Span.newBuilder()
      .setSpanId("b")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("x")
      .setStartTime(startTimestamp)
      .setDuration(1000)
      .addAllLogs(createClientSpanTags(startTimestamp, startTimestamp + 1000).asJavaCollection)
      .build()

    val bFirstServerSpan = Span.newBuilder()
      .setSpanId("b")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp)
      .setDuration(500)
      .addAllLogs(createClientSpanTags(startTimestamp, startTimestamp + 500).asJavaCollection)
      .build()

    val bSecondServerSpan = Span.newBuilder()
      .setSpanId("b")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("z")
      .setStartTime(startTimestamp + 500)
      .setDuration(500)
      .addAllLogs(createClientSpanTags(startTimestamp + 500, startTimestamp + 500 + 500).asJavaCollection)
      .build()

    toTrace(aSpan, bSpan, bFirstServerSpan, bSecondServerSpan)
  }
}
