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
package com.expedia.www.haystack.trace.indexer.integration.serdes

import java.util

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.commons.packer.Unpacker
import org.apache.kafka.common.serialization.{Deserializer, Serializer}

class SpanProtoSerializer extends Serializer[Span] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
  override def serialize(topic: String, data: Span): Array[Byte] = {
    data.toByteArray
  }
  override def close(): Unit = ()
}

class SnappyCompressedSpanBufferProtoDeserializer extends Deserializer[SpanBuffer] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

  override def deserialize(topic: String, data: Array[Byte]): SpanBuffer = {
    if(data == null) {
      null
    } else {
      Unpacker.readSpanBuffer(data)
    }
  }

  override def close(): Unit = ()
}
