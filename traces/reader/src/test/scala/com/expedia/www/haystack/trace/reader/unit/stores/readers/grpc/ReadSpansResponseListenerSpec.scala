/*
 *  Copyright 2017 Expedia, Inc.
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

package com.expedia.www.haystack.trace.reader.unit.stores.readers.grpc

import java.util.concurrent.Future

import com.codahale.metrics.{Meter, Timer}
import com.expedia.open.tracing.Span
import com.expedia.open.tracing.api.Trace
import com.expedia.open.tracing.backend.{ReadSpansResponse, TraceRecord}
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.commons.packer.NoopPacker
import com.expedia.www.haystack.trace.reader.stores.readers.grpc.ReadSpansResponseListener
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec
import com.google.protobuf.ByteString
import io.grpc.{Status, StatusException}
import org.easymock.EasyMock

import scala.collection.JavaConverters._
import scala.concurrent.Promise

class ReadSpansResponseListenerSpec extends BaseUnitTestSpec {
  val packer = new NoopPacker[SpanBuffer]

  describe("read span response listener for raw traces") {
    it("should read the trace-records, de-serialized spans and return the complete trace") {
      val mockReadResult = mock[Future[ReadSpansResponse]]

      val promise = mock[Promise[Seq[Trace]]]
      val failureMeter = mock[Meter]
      val tracesFailures = mock[Meter]
      val timer = mock[Timer.Context]

      val span_1 = Span.newBuilder().setTraceId("TRACE_ID1").setSpanId("SPAN_ID_1")
      val spanBuffer_1 = packer.apply(SpanBuffer.newBuilder().setTraceId("TRACE_ID1").addChildSpans(span_1).build())
      val traceRecord_1 = TraceRecord.newBuilder()
        .setTraceId("TRACE_ID1")
        .setTimestamp(System.currentTimeMillis())
        .setSpans(ByteString.copyFrom(spanBuffer_1.packedDataBytes))
        .build()
      val span_2 = Span.newBuilder().setTraceId("TRACE_ID1").setSpanId("SPAN_ID_2")
      val spanBuffer_2 = packer.apply(SpanBuffer.newBuilder().setTraceId("TRACE_ID1").addChildSpans(span_2).build())
      val traceRecord_2 = TraceRecord.newBuilder()
        .setTraceId("TRACE_ID1")
        .setTimestamp(System.currentTimeMillis())
        .setSpans(ByteString.copyFrom(spanBuffer_2.packedDataBytes))
        .build()

      val span_3 = Span.newBuilder().setTraceId("TRACE_ID3").setSpanId("SPAN_ID_3")
      val spanBuffer_3 = packer.apply(SpanBuffer.newBuilder().setTraceId("TRACE_ID3").addChildSpans(span_3).build())
      val traceRecord_3 = TraceRecord.newBuilder()
        .setTraceId("TRACE_ID3")
        .setTimestamp(System.currentTimeMillis())
        .setSpans(ByteString.copyFrom(spanBuffer_3.packedDataBytes))
        .build()

      val readSpanResponse = ReadSpansResponse.newBuilder().addAllRecords(List(traceRecord_1, traceRecord_2, traceRecord_3).asJava).build()
      val capturedTraces = EasyMock.newCapture[Seq[Trace]]()
      val capturedMeter = EasyMock.newCapture[Int]()
      expecting {
        timer.close()
        tracesFailures.mark(EasyMock.capture(capturedMeter))
        mockReadResult.get().andReturn(readSpanResponse)
        promise.success(EasyMock.capture(capturedTraces)).andReturn(promise)
      }

      whenExecuting(mockReadResult, promise, tracesFailures, failureMeter, timer) {
        val listener = new ReadSpansResponseListener(mockReadResult, promise, timer, failureMeter, tracesFailures, 2)
        listener.run()
        val traceIdSpansMap: Map[String, Set[String]] = capturedTraces.getValue.map(capturedTrace =>
          capturedTrace.getTraceId -> capturedTrace.getChildSpansList.asScala.map(_.getSpanId).toSet).toMap

        traceIdSpansMap("TRACE_ID1") shouldEqual Set("SPAN_ID_1", "SPAN_ID_2")
        traceIdSpansMap("TRACE_ID3") shouldEqual Set("SPAN_ID_3")

        capturedMeter.getValue shouldEqual 0
      }
    }



    it("should return an exception for empty traceId") {
      val mockReadResult = mock[Future[ReadSpansResponse]]
      val promise = mock[Promise[Seq[Trace]]]
      val failureMeter = mock[Meter]
      val tracesFailures = mock[Meter]
      val timer = mock[Timer.Context]
      val readSpansResponse = ReadSpansResponse.newBuilder().build()
      val capturedException = EasyMock.newCapture[StatusException]()
      val capturedMeter = EasyMock.newCapture[Int]()
      expecting {
        timer.close()
        failureMeter.mark()
        tracesFailures.mark(EasyMock.capture(capturedMeter))
        mockReadResult.get().andReturn(readSpansResponse)
        promise.failure(EasyMock.capture(capturedException)).andReturn(promise)
      }

      whenExecuting(mockReadResult, promise, failureMeter, tracesFailures, timer) {
        val listener = new ReadSpansResponseListener(mockReadResult, promise, timer, failureMeter, tracesFailures, 0)
        listener.run()
        capturedException.getValue.getStatus.getCode shouldEqual Status.NOT_FOUND.getCode
        capturedMeter.getValue shouldEqual 0
      }
    }

  }
}
