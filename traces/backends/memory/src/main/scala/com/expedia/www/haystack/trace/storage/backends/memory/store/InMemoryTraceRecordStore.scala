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

package com.expedia.www.haystack.trace.storage.backends.memory.store

import com.expedia.open.tracing.backend.TraceRecord
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

class InMemoryTraceRecordStore()
                              (implicit val dispatcher: ExecutionContextExecutor) extends MetricsSupport with AutoCloseable {
  private val LOGGER = LoggerFactory.getLogger(classOf[InMemoryTraceRecordStore])


  private var inMemoryTraceRecords = Map[String, List[TraceRecord]]()

  def readTraceRecords(traceIds: List[String]): Seq[TraceRecord] = {

    try {
      traceIds.flatMap(traceId => {
        inMemoryTraceRecords.getOrElse(traceId, List())
      })
    } catch {
      case ex: Exception =>
        LOGGER.error("Failed to read raw traces with exception", ex)
        List()
    }
  }

  /**
    * writes the traceId and its spans to a in. Use the current timestamp as the sort key for the writes to same
    * TraceId. Also if the parallel writes exceed the max inflight requests, then we block and this puts backpressure on
    * upstream
    *
    * @param traceRecords : trace records which need to be written
    * @return
    */
  def writeTraceRecords(traceRecords: List[TraceRecord]): Unit = {


    traceRecords.foreach(record => {

      try {
        val existingRecords: List[TraceRecord] = inMemoryTraceRecords.getOrElse(record.getTraceId, List())
        val records = record :: existingRecords
        inMemoryTraceRecords = inMemoryTraceRecords + (record.getTraceId -> records)
      } catch {
        case ex: Exception =>
          LOGGER.error("Fail to write the spans to memory with exception", ex)

      }
    })
  }

  override def close(): Unit = ()
}
