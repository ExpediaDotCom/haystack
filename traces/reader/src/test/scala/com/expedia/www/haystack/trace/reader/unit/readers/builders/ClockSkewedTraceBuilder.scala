package com.expedia.www.haystack.trace.reader.unit.readers.builders

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.api.Trace
import com.expedia.www.haystack.trace.reader.readers.utils.SpanTree

import scala.collection.JavaConverters._

// helper to create various types of traces for unit testing
trait ClockSkewedTraceBuilder extends TraceBuilder {

  /**
    * trace spanning multiple services without clock skew
    *
    * ...................................................... x
    *     a |============================================|
    *     b |---------------------|
    *                           c |----------------------|
    *
    *  ..................................................... y
    *     b |=====================|
    *     d |---------|
    *               e |-----------|
    *
    */
  def buildMultiServiceWithoutSkewTrace(): Trace = {
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
      .setDuration(500)
      .addAllLogs(createClientSpanTags(startTimestamp, startTimestamp + 500).asJavaCollection)
      .build()

    val cSpan = Span.newBuilder()
      .setSpanId("c")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("x")
      .setStartTime(startTimestamp + 500)
      .setDuration(500)
      .addAllLogs(createClientSpanTags(startTimestamp + 500, startTimestamp + 500 + 500).asJavaCollection)
      .build()

    val bServerSpan = Span.newBuilder()
      .setSpanId("b")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp)
      .setDuration(500)
      .addAllLogs(createServerSpanTags(startTimestamp, startTimestamp + 500).asJavaCollection)
      .build()

    val dSpan = Span.newBuilder()
      .setSpanId("d")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp)
      .setDuration(200)
      .addAllLogs(createClientSpanTags(startTimestamp, startTimestamp + 200).asJavaCollection)
      .build()

    val eSpan = Span.newBuilder()
      .setSpanId("e")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp + 200)
      .setDuration(300)
      .addAllLogs(createClientSpanTags(startTimestamp + 200, startTimestamp + 200 + 300).asJavaCollection)
      .build()

    toTrace(aSpan, bSpan, cSpan, bServerSpan, dSpan, eSpan)
  }

  /**
    * trace spanning multiple services with positive clock skew
    *
    * ...................................................... x
    *     a |============================================|
    *     b |---------------------|
    *                           c |----------------------|
    *
    *  ..................................................... y
    *        b |=====================|
    *        d |--------|
    *                 e |------------|
    *
    */
  def buildMultiServiceWithPositiveSkewTrace(): Trace = {
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
      .setDuration(500)
      .addAllLogs(createClientSpanTags(startTimestamp, startTimestamp + 500).asJavaCollection)
      .build()

    val cSpan = Span.newBuilder()
      .setSpanId("c")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("x")
      .setStartTime(startTimestamp + 500)
      .setDuration(500)
      .addAllLogs(createClientSpanTags(startTimestamp + 500, startTimestamp + 500 + 500).asJavaCollection)
      .build()

    val bServerSpan = Span.newBuilder()
      .setSpanId("b")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp + 100)
      .setDuration(500)
      .addAllLogs(createServerSpanTags(startTimestamp + 100, startTimestamp + 100 + 500).asJavaCollection)
      .build()

    val dSpan = Span.newBuilder()
      .setSpanId("d")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp + 100)
      .setDuration(200)
      .addAllLogs(createClientSpanTags(startTimestamp + 100, startTimestamp + 100 + 200).asJavaCollection)
      .build()

    val eSpan = Span.newBuilder()
      .setSpanId("e")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp + 300)
      .setDuration(300)
      .addAllLogs(createClientSpanTags(startTimestamp + 300, startTimestamp + 300 + 300).asJavaCollection)
      .build()

    toTrace(aSpan, bSpan, cSpan, bServerSpan, dSpan, eSpan)
  }

  /**
    * trace spanning multiple services with negative clock skew
    *
    * ...................................................... x
    *     a |============================================|
    *     b |---------------------|
    *                           c |----------------------|
    *
    *  ..................................................... y
    *  b |===================|
    *  d |--------|
    *           e |----------|
    *
    */
  def buildMultiServiceWithNegativeSkewTrace(): Trace = {
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
      .setDuration(500)
      .addAllLogs(createClientSpanTags(startTimestamp, startTimestamp + 500).asJavaCollection)
      .build()

    val cSpan = Span.newBuilder()
      .setSpanId("c")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("x")
      .setStartTime(startTimestamp + 500)
      .setDuration(500)
      .addAllLogs(createClientSpanTags(startTimestamp + 500, startTimestamp + 500 + 500).asJavaCollection)
      .build()

    val bServerSpan = Span.newBuilder()
      .setSpanId("b")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp - 100)
      .setDuration(500)
      .addAllLogs(createServerSpanTags(startTimestamp - 100, startTimestamp - 100 + 500).asJavaCollection)
      .build()

    val dSpan = Span.newBuilder()
      .setSpanId("d")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp - 100)
      .setDuration(200)
      .addAllLogs(createClientSpanTags(startTimestamp - 100, startTimestamp - 100 + 200).asJavaCollection)
      .build()

    val eSpan = Span.newBuilder()
      .setSpanId("e")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp + 100)
      .setDuration(300)
      .addAllLogs(createClientSpanTags(startTimestamp + 100, startTimestamp + 100 + 300).asJavaCollection)
      .build()

    toTrace(aSpan, bSpan, cSpan, bServerSpan, dSpan, eSpan)
  }

  /**
    * trace spanning multiple services with multi-level clock skew
    *
    * ...................................................... x
    *     a |============================================|
    *     b |--------------------------------------------|
    *
    *  ..................................................... y
    *  b |============================================|
    *  c |--------------------------------------------|
    *
    *  ..................................................... z
    *         c |============================================|
    *         d |--------------------------------------------|
    *
    */
  def buildMultiLevelSkewTrace(): Trace = {
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

    val bServerSpan = Span.newBuilder()
      .setSpanId("b")
      .setParentSpanId("a")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp - 100)
      .setDuration(1000)
      .addAllLogs(createServerSpanTags(startTimestamp - 100, startTimestamp - 100 + 1000).asJavaCollection)
      .build()

    val cSpan = Span.newBuilder()
      .setSpanId("c")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("y")
      .setStartTime(startTimestamp - 100)
      .setDuration(1000)
      .addAllLogs(createClientSpanTags(startTimestamp - 100, startTimestamp -100 + 1000).asJavaCollection)
      .build()

    val cServerSpan = Span.newBuilder()
      .setSpanId("c")
      .setParentSpanId("b")
      .setTraceId(traceId)
      .setServiceName("z")
      .setStartTime(startTimestamp + 500)
      .setDuration(1000)
      .addAllLogs(createServerSpanTags(startTimestamp + 500, startTimestamp + 500 + 1000).asJavaCollection)
      .build()

    val dSpan = Span.newBuilder()
      .setSpanId("d")
      .setParentSpanId("c")
      .setTraceId(traceId)
      .setServiceName("z")
      .setStartTime(startTimestamp + 500)
      .setDuration(1000)
      .addAllLogs(createClientSpanTags(startTimestamp + 500, startTimestamp + 500 + 1000).asJavaCollection)
      .build()

    toTrace(aSpan, bSpan, cSpan, bServerSpan, cServerSpan, dSpan)
  }
}
