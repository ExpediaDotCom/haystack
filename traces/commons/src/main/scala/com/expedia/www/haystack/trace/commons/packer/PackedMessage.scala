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

package com.expedia.www.haystack.trace.commons.packer

import java.nio.ByteBuffer

import com.google.protobuf.GeneratedMessageV3
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats}

object PackedMessage {
  implicit val formats: Formats = DefaultFormats + new org.json4s.ext.EnumSerializer(PackerType)
  val MAGIC_BYTES: Array[Byte] = "hytc".getBytes("utf-8")
}

case class PackedMessage[T <: GeneratedMessageV3](protoObj: T,
                                                  private val pack: (T => Array[Byte]),
                                                  private val metadata: PackedMetadata) {
  import PackedMessage._
  private lazy val metadataBytes: Array[Byte] = Serialization.write(metadata).getBytes("utf-8")

  val packedDataBytes: Array[Byte] = {
    val packedDataBytes = pack(protoObj)
    if (PackerType.NONE == metadata.t) {
      packedDataBytes
    } else {
      ByteBuffer
        .allocate(MAGIC_BYTES.length + 4 + metadataBytes.length + packedDataBytes.length)
        .put(MAGIC_BYTES)
        .putInt(metadataBytes.length)
        .put(metadataBytes)
        .put(packedDataBytes).array()
    }
  }
}
