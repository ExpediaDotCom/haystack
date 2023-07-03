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

package com.expedia.www.haystack.trace.indexer.unit

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.commons.packer.{NoopPacker, PackedMessage, PackerType}
import com.expedia.www.haystack.trace.indexer.config.entities.SpanAccumulatorConfiguration
import com.expedia.www.haystack.trace.indexer.processors.SpanIndexProcessor
import com.expedia.www.haystack.trace.indexer.store.SpanBufferMemoryStoreSupplier
import com.expedia.www.haystack.trace.indexer.store.data.model.SpanBufferWithMetadata
import com.expedia.www.haystack.trace.indexer.store.traits.SpanBufferKeyValueStore
import com.expedia.www.haystack.trace.indexer.writers.TraceWriter
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.record.TimestampType
import org.easymock.EasyMock
import org.easymock.EasyMock._
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

import scala.collection.mutable

class SpanIndexProcessorSpec extends FunSpec with Matchers with EasyMockSugar {
  private implicit val executor = scala.concurrent.ExecutionContext.global

  private val TRACE_ID = "traceid"
  private val startRecordTimestamp = System.currentTimeMillis()
  private val timestampInterval = 100
  private val maxSpans = 10
  private val bufferingWindow = 10000
  private val startRecordOffset = 11
  private val accumulatorConfig = SpanAccumulatorConfiguration(10, 100, 2000, bufferingWindow, PackerType.NONE)

  describe("Span Index Processor") {
    it("should process the records for a partition and return the offsets to commit") {
      // mock entities
      val mockStore = mock[SpanBufferKeyValueStore]
      val storeSupplier = new SpanBufferMemoryStoreSupplier(10, 100) {
        override def get(): SpanBufferKeyValueStore = mockStore
      }

      val mockBackend = mock[TraceWriter]

      val processor = new SpanIndexProcessor(accumulatorConfig, storeSupplier, Seq(mockBackend), new NoopPacker[SpanBuffer])(executor)
      val (spanBufferWithMetadata, records) = createConsumerRecordsAndSetStoreExpectation(maxSpans, startRecordTimestamp, timestampInterval, startRecordOffset, mockStore)
      val finalStreamTimestamp = startRecordTimestamp + ((maxSpans - 1) * timestampInterval)

      val packedMessage = EasyMock.newCapture[PackedMessage[SpanBuffer]]()
      val writeTraceIdCapture = EasyMock.newCapture[String]()
      val writeLastRecordCapture = EasyMock.newCapture[Boolean]()

      expecting {
        mockStore.addEvictionListener(processor)
        mockStore.init()
        mockStore.getAndRemoveSpanBuffersOlderThan(finalStreamTimestamp - bufferingWindow).andReturn(mutable.ListBuffer(spanBufferWithMetadata))
        mockBackend.writeAsync(capture(writeTraceIdCapture), capture(packedMessage), capture(writeLastRecordCapture))
        mockStore.close()
      }

      whenExecuting(mockStore, mockBackend) {
        processor.init()
        val offsets = processor.process(records)
        SpanBuffer.parseFrom(packedMessage.getValue.packedDataBytes).getChildSpansCount shouldBe maxSpans
        writeTraceIdCapture.getValue shouldBe TRACE_ID
        writeLastRecordCapture.getValue shouldBe true
        offsets.get.offset() shouldBe startRecordOffset

        processor.close()
      }
    }

    it("should process the records for a partition, and if store does not emit any 'old' spanBuffers, then writers will not be called and no offsets will be committted") {
      // mock entities
      val mockStore = mock[SpanBufferKeyValueStore]
      val storeSupplier = new SpanBufferMemoryStoreSupplier(10, 100) {
        override def get(): SpanBufferKeyValueStore = mockStore
      }

      val mockBackend = mock[TraceWriter]

      val processor = new SpanIndexProcessor(accumulatorConfig, storeSupplier, Seq(mockBackend), new NoopPacker)(executor)
      val (_, records) = createConsumerRecordsAndSetStoreExpectation(maxSpans, startRecordTimestamp, timestampInterval, startRecordOffset, mockStore)
      val finalStreamTimestamp = startRecordTimestamp + ((maxSpans - 1) * timestampInterval)

      expecting {
        mockStore.addEvictionListener(processor)
        mockStore.init()
        mockStore.getAndRemoveSpanBuffersOlderThan(finalStreamTimestamp - bufferingWindow).andReturn(mutable.ListBuffer())
      }

      whenExecuting(mockStore, mockBackend) {
        processor.init()
        val offsets = processor.process(records)
        offsets shouldBe 'empty
      }
    }
  }

  private def createConsumerRecordsAndSetStoreExpectation(maxSpans: Int,
                                                          startRecordTimestamp: Long,
                                                          timestampInterval: Long,
                                                          startRecordOffset: Int,
                                                          mockStore: SpanBufferKeyValueStore):
  (SpanBufferWithMetadata, Iterable[ConsumerRecord[String, Span]]) = {

    val builder = SpanBuffer.newBuilder().setTraceId(TRACE_ID)
    val spanBufferWithMetadata = SpanBufferWithMetadata(builder, startRecordTimestamp, startRecordOffset)

    val consumerRecords =
    for (idx <- 0 until maxSpans;
         span = Span.newBuilder().setTraceId(TRACE_ID).setSpanId(idx.toString).build();
         timestamp = startRecordTimestamp + (idx * timestampInterval);
         _ = builder.addChildSpans(span);
         _ = mockStore.addOrUpdateSpanBuffer(TRACE_ID, span, timestamp, idx + startRecordOffset).andReturn(spanBufferWithMetadata))
      yield new ConsumerRecord[String, Span]("topic",
        0,
        idx + startRecordOffset,
        timestamp,
        TimestampType.CREATE_TIME,
        0, 0, 0,
        TRACE_ID,
        span)

    (spanBufferWithMetadata, consumerRecords)
  }
}
