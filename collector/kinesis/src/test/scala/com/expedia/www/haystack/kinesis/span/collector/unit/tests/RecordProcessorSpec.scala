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

package com.expedia.www.haystack.kinesis.span.collector.unit.tests

import java.nio.ByteBuffer
import java.util.Date

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput
import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.amazonaws.services.kinesis.model.Record
import com.expedia.open.tracing.Span
import com.expedia.www.haystack.collector.commons.config.{ExtractorConfiguration, Format, SpanMaxSize, SpanValidation}
import com.expedia.www.haystack.collector.commons.record.KeyValuePair
import com.expedia.www.haystack.collector.commons.sink.RecordSink
import com.expedia.www.haystack.collector.commons.{MetricsSupport, ProtoSpanExtractor}
import com.expedia.www.haystack.kinesis.span.collector.config.entities.KinesisConsumerConfiguration
import com.expedia.www.haystack.kinesis.span.collector.kinesis.RecordProcessor
import org.easymock.EasyMock
import org.easymock.EasyMock._
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class RecordProcessorSpec extends FunSpec with Matchers with EasyMockSugar with MetricsSupport {
  private val StartTimeMicros = System.currentTimeMillis() * 1000
  private val DurationMicros = 42
  describe("Record Processor") {

    val kinesisConfig = KinesisConsumerConfiguration("us-west-2", None,
      "app-group", "stream-1", InitialPositionInStream.LATEST, 10.seconds, 10, 10.seconds, None, None, None,
      10000, 500.millis, 10000.millis, MetricsLevel.NONE, 10000.millis, 200.millis)

    it("should process the record, sends to sink and perform checkpointing") {
      val sink = mock[RecordSink]
      val checkpointer = mock[IRecordProcessorCheckpointer]

      val span_1 = Span.newBuilder()
        .setSpanId("span-id-1")
        .setTraceId("trace-id")
        .setServiceName("service")
        .setOperationName("operation")
        .setStartTime(StartTimeMicros)
        .setDuration(DurationMicros)
        .build()
      val record = new Record()
        .withApproximateArrivalTimestamp(new Date())
        .withData(ByteBuffer.wrap(span_1.toByteArray))

      val capturedKVPair = EasyMock.newCapture[KeyValuePair[Array[Byte], Array[Byte]]]()

      expecting {
        sink.toAsync(
          capture(capturedKVPair),
          anyObject(classOf[(KeyValuePair[Array[Byte], Array[Byte]], Exception) => Unit]))
      }.times(2)

      expecting {
        checkpointer.checkpoint()
      }.once()

      whenExecuting(sink, checkpointer) {
        val spanValidationConfig = SpanValidation(SpanMaxSize(enable = false, logOnly = false, 5000, "", "", Seq(), Seq()))
        val processor = new RecordProcessor(kinesisConfig, new ProtoSpanExtractor(ExtractorConfiguration(Format.PROTO, spanValidationConfig), LoggerFactory.getLogger(classOf[ProtoSpanExtractor]), List()), sink)
        val input = new ProcessRecordsInput().withRecords(List(record).asJava).withCheckpointer(checkpointer)
        processor.processRecords(input)

        capturedKVPair.getValue.key shouldEqual "trace-id".getBytes("UTF-8")
        capturedKVPair.getValue.value shouldEqual span_1.toByteArray

        // check-pointing should be called just once
        processor.processRecords(input)
      }
    }

    it("should process a span without transactionid, but send to sink and perform checkpointing") {
      val sink = mock[RecordSink]
      val checkpointer = mock[IRecordProcessorCheckpointer]

      val span_1 = Span.newBuilder()
        .setSpanId("span-id-1")
        .setTraceId("trace-id-1")
        .setServiceName("service")
        .setOperationName("operation")
        .setStartTime(StartTimeMicros)
        .setDuration(DurationMicros)
        .build()

      val span_2 = Span.newBuilder()
        .setSpanId("span-id-2")
        .setTraceId("trace-id-2")
        .setServiceName("service")
        .setOperationName("operation")
        .setStartTime(StartTimeMicros)
        .setDuration(DurationMicros)
        .build()

      val record_1 = new Record()
        .withPartitionKey(null)
        .withApproximateArrivalTimestamp(new Date())
        .withData(ByteBuffer.wrap(span_1.toByteArray))

      val record_2 = new Record()
        .withPartitionKey(null)
        .withApproximateArrivalTimestamp(new Date())
        .withData(ByteBuffer.wrap(span_2.toByteArray))

      val captureKvPair = EasyMock.newCapture[KeyValuePair[Array[Byte], Array[Byte]]]()

      expecting {
        sink.toAsync(
          capture(captureKvPair),
          anyObject(classOf[(KeyValuePair[Array[Byte], Array[Byte]], Exception) => Unit]))
      }.times(2)

      expecting {
        checkpointer.checkpoint()
      }.once()

      whenExecuting(sink, checkpointer) {
        val spanValidationConfig = SpanValidation(SpanMaxSize(enable = false, logOnly = false, 5000, "", "", Seq(), Seq()))
        val processor = new RecordProcessor(kinesisConfig, new ProtoSpanExtractor(ExtractorConfiguration(Format.PROTO, spanValidationConfig), LoggerFactory.getLogger(classOf[ProtoSpanExtractor]), List()), sink)
        val input_1 = new ProcessRecordsInput().withRecords(List(record_1).asJava).withCheckpointer(checkpointer)
        processor.processRecords(input_1)

        captureKvPair.getValue.key shouldEqual "trace-id-1".getBytes("UTF-8")
        captureKvPair.getValue.value shouldEqual span_1.toByteArray

        val input = new ProcessRecordsInput().withRecords(List(record_2).asJava).withCheckpointer(checkpointer)
        processor.processRecords(input)

        captureKvPair.getValue.key shouldEqual "trace-id-2".getBytes("UTF-8")
        captureKvPair.getValue.value shouldEqual span_2.toByteArray

      }
    }

    it("should not emit an illegal json span to sink but perform checkpointing") {
      val sink = mock[RecordSink]
      val checkpointer = mock[IRecordProcessorCheckpointer]

      val spanData = "random-span-proto-bytes".getBytes()
      val record = new Record().withPartitionKey(null).withApproximateArrivalTimestamp(new Date()).withData(ByteBuffer.wrap(spanData))

      expecting {
        checkpointer.checkpoint()
      }.once

      whenExecuting(sink, checkpointer) {
        val spanValidationConfig = SpanValidation(SpanMaxSize(enable = false, logOnly = false, 5000, "", "", Seq(), Seq()))
        val processor = new RecordProcessor(kinesisConfig, new ProtoSpanExtractor(ExtractorConfiguration(Format.PROTO, spanValidationConfig), LoggerFactory.getLogger(classOf[ProtoSpanExtractor]), List()), sink)
        val input = new ProcessRecordsInput().withRecords(List(record).asJava).withCheckpointer(checkpointer)
        processor.processRecords(input)
      }
    }
  }
}
