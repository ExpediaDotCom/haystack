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

package com.expedia.www.haystack.trace.reader.integration

import java.util.UUID

import com.expedia.open.tracing.api.ExpressionTree.Operator
import com.expedia.open.tracing.api._
import com.expedia.www.haystack.trace.commons.clients.es.document.TraceIndexDoc
import io.grpc.health.v1.{HealthCheckRequest, HealthCheckResponse}
import io.grpc.{Status, StatusRuntimeException}

import scala.collection.JavaConverters._
import scala.collection.mutable

class TraceServiceIntegrationTestSpec extends BaseIntegrationTestSpec {

  describe("TraceReader.getFieldNames") {
    it("should return names of enabled fields") {
      Given("trace in trace-backend and elasticsearch")
      val field1 = FieldWithMetadata("abc", isRangeQuery = true)
      val field2 = FieldWithMetadata("def", isRangeQuery = false)
      putWhitelistIndexFieldsInEs(List(field1, field2))

      When("calling getFieldNames")
      val fieldNames = client.getFieldNames(Empty.newBuilder().build())

      Then("should return fieldNames available in index")
      fieldNames.getNamesCount should be(2)
      fieldNames.getFieldMetadataCount should be(2)
      fieldNames.getNamesList.asScala.toList should contain allOf("abc", "def")
    }
  }

