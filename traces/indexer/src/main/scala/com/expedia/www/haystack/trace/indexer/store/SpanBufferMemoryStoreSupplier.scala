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

package com.expedia.www.haystack.trace.indexer.store

import com.expedia.www.haystack.trace.indexer.store.impl.SpanBufferMemoryStore
import com.expedia.www.haystack.trace.indexer.store.traits.SpanBufferKeyValueStore

class SpanBufferMemoryStoreSupplier(minTracesPerCache: Int,
                                    maxEntriesAcrossStores: Int)
  extends StoreSupplier[SpanBufferKeyValueStore] {

  private val dynamicCacheSizer = new DynamicCacheSizer(minTracesPerCache, maxEntriesAcrossStores)

  /**
    * @return kv store for maintaining buffered-spans. If logging is enabled, we persist the changelog to kafka topic
    *         else it is purely in-memory
    */
  override def get(): SpanBufferKeyValueStore = new SpanBufferMemoryStore(dynamicCacheSizer)
}
