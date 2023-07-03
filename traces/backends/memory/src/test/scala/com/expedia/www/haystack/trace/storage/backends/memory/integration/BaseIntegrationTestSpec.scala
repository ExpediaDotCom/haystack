/*
 *  Copyright 2017 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */

package com.expedia.www.haystack.trace.storage.backends.memory.integration

import java.util.UUID
import java.util.concurrent.Executors

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.backend.StorageBackendGrpc
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.storage.backends.memory.Service
import io.grpc.ManagedChannelBuilder
import org.scalatest._

import scala.collection.JavaConverters._

trait BaseIntegrationTestSpec extends FunSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with BeforeAndAfterEach  {
  protected var client: StorageBackendGrpc.StorageBackendBlockingStub = _


  private val executors = Executors.newSingleThreadExecutor()


  override def beforeAll() {




    executors.submit(new Runnable {
      override def run(): Unit = Service.main(null)
    })

    Thread.sleep(5000)

    client = StorageBackendGrpc.newBlockingStub(ManagedChannelBuilder.forAddress("localhost", 8090)
      .usePlaintext(true)
      .build())
  }

  protected def createSerializedSpanBuffer(traceId: String = UUID.randomUUID().toString,
                                           spanId: String = UUID.randomUUID().toString,
                                           serviceName: String = "test-service",
                                           operationName: String = "test-operation",
                                           tags: Map[String, String] = Map.empty,
                                           startTime: Long = System.currentTimeMillis() * 1000,
                                           sleep: Boolean = true): Array[Byte] = {
    val spanBuffer = createSpanBufferWithSingleSpan(traceId, spanId, serviceName, operationName, tags, startTime)
    spanBuffer.toByteArray
  }

  private def createSpanBufferWithSingleSpan(traceId: String,
                                             spanId: String,
                                             serviceName: String,
                                             operationName: String,
                                             tags: Map[String, String],
                                             startTime: Long) = {
    val spanTags = tags.map(tag => com.expedia.open.tracing.Tag.newBuilder().setKey(tag._1).setVStr(tag._2).build())

    SpanBuffer
      .newBuilder()
      .setTraceId(traceId)
      .addChildSpans(Span
        .newBuilder()
        .setTraceId(traceId)
        .setSpanId(spanId)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .setStartTime(startTime)
        .addAllTags(spanTags.asJava)
        .build())
      .build()
  }
}
