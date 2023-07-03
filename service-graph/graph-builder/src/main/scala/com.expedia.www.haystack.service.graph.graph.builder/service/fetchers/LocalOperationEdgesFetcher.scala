/*
 *
 *     Copyright 2018 Expedia, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */
package com.expedia.www.haystack.service.graph.graph.builder.service.fetchers

import com.expedia.www.haystack.commons.entities.GraphEdge
import com.expedia.www.haystack.service.graph.graph.builder.model.{EdgeStats, OperationGraphEdge}
import com.expedia.www.haystack.service.graph.graph.builder.service.utils.IOUtils
import org.apache.kafka.streams.kstream.Windowed
import org.apache.kafka.streams.state.{KeyValueIterator, QueryableStoreTypes, ReadOnlyWindowStore}
import org.apache.kafka.streams.{KafkaStreams, KeyValue}

import scala.collection.JavaConverters._

class LocalOperationEdgesFetcher(streams: KafkaStreams, storeName: String) {
  private lazy val store: ReadOnlyWindowStore[GraphEdge, EdgeStats] =
    streams.store(storeName, QueryableStoreTypes.windowStore[GraphEdge, EdgeStats]())

  def fetchEdges(from: Long, to: Long): List[OperationGraphEdge] = {
    var iterator: KeyValueIterator[Windowed[GraphEdge], EdgeStats] = null
    try {
      iterator = store.fetchAll(from, to)
      val edges = for (kv: KeyValue[Windowed[GraphEdge], EdgeStats] <- iterator.asScala)
        yield OperationGraphEdge(
          kv.key.key.source.name,
          kv.key.key.destination.name,
          kv.key.key.operation,
          kv.value,
          kv.key.window().start(),
          Math.min(System.currentTimeMillis(), kv.key.window().end()))
      edges.toList
    } finally {
      IOUtils.closeSafely(iterator)
    }
  }
}
