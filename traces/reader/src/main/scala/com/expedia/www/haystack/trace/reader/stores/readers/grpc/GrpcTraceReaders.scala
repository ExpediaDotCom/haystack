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

package com.expedia.www.haystack.trace.reader.stores.readers.grpc

import com.expedia.open.tracing.api.Trace
import com.expedia.open.tracing.backend.{ReadSpansRequest, StorageBackendGrpc}
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trace.commons.config.entities.TraceStoreBackends
import com.expedia.www.haystack.trace.reader.exceptions.TraceNotFoundException
import com.expedia.www.haystack.trace.reader.metrics.AppMetricNames
import com.expedia.www.haystack.trace.reader.readers.utils.TraceMerger
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

class GrpcTraceReaders(config: TraceStoreBackends)
                      (implicit val dispatcher: ExecutionContextExecutor) extends MetricsSupport with AutoCloseable {
  private val LOGGER = LoggerFactory.getLogger(classOf[GrpcTraceReaders])

  private val readTimer = metricRegistry.timer(AppMetricNames.BACKEND_READ_TIME)
  private val readFailures = metricRegistry.meter(AppMetricNames.BACKEND_READ_FAILURES)
  private val tracesFailures = metricRegistry.meter(AppMetricNames.BACKEND_TRACES_FAILURE)

  private val clients: Seq[GrpcChannelClient] =  config.backends.map {
    backend => {
      val channel = ManagedChannelBuilder
        .forAddress(backend.host, backend.port)
        .usePlaintext(true)
        .build()

      val client = StorageBackendGrpc.newFutureStub(channel)
      GrpcChannelClient(channel, client)
    }
  }

  def readTraces(traceIds: List[String]): Future[Seq[Trace]] = {
    val allFutures = clients.map {
      client =>
        readTraces(traceIds, client.stub) recoverWith  {
          case _: Exception => Future.successful(Seq.empty[Trace])
        }
    }

    Future.sequence(allFutures)
      .map(traceSeq => traceSeq.flatten)
      .map {
        traces =>
          if (traces.isEmpty) throw new TraceNotFoundException() else TraceMerger.merge(traces)
      }
  }

  private def readTraces(traceIds: List[String], client: StorageBackendGrpc.StorageBackendFutureStub): Future[Seq[Trace]] = {
    val timer = readTimer.time()
    val promise = Promise[Seq[Trace]]

    try {
      val readSpansRequest = ReadSpansRequest.newBuilder().addAllTraceIds(traceIds.asJavaCollection).build()
      val futureResponse = client.readSpans(readSpansRequest)
      futureResponse.addListener(new ReadSpansResponseListener(
        futureResponse,
        promise,
        timer,
        readFailures,
        tracesFailures,
        traceIds.size), dispatcher)

      // return the future with the results for the given client
      promise.future
    } catch {
      case ex: Exception =>
        readFailures.mark()
        timer.stop()
        LOGGER.error("Failed to read raw traces with exception", ex)
        Future.failed(ex)
    }
  }

  override def close(): Unit = {
    clients.foreach(_.channel.shutdown())
  }

  case class GrpcChannelClient(channel: ManagedChannel, stub: StorageBackendGrpc.StorageBackendFutureStub)
}
