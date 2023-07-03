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

package com.expedia.www.haystack.service.graph.node.finder.app.metadata

import com.expedia.www.haystack.service.graph.node.finder.model.{ServiceNodeMetadata, SpanPair}
import org.apache.kafka.streams.processor.{Processor, ProcessorContext, ProcessorSupplier}
import org.apache.kafka.streams.state.KeyValueStore

class MetadataProducerSupplier(metadataStoreName: String) extends ProcessorSupplier[String, SpanPair] {
  override def get(): Processor[String, SpanPair] = new MetadataProducer(metadataStoreName)
}

class MetadataProducer(metadataStoreName: String) extends Processor[String, SpanPair] {
  private var context: ProcessorContext = _
  private var store: KeyValueStore[String, ServiceNodeMetadata] = _

  override def init(context: ProcessorContext): Unit = {
    this.context = context
    this.store = context.getStateStore(metadataStoreName).asInstanceOf[KeyValueStore[String, ServiceNodeMetadata]]
  }

  override def process(key: String, spanPair: SpanPair): Unit = {
    // emit the metadata only if service uses SharedSpan merge style
    if (this.store.get(spanPair.getServerSpan.serviceName) == null && spanPair.IsSharedSpan) {
      context.forward(spanPair.getServerSpan.serviceName, ServiceNodeMetadata(spanPair.IsSharedSpan))
    }
  }

  override def punctuate(timestamp: Long): Unit = ()

  override def close(): Unit = ()
}