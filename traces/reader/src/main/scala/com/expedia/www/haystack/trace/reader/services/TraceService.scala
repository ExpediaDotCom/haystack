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

package com.expedia.www.haystack.trace.reader.services

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.api._
import com.expedia.www.haystack.trace.reader.config.entities.{TraceTransformersConfiguration, TraceValidatorsConfiguration}
import com.expedia.www.haystack.trace.reader.readers.TraceReader
import com.expedia.www.haystack.trace.reader.stores.TraceStore
import io.grpc.stub.StreamObserver

import scala.concurrent.ExecutionContextExecutor

class TraceService(traceStore: TraceStore,
                   validatorsConfig: TraceValidatorsConfiguration,
                   transformersConfig: TraceTransformersConfiguration)
                  (implicit val executor: ExecutionContextExecutor) extends TraceReaderGrpc.TraceReaderImplBase {

  private val handleGetTraceResponse = new GrpcHandler(TraceReaderGrpc.METHOD_GET_TRACE.getFullMethodName)
  private val handleGetRawTraceResponse = new GrpcHandler(TraceReaderGrpc.METHOD_GET_RAW_TRACE.getFullMethodName)
  private val handleGetRawSpanResponse = new GrpcHandler(TraceReaderGrpc.METHOD_GET_RAW_SPAN.getFullMethodName)
  private val handleSearchResponse = new GrpcHandler(TraceReaderGrpc.METHOD_SEARCH_TRACES.getFullMethodName)
  private val handleFieldNamesResponse = new GrpcHandler(TraceReaderGrpc.METHOD_GET_FIELD_NAMES.getFullMethodName)
  private val handleFieldValuesResponse = new GrpcHandler(TraceReaderGrpc.METHOD_GET_FIELD_VALUES.getFullMethodName)
  private val handleTraceCallGraphResponse = new GrpcHandler(TraceReaderGrpc.METHOD_GET_TRACE_CALL_GRAPH.getFullMethodName)
  private val traceReader = new TraceReader(traceStore, validatorsConfig, transformersConfig)

  /**
    * endpoint for fetching a trace
    * trace will be validated and transformed
    *
    * @param request          TraceRequest object containing traceId of the trace to fetch
    * @param responseObserver response observer will contain Trace object
    *                         or will error out with [[com.expedia.www.haystack.trace.reader.exceptions.TraceNotFoundException]]
    */
  override def getTrace(request: TraceRequest, responseObserver: StreamObserver[Trace]): Unit = {
    handleGetTraceResponse.handle(request, responseObserver) {
      traceReader.getTrace(request)
    }
  }

  /**
    * endpoint for fetching raw trace logs, trace will returned without validations and transformations
    *
    * @param request          TraceRequest object containing traceId of the trace to fetch
    * @param responseObserver response observer will stream out [[Trace]] object
    *                         or will error out with [[com.expedia.www.haystack.trace.reader.exceptions.TraceNotFoundException]]
    */
  override def getRawTrace(request: TraceRequest, responseObserver: StreamObserver[Trace]): Unit = {
    handleGetRawTraceResponse.handle(request, responseObserver) {
      traceReader.getRawTrace(request)
    }
  }

  /**
    * endpoint for fetching raw span logs, span will returned without validations and transformations
    *
    * @param request          SpanRequest object containing spanId and parent traceId of the span to fetch
    * @param responseObserver response observer will stream out [[Span]] object
    *                         or will error out with [[com.expedia.www.haystack.trace.reader.exceptions.SpanNotFoundException]]
    */
  override def getRawSpan(request: SpanRequest, responseObserver: StreamObserver[SpanResponse]): Unit = {
    handleGetRawSpanResponse.handle(request, responseObserver) {
      traceReader.getRawSpan(request)
    }
  }

  /**
    * endpoint for searching traces
    *
    * @param request          TracesSearchRequest object containing criteria and filters for traces to find
    * @param responseObserver response observer will stream out [[List[Trace]]
    */
  override def searchTraces(request: TracesSearchRequest, responseObserver: StreamObserver[TracesSearchResult]): Unit = {
    handleSearchResponse.handle(request, responseObserver) {
      traceReader.searchTraces(request)
    }
  }

  /**
    * get list of field names available in indexing system
    *
    * @param request          empty request object
    * @param responseObserver response observer will contain list of field names
    */
  override def getFieldNames(request: Empty, responseObserver: StreamObserver[FieldNames]): Unit = {
    handleFieldNamesResponse.handle(request, responseObserver) {
      traceReader.getFieldNames
    }
  }

  /**
    * get list of possible field values for a given field
    *
    * @param request          contains field name and other field name-value pairs to be used as filters
    * @param responseObserver response observer will contain list of field values for filter condition
    */
  override def getFieldValues(request: FieldValuesRequest, responseObserver: StreamObserver[FieldValues]): Unit = {
    handleFieldValuesResponse.handle(request, responseObserver) {
      traceReader.getFieldValues(request)
    }
  }

  override def getTraceCallGraph(request: TraceRequest, responseObserver: StreamObserver[TraceCallGraph]): Unit = {
    handleTraceCallGraphResponse.handle(request, responseObserver) {
      traceReader.getTraceCallGraph(request)
    }
  }

  override def getTraceCounts(request: TraceCountsRequest, responseObserver: StreamObserver[TraceCounts]): Unit = {
    handleTraceCallGraphResponse.handle(request, responseObserver) {
      traceReader.getTraceCounts(request)
    }
  }

  override def getRawTraces(request: RawTracesRequest, responseObserver: StreamObserver[RawTracesResult]): Unit = {
    handleTraceCallGraphResponse.handle(request, responseObserver) {
      traceReader.getRawTraces(request)
    }
  }
}
