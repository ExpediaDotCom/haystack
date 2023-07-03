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

package com.expedia.www.haystack.trace.indexer.store.impl

import java.util
import java.util.concurrent.atomic.AtomicInteger

import com.codahale.metrics.Meter
import com.expedia.open.tracing.Span
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trace.indexer.metrics.AppMetricNames._
import com.expedia.www.haystack.trace.indexer.store.DynamicCacheSizer
import com.expedia.www.haystack.trace.indexer.store.data.model.SpanBufferWithMetadata
import com.expedia.www.haystack.trace.indexer.store.traits.{CacheSizeObserver, EldestBufferedSpanEvictionListener, SpanBufferKeyValueStore}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

object SpanBufferMemoryStore extends MetricsSupport {
  protected val LOGGER: Logger = LoggerFactory.getLogger(SpanBufferMemoryStore.getClass)
  protected val evictionMeter: Meter = metricRegistry.meter(STATE_STORE_EVICTION)
}

class SpanBufferMemoryStore(cacheSizer: DynamicCacheSizer) extends SpanBufferKeyValueStore with CacheSizeObserver {
  import SpanBufferMemoryStore._

  @volatile protected var open = false

  // This maxEntries will be adjusted by the dynamic cacheSizer, lets default it to a reasonable value 10000
  protected val maxEntries = new AtomicInteger(10000)
  private val listeners: mutable.ListBuffer[EldestBufferedSpanEvictionListener] = mutable.ListBuffer()
  private var totalSpansInMemStore: Int = 0
  private var map: util.LinkedHashMap[String, SpanBufferWithMetadata] = _

  override def init() {
    cacheSizer.addCacheObserver(this)

    // initialize the map
    map = new util.LinkedHashMap[String, SpanBufferWithMetadata](cacheSizer.minTracesPerCache, 1.01f, false) {
      override protected def removeEldestEntry(eldest: util.Map.Entry[String, SpanBufferWithMetadata]): Boolean = {
        val evict = totalSpansInMemStore >= maxEntries.get()
        if (evict) {
          evictionMeter.mark()
          totalSpansInMemStore -= eldest.getValue.builder.getChildSpansCount
          listeners.foreach(listener => listener.onEvict(eldest.getKey, eldest.getValue))
        }
        evict
      }
    }

    open = true

    LOGGER.info("Span buffer memory store has been initialized")
  }

  /**
    * removes and returns all the span buffers from the map that are recorded before the given timestamp
    *
    * @param timestamp timestamp before which all buffered spans should be read and removed
    * @return
    */
  override def getAndRemoveSpanBuffersOlderThan(timestamp: Long): mutable.ListBuffer[SpanBufferWithMetadata] = {
    val result = mutable.ListBuffer[SpanBufferWithMetadata]()

    val iterator = this.map.entrySet().iterator()
    var done = false

    while (!done && iterator.hasNext) {
      val el = iterator.next()
      if (el.getValue.firstSpanSeenAt <= timestamp) {
        iterator.remove()
        totalSpansInMemStore -= el.getValue.builder.getChildSpansCount
        result += el.getValue
      } else {
        // here we apply a basic optimization and skip further iteration because all following records
        // in this map will have higher recordTimestamp. When we insert the first span for a unique traceId
        // in the map, we set the 'firstRecordTimestamp' attribute with record's timestamp
        done = true
      }
    }
    result
  }

  override def addEvictionListener(l: EldestBufferedSpanEvictionListener): Unit = this.listeners += l

  override def close(): Unit = {
    if(open) {
      LOGGER.info("Closing the span buffer memory store")
      cacheSizer.removeCacheObserver(this)
      open = false
    }
  }

  def onCacheSizeChange(maxEntries: Int): Unit = {
    LOGGER.info("Cache size has been changed to " + maxEntries)
    this.maxEntries.set(maxEntries)
  }

  override def addOrUpdateSpanBuffer(traceId: String, span: Span, spanRecordTimestamp: Long, offset: Long): SpanBufferWithMetadata = {
    var value = this.map.get(traceId)
    if (value == null) {
      val spanBuffer = SpanBuffer.newBuilder().setTraceId(span.getTraceId).addChildSpans(span)
      value = SpanBufferWithMetadata(spanBuffer, spanRecordTimestamp, offset)
      this.map.put(traceId, value)
    } else {
      value.builder.addChildSpans(span)
    }
    totalSpansInMemStore += 1
    value
  }

  def totalSpans: Int = totalSpansInMemStore
}