  describe("TraceReader.getFieldValues") {
    it("should return values of a given fields") {
      Given("trace in trace-backend and elasticsearch")
      val serviceName = "get_values_servicename"
      putTraceInEsAndTraceBackend(UUID.randomUUID().toString, UUID.randomUUID().toString, serviceName, "op")
      val request = FieldValuesRequest.newBuilder()
        .setFieldName(TraceIndexDoc.SERVICE_KEY_NAME)
        .build()

      When("calling getFieldValues")
      val result = client.getFieldValues(request)

      Then("should return possible values for given field")
      result.getValuesList.asScala should contain(serviceName)
    }

    it("should return values of a given fields with filters") {
      Given("trace in trace-backend and elasticsearch")
      val serviceName = "get_values_with_filters_servicename"
      val op1 = "get_values_with_filters_operationname_1"
      val op2 = "get_values_with_filters_operationname_2"

      putTraceInEsAndTraceBackend(UUID.randomUUID().toString, UUID.randomUUID().toString, serviceName, op1)
      putTraceInEsAndTraceBackend(UUID.randomUUID().toString, UUID.randomUUID().toString, serviceName, op2)
      putTraceInEsAndTraceBackend(UUID.randomUUID().toString, UUID.randomUUID().toString, "non_matching_servicename", "non_matching_operationname")

      val request = FieldValuesRequest.newBuilder()
        .addFilters(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName))
        .setFieldName(TraceIndexDoc.OPERATION_KEY_NAME)
        .build()

      When("calling getFieldValues")
      val result = client.getFieldValues(request)

      Then("should return filtered values for given field")
      result.getValuesList.size() should be(2)
      result.getValuesList.asScala should contain allOf(op1, op2)
    }
  }

  describe("TraceReader.getTrace") {
    it("should get trace for given traceID from trace-backend") {
      Given("trace in trace-backend")
      val traceId = UUID.randomUUID().toString
      putTraceInBackend(traceId)

      When("getTrace is invoked")
      val trace = client.getTrace(TraceRequest.newBuilder().setTraceId(traceId).build())

      Then("should return the trace")
      trace.getTraceId shouldBe traceId
    }

    it("should return TraceNotFound exception if traceID is not in trace-backend") {
      Given("trace in trace-backend")
      putTraceInBackend(UUID.randomUUID().toString)

      When("getTrace is invoked")
      val thrown = the[StatusRuntimeException] thrownBy {
        client.getTrace(TraceRequest.newBuilder().setTraceId(UUID.randomUUID().toString).build())
      }

      Then("thrown StatusRuntimeException should have 'not found' error")
      thrown.getStatus.getCode should be(Status.NOT_FOUND.getCode)
      thrown.getStatus.getDescription should include("traceId not found")
    }
  }

  describe("TraceReader.getRawTrace") {
    it("should get trace for given traceID from trace-backend") {
      Given("trace in trace-backend")
      val traceId = UUID.randomUUID().toString
      putTraceInBackend(traceId)

      When("getRawTrace is invoked")
      val trace = client.getRawTrace(TraceRequest.newBuilder().setTraceId(traceId).build())

      Then("should return the trace")
      trace.getTraceId shouldBe traceId
    }

    it("should return TraceNotFound exception if traceID is not in trace-backend") {
      Given("trace in trace-backend")
      putTraceInBackend(UUID.randomUUID().toString)

      When("getRawTrace is invoked")
      val thrown = the[StatusRuntimeException] thrownBy {
        client.getRawTrace(TraceRequest.newBuilder().setTraceId(UUID.randomUUID().toString).build())
      }

      Then("thrown StatusRuntimeException should have 'not found' error")
      thrown.getStatus.getCode should be(Status.NOT_FOUND.getCode)
      thrown.getStatus.getDescription should include("traceId not found")
    }
  }

  describe("TraceReader.getRawSpan") {
    it("should get spanId for given traceID-spanId from trace-backend") {
      Given("trace in trace-backend")
      val traceId = UUID.randomUUID().toString
      val spanId = UUID.randomUUID().toString
      putTraceInBackend(traceId, spanId, "svc1")
      putTraceInBackend(traceId, spanId, "svc2")


      When("getRawSpan is invoked")
      val spanResponse = client.getRawSpan(SpanRequest
        .newBuilder()
        .setTraceId(traceId)
        .setSpanId(spanId)
        .build())

      Then("should return the trace")
      spanResponse.getSpansCount shouldBe 2
      val servicesObserved = mutable.Set[String]()
      spanResponse.getSpansList.asScala foreach { span =>
        span.getTraceId shouldBe traceId
        span.getSpanId shouldBe spanId
        servicesObserved += span.getServiceName
      }
      servicesObserved should contain allOf("svc1", "svc2")
    }

    it("should return TraceNotFound exception if traceID is not in trace-backend") {
      Given("trace in trace-backend")
      putTraceInBackend(UUID.randomUUID().toString)

      When("getRawSpan is invoked")
      val thrown = the[StatusRuntimeException] thrownBy {
        client.getRawSpan(SpanRequest
          .newBuilder()
          .setTraceId(UUID.randomUUID().toString)
          .setSpanId(UUID.randomUUID().toString)
          .build())
      }

      Then("thrown StatusRuntimeException should have 'traceId not found' error")
      thrown.getStatus.getCode should be(Status.NOT_FOUND.getCode)
      thrown.getStatus.getDescription should include("traceId not found")
    }

    it("should return SpanNotFound exception if spanId is not part of Trace") {
      Given("trace in trace-backend")
      val traceId = UUID.randomUUID().toString
      putTraceInBackend(traceId)

      When("getRawSpan is invoked")
      val thrown = the[StatusRuntimeException] thrownBy {
        client.getRawSpan(SpanRequest
          .newBuilder()
          .setTraceId(traceId)
          .setSpanId(UUID.randomUUID().toString)
          .build())
      }

      Then("thrown StatusRuntimeException should have 'spanId not found' error")
      thrown.getStatus.getCode should be(Status.NOT_FOUND.getCode)
      thrown.getStatus.getDescription should include("spanId not found")
    }
  }

  describe("TraceReader.searchTraces") {
    it("should search traces for given service and operation") {
      Given("trace in trace-backend and elasticsearch")
      val traceId = UUID.randomUUID().toString
      val spanId = UUID.randomUUID().toString
      val serviceName = "svcName"
      val operationName = "opName"
      val startTime = 1
      val endTime = (System.currentTimeMillis() + 10000000) * 1000
      putTraceInEsAndTraceBackend(traceId, spanId, serviceName, operationName)

      When("searching traces for service and operation")
      val traces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .addFields(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName).build())
        .addFields(Field.newBuilder().setName(TraceIndexDoc.OPERATION_KEY_NAME).setValue(operationName).build())
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build())

      Then("should return traces for the searched service and operation name")
      traces.getTracesList.size() should be > 0
      traces.getTraces(0).getTraceId shouldBe traceId
      traces.getTraces(0).getChildSpans(0).getServiceName shouldBe serviceName
      traces.getTraces(0).getChildSpans(0).getOperationName shouldBe operationName
    }

    it("should search traces for given service") {
      Given("traces in trace-backend and elasticsearch")
      val traceId1 = UUID.randomUUID().toString
      val traceId2 = UUID.randomUUID().toString
      val serviceName = "serviceToSearch"
      val operationName = "opName"
      val startTime = 1
      val endTime = (System.currentTimeMillis() + 10000000) * 1000
      putTraceInEsAndTraceBackend(traceId1, UUID.randomUUID().toString, serviceName, operationName)
      putTraceInEsAndTraceBackend(traceId2, UUID.randomUUID().toString, serviceName, operationName)

      When("searching traces for service")
      val traces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .addFields(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName).build())
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build())

      Then("should return all traces for the service")
      traces.getTracesList.size() should be(2)
      traces.getTracesList.asScala.exists(_.getTraceId == traceId1) shouldBe true
      traces.getTracesList.asScala.exists(_.getTraceId == traceId2) shouldBe true
    }

    it("should not return traces for unavailable searches") {
      Given("traces in trace-backend and elasticsearch")

      When("searching traces for service")
      val traces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .addFields(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue("unavailableService").build())
        .setStartTime(1)
        .setEndTime((System.currentTimeMillis() + 10000000) * 1000)
        .setLimit(10)
        .build())

      Then("should not return traces")
      traces.getTracesList.size() should be(0)
    }

    it("should search traces for given whitelisted tags") {
      Given("traces with tags in trace-backend and elasticsearch")
      val traceId = UUID.randomUUID().toString
      val serviceName = "svcWhitelisteTags"
      val operationName = "opWhitelisteTags"
      val tags = Map("aKey" -> "aValue", "bKey" -> "bValue")
      val startTime = 1
      val endTime = (System.currentTimeMillis() + 10000000) * 1000
      putTraceInEsAndTraceBackend(traceId, UUID.randomUUID().toString, serviceName, operationName, tags)

      When("searching traces for tags")
      val traces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .addFields(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName).build())
        .addFields(Field.newBuilder().setName("akey").setValue("avalue").build())
        .addFields(Field.newBuilder().setName("bkey").setValue("bvalue").build())
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build())

      Then("should return traces having tags")
      traces.getTracesList.asScala.exists(_.getTraceId == traceId) shouldBe true
    }

    it("should not return traces if tags are not available") {
      Given("traces with tags in trace-backend and elasticsearch")
      val traceId = UUID.randomUUID().toString
      val serviceName = "svcWhitelisteTags"
      val operationName = "opWhitelisteTags"
      val tags = Map("cKey" -> "cValue", "dKey" -> "dValue")
      val startTime = 1
      val endTime = (System.currentTimeMillis() + 10000000) * 1000
      putTraceInEsAndTraceBackend(traceId, UUID.randomUUID().toString, serviceName, operationName, tags)

      When("searching traces for tags")
      val traces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .addFields(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName).build())
        .addFields(Field.newBuilder().setName("ckey").setValue("cvalue").build())
        .addFields(Field.newBuilder().setName("akey").setValue("avalue").build())
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build())

      Then("should not return traces")
      traces.getTracesList.asScala.exists(_.getTraceId == traceId) shouldBe false
    }

    it("should return traces for expression tree based search targeting ") {
      Given("traces with tags in trace-backend and elasticsearch")
      val traceId = UUID.randomUUID().toString
      val serviceName = "expressionTraceSvc"
      val operationName = "expressionTraceOp"
      val tags = Map("uKey" -> "uValue", "vKey" -> "vValue")
      val startTime = 1
      val endTime = (System.currentTimeMillis() + 10000000) * 1000
      putTraceInEsAndTraceBackend(traceId, UUID.randomUUID().toString, serviceName, operationName, tags)

      When("searching traces for tags using expression tree")
      val expression = ExpressionTree
        .newBuilder()
        .setOperator(Operator.AND)
        .addOperands(Operand.newBuilder().setField(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName)))

      val traces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .setFilterExpression(expression)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build())

      Then("should return traces")
      traces.getTracesList.asScala.exists(_.getTraceId == traceId) shouldBe true
    }

    it("should not return traces for expression tree using not_equal operator based search") {
      Given("traces with tags in trace-backend and elasticsearch")
      val traceId = UUID.randomUUID().toString
      val serviceName = "expressionTraceSvc"
      val operationName = "expressionTraceOp"
      val tags = Map("uKey" -> "uValue", "vKey" -> "vValue")
      val startTime = 1
      val endTime = (System.currentTimeMillis() + 10000000) * 1000
      putTraceInEsAndTraceBackend(traceId, UUID.randomUUID().toString, serviceName, operationName, tags)

      When("searching traces for tags using expression tree")
      val expression = ExpressionTree
        .newBuilder()
        .setOperator(Operator.AND)
        .addOperands(Operand.newBuilder().setField(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName)))
        .addOperands(Operand.newBuilder().setField(Field.newBuilder().setName(TraceIndexDoc.OPERATION_KEY_NAME).setValue(operationName).setOperator(Field.Operator.NOT_EQUAL)))

      val traces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .setFilterExpression(expression)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build())

      Then("should not return traces")
      traces.getTracesList.size() shouldBe 0
    }

    it("should return traces for expression tree using not_equal operator based search") {
      Given("traces with tags in trace-backend and elasticsearch")
      val traceId = UUID.randomUUID().toString
      val serviceName = "expressionTraceSvc_1"
      val operationName = "expressionTraceOp_1"
      val tags = Map("uKey" -> "uValue", "vKey" -> "vValue")
      val startTime = 1
      val endTime = (System.currentTimeMillis() + 10000000) * 1000
      putTraceInEsAndTraceBackend(traceId, UUID.randomUUID().toString, serviceName, operationName, tags)

      When("searching traces for tags using expression tree")
      val expression = ExpressionTree
        .newBuilder()
        .setOperator(Operator.AND)
        .addOperands(Operand.newBuilder().setField(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName)))
        .addOperands(Operand.newBuilder().setField(Field.newBuilder().setName(TraceIndexDoc.OPERATION_KEY_NAME).setValue("somethingelse").setOperator(Field.Operator.NOT_EQUAL)))

      val traces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .setFilterExpression(expression)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build())

      Then("should return traces")
      traces.getTracesList.asScala.exists(_.getTraceId == traceId) shouldBe true
    }

    it("should return traces for expression tree with duration filter") {
      Given("traces with tags in trace-backend and elasticsearch")
      val traceId = UUID.randomUUID().toString
      val serviceName = "expressionTraceSvc_1"
      val operationName = "expressionTraceOp_1"
      val tags = Map("uKey" -> "uValue", "vKey" -> "vValue")
      val startTime = 1
      val endTime = (System.currentTimeMillis() + 10000000) * 1000
      putTraceInEsAndTraceBackend(traceId, UUID.randomUUID().toString, serviceName, operationName, tags)

      When("searching traces for tags using expression tree")
      val baseExpr = ExpressionTree
        .newBuilder()
        .setOperator(Operator.AND)
        .setIsSpanLevelExpression(true)
        .addOperands(Operand.newBuilder().setField(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName)))

      val greaterThanExpr = baseExpr.addOperands(Operand.newBuilder().setField(Field.newBuilder().setName(TraceIndexDoc.DURATION_KEY_NAME).setOperator(Field.Operator.GREATER_THAN).setValue("300000")))
      val nonEmptyTraces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .setFilterExpression(greaterThanExpr)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build())

      val lessThanExpr = baseExpr.addOperands(Operand.newBuilder().setField(Field.newBuilder().setName(TraceIndexDoc.DURATION_KEY_NAME).setOperator(Field.Operator.LESS_THAN).setValue("300000")))
      val emptyTraces = client.searchTraces(TracesSearchRequest
        .newBuilder()
        .setFilterExpression(lessThanExpr)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build())

      Then("should return traces")
      nonEmptyTraces.getTracesList.asScala.exists(_.getTraceId == traceId) shouldBe true
      emptyTraces.getTracesList shouldBe empty
    }
  }

  describe("TraceReader.getTraceCallGraph") {
    it("should get trace  for given traceID from trace-backend") {
      Given("trace in trace-backend")
      val traceId = "traceId"
      putTraceInBackendWithPartialSpans(traceId)

      When("getTrace is invoked")
      val traceCallGraph = client.getTraceCallGraph(TraceRequest.newBuilder().setTraceId(traceId).build())

      Then("should return the trace call graph")
      traceCallGraph.getCallsCount should be(3)
    }
  }

  describe("TraceReader.getTraceCounts") {
    it("should return trace counts histogram for given time span") {
      Given("traces elasticsearch")
      val serviceName = "dummy-servicename"
      val operationName = "dummy-operationname"
      val currentTimeMicros = System.currentTimeMillis() * 1000l

      val bucketIntervalInMicros = 10l * 1000 * 10000
      val bucketCount = 4
      val randomStartTimes = 0 until bucketCount map (idx => currentTimeMicros - (bucketIntervalInMicros * idx))
      val startTimeInMicroSec = currentTimeMicros - (bucketIntervalInMicros * bucketCount)
      val endTimeInMicroSec = currentTimeMicros

      randomStartTimes.foreach(startTime =>
        putTraceInEsAndTraceBackend(serviceName = serviceName, operationName = operationName, startTime = startTime, sleep = false))
      Thread.sleep(5000)

      When("calling getTraceCounts")
      val traceCountsRequest = TraceCountsRequest
        .newBuilder()
        .addFields(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName).build())
        .addFields(Field.newBuilder().setName(TraceIndexDoc.OPERATION_KEY_NAME).setValue(operationName).build())
        .setStartTime(startTimeInMicroSec)
        .setEndTime(endTimeInMicroSec)
        .setInterval(bucketIntervalInMicros)
        .build()

      val traceCounts = client.getTraceCounts(traceCountsRequest)

      Then("should return possible values for given field")
      traceCounts.getTraceCountCount shouldEqual bucketCount
      traceCounts.getTraceCountList.asScala.foreach(_.getCount shouldBe 1)
    }
  }

  describe("TraceReader.getRawTraces") {
    it("should get raw traces for given traceIds from trace-backend") {
      Given("traces in trace-backend")
      val traceId1 = UUID.randomUUID().toString
      val spanId1 = UUID.randomUUID().toString
      val spanId2 = UUID.randomUUID().toString
      putTraceInBackend(traceId1, spanId1, "svc1", "oper1")
      putTraceInBackend(traceId1, spanId2, "svc2", "oper2")

      val traceId2 = UUID.randomUUID().toString
      val spanId3 = UUID.randomUUID().toString
      putTraceInBackend(traceId2, spanId3, "svc1", "oper1")

      When("getRawTraces is invoked")
      val tracesResult = client.getRawTraces(RawTracesRequest.newBuilder().addAllTraceId(Seq(traceId1, traceId2).asJava).build())

      Then("should return the traces")
      val traceIdSpansMap: Map[String, Set[String]] = tracesResult.getTracesList.asScala
        .map(trace => trace.getTraceId -> trace.getChildSpansList.asScala.map(_.getSpanId).toSet).toMap

      traceIdSpansMap(traceId1) shouldEqual Set(spanId1, spanId2)
      traceIdSpansMap(traceId2) shouldEqual Set(spanId3)
    }
  }

  describe("TraceReader.HealthCheck") {
    it("should return SERVING as health check response") {
      val request = HealthCheckRequest.newBuilder().build()
      val response = healthCheckClient.check(request)
      response.getStatus shouldEqual HealthCheckResponse.ServingStatus.SERVING
    }
  }
}
