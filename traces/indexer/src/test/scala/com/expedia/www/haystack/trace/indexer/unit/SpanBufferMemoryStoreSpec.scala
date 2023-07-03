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

package com.expedia.www.haystack.trace.indexer.unit

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.trace.indexer.store.DynamicCacheSizer
import com.expedia.www.haystack.trace.indexer.store.impl.SpanBufferMemoryStore
import org.apache.kafka.streams.processor.internals.{ProcessorContextImpl, RecordCollector}
import org.apache.kafka.streams.processor.{StateRestoreCallback, StateStore, TaskId}
import org.easymock.EasyMock._
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class SpanBufferMemoryStoreSpec extends FunSpec with Matchers with EasyMockSugar {

  private val TRACE_ID_1 = "TraceId_1"
  private val TRACE_ID_2 = "TraceId_2"

  describe("SpanBuffer Memory Store") {
    it("should create spanBuffer, add child spans and allow retrieving old spanBuffers from the store") {
      val (context, rootStateStore, recordCollector, spanBufferStore) = createSpanBufferStore

      whenExecuting(context, recordCollector, rootStateStore) {

        val span1 = Span.newBuilder().setTraceId(TRACE_ID_1).setSpanId("SPAN_ID_1").build()
        val span2 = Span.newBuilder().setTraceId(TRACE_ID_1).setSpanId("SPAN_ID_2").build()

        spanBufferStore.addOrUpdateSpanBuffer(TRACE_ID_1, span1, 11000L, 10)
        spanBufferStore.addOrUpdateSpanBuffer(TRACE_ID_1, span2, 12000L, 11)

        spanBufferStore.totalSpans shouldBe 2

        val result = spanBufferStore.getAndRemoveSpanBuffersOlderThan(13000L)

        result.size shouldBe 1
        result.foreach {
          spanBufferWithMetadata =>
            spanBufferWithMetadata.builder.getTraceId shouldBe TRACE_ID_1
            spanBufferWithMetadata.builder.getChildSpansCount shouldBe 2
            spanBufferWithMetadata.builder.getChildSpans(0).getSpanId shouldBe "SPAN_ID_1"
            spanBufferWithMetadata.builder.getChildSpans(1).getSpanId shouldBe "SPAN_ID_2"
        }
        spanBufferStore.totalSpans shouldBe 0
      }
    }

    it("should create two spanBuffers for different traceIds, allow retrieving old spanBuffers from the store") {
      val (context, rootStateStore, recordCollector, spanBufferStore) = createSpanBufferStore

      whenExecuting(context, recordCollector, rootStateStore) {
        val span1 = Span.newBuilder().setTraceId(TRACE_ID_1).setSpanId("SPAN_ID_1").build()
        val span2 = Span.newBuilder().setTraceId(TRACE_ID_2).setSpanId("SPAN_ID_2").build()
        val span3 = Span.newBuilder().setTraceId(TRACE_ID_2).setSpanId("SPAN_ID_3").build()

        spanBufferStore.addOrUpdateSpanBuffer(TRACE_ID_1, span1, 11000L, 10)
        spanBufferStore.addOrUpdateSpanBuffer(TRACE_ID_2, span2, 12000L, 11)
        spanBufferStore.addOrUpdateSpanBuffer(TRACE_ID_2, span3, 12500L, 12)

        spanBufferStore.totalSpans shouldBe 3

        var result = spanBufferStore.getAndRemoveSpanBuffersOlderThan(11500L)

        result.size shouldBe 1
        result.foreach {
          spanBufferWithMetadata =>
            spanBufferWithMetadata.builder.getTraceId shouldBe TRACE_ID_1
            spanBufferWithMetadata.builder.getChildSpansCount shouldBe 1
            spanBufferWithMetadata.builder.getChildSpans(0).getSpanId shouldBe "SPAN_ID_1"
        }

        spanBufferStore.totalSpans shouldBe 2

        result = spanBufferStore.getAndRemoveSpanBuffersOlderThan(11500L)
        result.size shouldBe 0

        result = spanBufferStore.getAndRemoveSpanBuffersOlderThan(13000L)

        result.size shouldBe 1
        result.foreach {
          spanBufferWithMetadata =>
            spanBufferWithMetadata.builder.getTraceId shouldBe TRACE_ID_2
            spanBufferWithMetadata.builder.getChildSpansCount shouldBe 2
            spanBufferWithMetadata.builder.getChildSpans(0).getSpanId shouldBe "SPAN_ID_2"
            spanBufferWithMetadata.builder.getChildSpans(1).getSpanId shouldBe "SPAN_ID_3"
        }

        spanBufferStore.totalSpans shouldBe 0
      }
    }
  }

  private def createSpanBufferStore = {
    val cacheSizer = new DynamicCacheSizer(10, 1000)
    val spanBufferStore = new SpanBufferMemoryStore(cacheSizer)
    spanBufferStore.init()

    val context = mock[ProcessorContextImpl]
    val rootStateStore = mock[StateStore]
    val recordCollector: RecordCollector = mock[RecordCollector]

    expecting {
      context.applicationId().andReturn("appId").anyTimes()
      context.taskId().andReturn(new TaskId(1, 0)).anyTimes()
      context.recordCollector().andReturn(recordCollector).anyTimes()
      context.register(anyObject(classOf[StateStore]), anyBoolean(), anyObject(classOf[StateRestoreCallback])).anyTimes()
    }
    (context, rootStateStore, recordCollector, spanBufferStore)
  }
}
