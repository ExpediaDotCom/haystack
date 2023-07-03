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

import com.expedia.open.tracing.backend.{ReadSpansRequest, TraceRecord, WriteSpansRequest}
import com.google.protobuf.ByteString

class InMemoryTraceBackendServiceIntegrationTestSpec extends BaseIntegrationTestSpec {


  describe("In Memory Persistence Service read trace records") {
    it("should get trace records for given traceID from in memory") {
      Given("trace-record ")
      val traceId = UUID.randomUUID().toString
      val serializedSpans =  createSerializedSpanBuffer(traceId)
      val traceRecord = TraceRecord.newBuilder()
        .setTraceId(traceId)
        .setSpans(ByteString.copyFrom(serializedSpans))
        .setTimestamp(System.currentTimeMillis())
        .build()

      When("write span is invoked")
      val writeSpanRequest = WriteSpansRequest.newBuilder().addRecords(traceRecord).build()
     val response =  client.writeSpans(writeSpanRequest)

      Then("should be able to retrieve the trace-record back")
      val readSpansResponse =  client.readSpans(ReadSpansRequest.newBuilder().addTraceIds(traceId).build())
      readSpansResponse.getRecordsCount shouldBe 1
      readSpansResponse.getRecordsCount shouldEqual  1
      readSpansResponse.getRecordsList.get(0).getTraceId shouldEqual traceId
    }
  }
}
