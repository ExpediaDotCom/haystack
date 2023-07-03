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

package com.expedia.www.haystack.trace.indexer.writers

import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.commons.packer.PackedMessage

trait TraceWriter extends AutoCloseable {

  /**
    * writes the span buffer to external store like grpc, elastic, or kafka
    * @param traceId trace id
    * @param packedSpanBuffer compressed serialized bytes of the span buffer object
    * @param isLastSpanBuffer tells if this is the last record, so the writer can flush
    */
  def writeAsync(traceId: String, packedSpanBuffer: PackedMessage[SpanBuffer], isLastSpanBuffer: Boolean)
}
