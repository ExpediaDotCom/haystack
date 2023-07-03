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

import java.util.concurrent.Future

import com.codahale.metrics.{Meter, Timer}
import com.expedia.open.tracing.api.Trace
import com.expedia.open.tracing.backend.{ReadSpansResponse, TraceRecord}
import com.expedia.www.haystack.trace.commons.packer.Unpacker
import com.expedia.www.haystack.trace.reader.exceptions.TraceNotFoundException
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

object ReadSpansResponseListener {
  protected val LOGGER: Logger = LoggerFactory.getLogger(classOf[ReadSpansResponseListener])
}

class ReadSpansResponseListener(readSpansResponse: Future[ReadSpansResponse],
                                promise: Promise[Seq[Trace]],
                                timer: Timer.Context,
                                failure: Meter,
                                tracesFailure: Meter,
                                traceIdCount: Int) extends Runnable {

  import ReadSpansResponseListener._

  override def run(): Unit = {
    timer.close()

    Try(readSpansResponse.get)
      .flatMap(tryGetTraceRows)
      .flatMap(tryDeserialize)
    match {
      case Success(traces) =>
        tracesFailure.mark(traceIdCount - traces.length)
        promise.success(traces)
      case Failure(ex) =>
        LOGGER.error("Failed in reading the record from trace-backend", ex)
        failure.mark()
        tracesFailure.mark(traceIdCount)
        promise.failure(ex)
    }
  }

  private def tryGetTraceRows(response: ReadSpansResponse): Try[Seq[TraceRecord]] = {
    val records = response.getRecordsList
    if (records.isEmpty) Failure(new TraceNotFoundException) else Success(records.asScala)
  }

  private def tryDeserialize(records: Seq[TraceRecord]): Try[Seq[Trace]] = {
    val traceBuilderMap = new mutable.HashMap[String, Trace.Builder]()
    var deserFailed: Failure[Seq[Trace]] = null

    records.foreach(record => {
      Try(Unpacker.readSpanBuffer(record.getSpans.toByteArray)) match {
        case Success(sBuffer) =>
          traceBuilderMap.getOrElseUpdate(sBuffer.getTraceId, Trace.newBuilder().setTraceId(sBuffer.getTraceId)).addAllChildSpans(sBuffer.getChildSpansList)
        case Failure(cause) => deserFailed = Failure(cause)
      }
    })
    if (deserFailed == null) Success(traceBuilderMap.values.map(_.build).toSeq) else deserFailed
  }
}
