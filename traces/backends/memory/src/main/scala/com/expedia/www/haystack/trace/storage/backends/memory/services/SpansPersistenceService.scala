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

package com.expedia.www.haystack.trace.storage.backends.memory.services

import com.expedia.open.tracing.backend.WriteSpansResponse.ResultCode
import com.expedia.open.tracing.backend._
import com.expedia.www.haystack.trace.storage.backends.memory.store.InMemoryTraceRecordStore
import io.grpc.stub.StreamObserver

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContextExecutor

class SpansPersistenceService(store: InMemoryTraceRecordStore)
                             (implicit val executor: ExecutionContextExecutor) extends StorageBackendGrpc.StorageBackendImplBase {


  override def writeSpans(request: WriteSpansRequest, responseObserver: StreamObserver[WriteSpansResponse]): Unit = {
    store.writeTraceRecords(request.getRecordsList.asScala.toList)
    val response =  WriteSpansResponse.newBuilder().setCode(
      ResultCode.SUCCESS
    ).build()
    responseObserver.onNext(response)
    responseObserver.onCompleted()
  }

  /**
    * <pre>
    * read buffered spans from backend
    * </pre>
    */
  override def readSpans(request: ReadSpansRequest, responseObserver: StreamObserver[ReadSpansResponse]): Unit = {

    val records = store.readTraceRecords(request.getTraceIdsList.iterator().asScala.toList)
    val response = ReadSpansResponse.newBuilder()
      .addAllRecords(records.asJava)
      .build()
    responseObserver.onNext(response)
    responseObserver.onCompleted()
  }
}
