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

package com.expedia.www.haystack.trace.storage.backends.cassandra.store

import java.util.concurrent.atomic.AtomicInteger

import com.expedia.open.tracing.backend.TraceRecord
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.commons.retries.RetryOperation._
import com.expedia.www.haystack.trace.storage.backends.cassandra.client.CassandraSession
import com.expedia.www.haystack.trace.storage.backends.cassandra.config.entities.CassandraConfiguration
import com.expedia.www.haystack.trace.storage.backends.cassandra.metrics.AppMetricNames
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success}

class CassandraTraceRecordWriter(cassandra: CassandraSession,
                                 config: CassandraConfiguration)(implicit val dispatcher: ExecutionContextExecutor)
  extends MetricsSupport {

  private val LOGGER = LoggerFactory.getLogger(classOf[CassandraTraceRecordWriter])
  private lazy val writeTimer = metricRegistry.timer(AppMetricNames.CASSANDRA_WRITE_TIME)
  private lazy val writeFailures = metricRegistry.meter(AppMetricNames.CASSANDRA_WRITE_FAILURE)

  cassandra.ensureKeyspace(config.clientConfig.tracesKeyspace)
  private val spanInsertPreparedStmt = cassandra.createSpanInsertPreparedStatement(config.clientConfig.tracesKeyspace)

  private def execute(record: TraceRecord): Future[Unit] = {

    val promise = Promise[Unit]
    // execute the request async with retry
    withRetryBackoff(retryCallback => {
      val timer = writeTimer.time()

      // prepare the statement
      val statement = cassandra.newTraceInsertBoundStatement(record.getTraceId,
        record.getSpans.toByteArray,
        config.writeConsistencyLevel(retryCallback.lastError()),
        spanInsertPreparedStmt)

      val asyncResult = cassandra.executeAsync(statement)
      asyncResult.addListener(new CassandraTraceRecordWriteResultListener(asyncResult, timer, retryCallback), dispatcher)
    },
      config.retryConfig,
      onSuccess = (_: Any) => promise.success(),
      onFailure = ex => {
        writeFailures.mark()
        LOGGER.error(s"Fail to write to cassandra after ${config.retryConfig.maxRetries} retry attempts for ${record.getTraceId}", ex)
        promise.failure(ex)
      })
    promise.future
  }

  /**
    * writes the traceId and its spans to cassandra. Use the current timestamp as the sort key for the writes to same
    * TraceId. Also if the parallel writes exceed the max inflight requests, then we block and this puts backpressure on
    * upstream
    *
    * @param traceRecords : trace records which need to be written
    * @return
    */
  def writeTraceRecords(traceRecords: List[TraceRecord]): Future[Unit] = {
    val promise = Promise[Unit]
    val writableRecordsLatch = new AtomicInteger(traceRecords.size)
    traceRecords.foreach(record => {
      /* write spanBuffer for a given traceId */
      execute(record).onComplete {
        case Success(_) => if (writableRecordsLatch.decrementAndGet() == 0) {
          promise.success()
        }
        case Failure(ex) =>
          //TODO: We fail the response only if the last cassandra write fails, ideally we should be failing if any of the cassandra writes fail
          if (writableRecordsLatch.decrementAndGet() == 0) {
            promise.failure(ex)
          }
      }
    })
    promise.future

  }
}
