package com.expedia.www.haystack.trace.reader.unit.readers.builders

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.api.Trace

import scala.collection.JavaConverters._

// helper to create various types of traces for unit testing
trait MultiRootTraceBuilder extends TraceBuilder {

  /**
    * trace with multiple root spans
    *
    * ..................................................... x
    *   a |=========| b |===================|
    *                 c |-------------------|
    *                         d |------|
    *
    */
  def buildMultiRootTrace(): Trace = {
    val aSpan = Span.newBuilder()
      .setSpanId("a")
      .setTraceId(traceId)
      .setServiceName("x")
      .setStartTime(startTimestamp)
      .setDuration(300)
      .addAllLogs(createServerSpanTags(startTimestamp, startTimestamp + 300).asJavaCollection)
      .build()

    val bSpan = Span.newBuilder()
      .setSpanId("b")
      .setTraceId(traceId)
      .setServiceName("x")
      .setStartTime(startTimestamp + 500)
      .setDuration(500)
      .addAllLogs(createServerSpanTags(startTimestamp + 500, startTimestamp + 500 + 500).asJavaCollection)
      .build()

    val cSpan = Span.newBuilder()
      .setSpanId("c")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("x")
      .setStartTime(startTimestamp + 500)
      .setDuration(500)
      .addAllLogs(createClientSpanTags(startTimestamp + 500, startTimestamp + 500 + 500).asJavaCollection)
      .build()

    val dSpan = Span.newBuilder()
      .setSpanId("d")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("x")
      .setStartTime(startTimestamp + 750)
      .setDuration(200)
      .addAllLogs(createClientSpanTags(startTimestamp + 750, startTimestamp + 750 + 200).asJavaCollection)
      .build()

    toTrace(aSpan, bSpan, cSpan, dSpan)
  }
}
