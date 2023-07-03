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

package com.expedia.www.haystack.trace.commons.unit

import java.util.UUID

import com.expedia.open.tracing.{Span, Tag}
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.commons.packer.{PackerFactory, PackerType, Unpacker}
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.easymock.EasyMockSugar

class PackerSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("A Packer") {
    it("should pack and unpack spanBuffer proto object for all packer types") {
      PackerType.values.foreach(packerType => {
        val packer = PackerFactory.spanBufferPacker(packerType)
        val span_1 = Span.newBuilder()
          .setTraceId(UUID.randomUUID().toString)
          .setSpanId(UUID.randomUUID().toString)
          .setServiceName("test_service")
          .setOperationName("/foo")
          .addTags(Tag.newBuilder().setKey("error").setVBool(false))
          .addTags(Tag.newBuilder().setKey("http.status_code").setVLong(200))
          .addTags(Tag.newBuilder().setKey("version").setVStr("1.1"))
          .build()
        val span_2 = Span.newBuilder()
          .setTraceId(UUID.randomUUID().toString)
          .setSpanId(UUID.randomUUID().toString)
          .setParentSpanId(UUID.randomUUID().toString)
          .setServiceName("another_test_service")
          .setOperationName("/bar")
          .addTags(Tag.newBuilder().setKey("error").setVBool(true))
          .addTags(Tag.newBuilder().setKey("http.status_code").setVLong(404))
          .addTags(Tag.newBuilder().setKey("version").setVStr("1.2"))
          .build()
        val spanBuffer = SpanBuffer.newBuilder().setTraceId("trace-1").addChildSpans(span_1).addChildSpans(span_2).build()
        val packedMessage = packer.apply(spanBuffer)
        val packedDataBytes = packedMessage.packedDataBytes
        packedDataBytes should not be null
        val spanBufferProto = Unpacker.readSpanBuffer(packedDataBytes)
        spanBufferProto shouldEqual spanBuffer
      })
    }
  }
}
