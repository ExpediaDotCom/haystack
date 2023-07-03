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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStream}
import java.util.zip.GZIPOutputStream

import com.expedia.www.haystack.trace.commons.packer.PackerType.PackerType
import com.github.luben.zstd.ZstdOutputStream
import com.google.protobuf.GeneratedMessageV3
import org.apache.commons.io.IOUtils
import org.xerial.snappy.SnappyOutputStream

object PackerType extends Enumeration {
  type PackerType = Value
  val GZIP, SNAPPY, NONE, ZSTD = Value
}

case class PackedMetadata(t: PackerType)

abstract class Packer[T <: GeneratedMessageV3] {
  val packerType: PackerType

  protected def compressStream(stream: OutputStream): OutputStream

  private def pack(protoObj: T): Array[Byte] = {
    val outStream = new ByteArrayOutputStream
    val compressedStream = compressStream(outStream)
    if (compressedStream != null) {
      IOUtils.copy(new ByteArrayInputStream(protoObj.toByteArray), compressedStream)
      compressedStream.close() // this flushes the data to final outStream
      outStream.toByteArray
    } else {
      protoObj.toByteArray
    }
  }

  def apply(protoObj: T): PackedMessage[T] = {
    PackedMessage(protoObj, pack, PackedMetadata(packerType))
  }
}

class NoopPacker[T <: GeneratedMessageV3] extends Packer[T] {
  override val packerType = PackerType.NONE
  override protected def compressStream(stream: OutputStream): OutputStream = null
}

class SnappyPacker[T <: GeneratedMessageV3] extends Packer[T] {
  override val packerType = PackerType.SNAPPY
  override protected def compressStream(stream: OutputStream): OutputStream = new SnappyOutputStream(stream)
}


class ZstdPacker[T <: GeneratedMessageV3] extends Packer[T] {
  override val packerType = PackerType.ZSTD
  override protected def compressStream(stream: OutputStream): OutputStream = new ZstdOutputStream(stream)
}

class GzipPacker[T <: GeneratedMessageV3] extends Packer[T] {
  override val packerType = PackerType.GZIP
  override protected def compressStream(stream: OutputStream): OutputStream = new GZIPOutputStream(stream)
}
