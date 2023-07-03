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

import com.expedia.www.haystack.service.graph.node.finder.model.ServiceNodeMetadata
import org.apache.kafka.streams.processor.{Processor, ProcessorContext, ProcessorSupplier}
import org.apache.kafka.streams.state.KeyValueStore

import scala.util.Try

class MetadataStoreUpdateProcessorSupplier(storeName: String) extends ProcessorSupplier[String, ServiceNodeMetadata] {
  override def get(): Processor[String, ServiceNodeMetadata] = new MetadataStoreUpdateProcessor(storeName)
}

class MetadataStoreUpdateProcessor(storeName: String) extends Processor[String, ServiceNodeMetadata] {
  private var store: KeyValueStore[String, ServiceNodeMetadata] = _

  override def init(context: ProcessorContext): Unit = {
    store = context.getStateStore(storeName).asInstanceOf[KeyValueStore[String, ServiceNodeMetadata]]
  }

  override def process(key: String, value: ServiceNodeMetadata): Unit = {
    store.put(key, value)
  }

  override def punctuate(timestamp: Long): Unit = ()

  override def close(): Unit = {
    Try(store.close())
  }
}