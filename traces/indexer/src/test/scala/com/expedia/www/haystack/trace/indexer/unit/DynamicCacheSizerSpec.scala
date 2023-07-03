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

import com.expedia.www.haystack.trace.indexer.store.traits.CacheSizeObserver
import com.expedia.www.haystack.trace.indexer.store.DynamicCacheSizer
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class DynamicCacheSizerSpec extends FunSpec with Matchers with EasyMockSugar {
  private val MAX_CACHE_ENTRIES = 500

  describe("dynamic cache sizer") {
    it("should notify the cache observer with new cache size") {
      val sizer = new DynamicCacheSizer(1, MAX_CACHE_ENTRIES)
      val observer = mock[CacheSizeObserver]
      expecting {
        observer.onCacheSizeChange(MAX_CACHE_ENTRIES)
      }
      whenExecuting(observer) {
        sizer.addCacheObserver(observer)
      }
    }

    it("should notify multiple cache observers with new cache size") {
      val sizer = new DynamicCacheSizer(1, MAX_CACHE_ENTRIES)
      val observer_1 = mock[CacheSizeObserver]
      val observer_2 = mock[CacheSizeObserver]

      expecting {
        observer_1.onCacheSizeChange(MAX_CACHE_ENTRIES)
        observer_1.onCacheSizeChange(MAX_CACHE_ENTRIES / 2)
        observer_2.onCacheSizeChange(MAX_CACHE_ENTRIES / 2)
      }
      whenExecuting(observer_1, observer_2) {
        sizer.addCacheObserver(observer_1)
        sizer.addCacheObserver(observer_2)
      }
    }

    it("should notify existing cache observers when an existing observer is removed with new cache size") {
      val sizer = new DynamicCacheSizer(1, MAX_CACHE_ENTRIES)
      val observer_1 = mock[CacheSizeObserver]
      val observer_2 = mock[CacheSizeObserver]

      expecting {
        observer_1.onCacheSizeChange(MAX_CACHE_ENTRIES)
        observer_1.onCacheSizeChange(MAX_CACHE_ENTRIES / 2)
        observer_2.onCacheSizeChange(MAX_CACHE_ENTRIES / 2)
        observer_2.onCacheSizeChange(MAX_CACHE_ENTRIES)
      }
      whenExecuting(observer_1, observer_2) {
        sizer.addCacheObserver(observer_1)
        sizer.addCacheObserver(observer_2)
        sizer.removeCacheObserver(observer_1)
      }
    }
  }
}
