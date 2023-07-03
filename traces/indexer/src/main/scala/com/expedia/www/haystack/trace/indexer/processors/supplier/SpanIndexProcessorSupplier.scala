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

package com.expedia.www.haystack.trace.indexer.processors.supplier

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.commons.packer.Packer
import com.expedia.www.haystack.trace.indexer.config.entities.SpanAccumulatorConfiguration
import com.expedia.www.haystack.trace.indexer.processors.{SpanIndexProcessor, StreamProcessor}
import com.expedia.www.haystack.trace.indexer.store.SpanBufferMemoryStoreSupplier
import com.expedia.www.haystack.trace.indexer.writers.TraceWriter

import scala.concurrent.ExecutionContextExecutor

class SpanIndexProcessorSupplier(accumulatorConfig: SpanAccumulatorConfiguration,
                                 storeSupplier: SpanBufferMemoryStoreSupplier,
                                 writers: Seq[TraceWriter],
                                 spanBufferPacker: Packer[SpanBuffer])(implicit val dispatcher: ExecutionContextExecutor)
  extends StreamProcessorSupplier[String, Span] {

  override def get(): StreamProcessor[String, Span] = {
    new SpanIndexProcessor(accumulatorConfig, storeSupplier, writers, spanBufferPacker)
  }
}
