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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream

import com.expedia.open.tracing.buffer.SpanBuffer
import com.github.luben.zstd.ZstdInputStream
import org.apache.commons.io.IOUtils
import org.json4s.jackson.Serialization
import org.xerial.snappy.SnappyInputStream

object Unpacker {
  import PackedMessage._

  private def readMetadata(packedDataBytes: Array[Byte]): Array[Byte] = {
    val byteBuffer = ByteBuffer.wrap(packedDataBytes)
    val magicBytesExist = MAGIC_BYTES.indices forall { idx => byteBuffer.get() == MAGIC_BYTES.apply(idx) }
    if (magicBytesExist) {
      val headerLength = byteBuffer.getInt
      val metadataBytes = new Array[Byte](headerLength)
      byteBuffer.get(metadataBytes, 0, headerLength)
      metadataBytes
    } else {
      null
    }
  }

  private def unpack(compressedStream: InputStream) = {
    val outputStream = new ByteArrayOutputStream()
    IOUtils.copy(compressedStream, outputStream)
    outputStream.toByteArray
  }

  def readSpanBuffer(packedDataBytes: Array[Byte]): SpanBuffer = {
    var parsedDataBytes: Array[Byte] = null
    val metadataBytes = readMetadata(packedDataBytes)
    if (metadataBytes != null) {
      val packedMetadata = Serialization.read[PackedMetadata](new String(metadataBytes))
      val compressedDataOffset = MAGIC_BYTES.length + 4 + metadataBytes.length
      packedMetadata.t match {
        case PackerType.SNAPPY =>
          parsedDataBytes = unpack(
            new SnappyInputStream(
              new ByteArrayInputStream(packedDataBytes, compressedDataOffset, packedDataBytes.length - compressedDataOffset)))
        case PackerType.GZIP =>
          parsedDataBytes = unpack(
            new GZIPInputStream(
              new ByteArrayInputStream(packedDataBytes, compressedDataOffset, packedDataBytes.length - compressedDataOffset)))
        case PackerType.ZSTD =>
          parsedDataBytes = unpack(
            new ZstdInputStream(
              new ByteArrayInputStream(packedDataBytes, compressedDataOffset, packedDataBytes.length - compressedDataOffset)))
        case _ =>
          return SpanBuffer.parseFrom(
            new ByteArrayInputStream(packedDataBytes, compressedDataOffset, packedDataBytes.length - compressedDataOffset))
      }
    } else {
      parsedDataBytes = packedDataBytes
    }
    SpanBuffer.parseFrom(parsedDataBytes)
  }
}
