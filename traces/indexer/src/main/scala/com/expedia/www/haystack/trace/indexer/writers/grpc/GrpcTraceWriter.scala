package com.expedia.www.haystack.trace.indexer.writers.grpc

/*
 *  Copyright 2018 Expedia, Group.
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


import java.util.concurrent.Semaphore

import com.expedia.open.tracing.backend.{StorageBackendGrpc, TraceRecord, WriteSpansRequest}
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trace.commons.packer.PackedMessage
import com.expedia.www.haystack.trace.indexer.config.entities.TraceBackendConfiguration
import com.expedia.www.haystack.trace.indexer.metrics.AppMetricNames
import com.expedia.www.haystack.trace.indexer.writers.TraceWriter
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

class GrpcTraceWriter(config: TraceBackendConfiguration)(implicit val dispatcher: ExecutionContextExecutor)
  extends TraceWriter with MetricsSupport {

  private val LOGGER = LoggerFactory.getLogger(classOf[GrpcTraceWriter])
  private val writeTimer = metricRegistry.timer(AppMetricNames.BACKEND_WRITE_TIME)
  private val writeFailures = metricRegistry.meter(AppMetricNames.BACKEND_WRITE_FAILURE)

  private val channel =  {
    val grpcConfig = config.clientConfig.backends.head
    ManagedChannelBuilder.forAddress(grpcConfig.host, grpcConfig.port)
      .usePlaintext(true)
      .build()
  }
  private val client = StorageBackendGrpc.newStub(channel)

  // this semaphore controls the parallel writes to trace-backend
  private val inflightRequestsSemaphore = new Semaphore(config.maxInFlightRequests, true)

  private def execute(traceId: String, packedSpanBuffer: PackedMessage[SpanBuffer]): Unit = {
    val timer = writeTimer.time()
    val singleRecord = TraceRecord
      .newBuilder()
      .setTraceId(traceId)
      .setTimestamp(System.currentTimeMillis())
      .setSpans(ByteString.copyFrom(packedSpanBuffer.packedDataBytes))
    val writeSpansRequest = WriteSpansRequest.newBuilder().addRecords(singleRecord).build()

    // execute the request async with retry
    client.writeSpans(writeSpansRequest, new WriteSpansResponseObserver(timer, inflightRequestsSemaphore))
  }

  /**
    * writes the traceId and its spans to trace-backend. Use the current timestamp as the sort key for the writes to same
    * TraceId. Also if the parallel writes exceed the max inflight requests, then we block and this puts backpressure on
    * upstream
    *
    * @param traceId          : trace id
    * @param packedSpanBuffer : list of spans belonging to this traceId - span buffer
    * @param isLastSpanBuffer tells if this is the last record, so the writer can flush``
    * @return
    */
  override def writeAsync(traceId: String, packedSpanBuffer: PackedMessage[SpanBuffer], isLastSpanBuffer: Boolean): Unit = {
    var isSemaphoreAcquired = false

    try {
      inflightRequestsSemaphore.acquire()
      isSemaphoreAcquired = true
      /* write spanBuffer for a given traceId */
      execute(traceId, packedSpanBuffer)
    } catch {
      case ex: Exception =>
        LOGGER.error("Fail to write the spans to trace-backend with exception", ex)
        writeFailures.mark()
        if (isSemaphoreAcquired) inflightRequestsSemaphore.release()
    }
  }

  override def close(): Unit = {
    LOGGER.info("Closing backend client now..")
    Try(channel.shutdown())
  }
}
