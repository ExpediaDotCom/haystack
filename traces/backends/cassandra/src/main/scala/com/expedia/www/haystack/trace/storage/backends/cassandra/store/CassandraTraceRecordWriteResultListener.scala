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

import com.codahale.metrics.{Meter, Timer}
import com.datastax.driver.core.ResultSetFuture
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.commons.retries.RetryOperation
import com.expedia.www.haystack.trace.storage.backends.cassandra.metrics.AppMetricNames
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object CassandraTraceRecordWriteResultListener extends MetricsSupport {
  protected val LOGGER: Logger = LoggerFactory.getLogger(CassandraTraceRecordWriteResultListener.getClass)
  protected val writeFailures: Meter = metricRegistry.meter(AppMetricNames.CASSANDRA_WRITE_FAILURE)
  protected val writeWarnings: Meter = metricRegistry.meter(AppMetricNames.CASSANDRA_WRITE_WARNINGS)
}

class CassandraTraceRecordWriteResultListener(asyncResult: ResultSetFuture,
                                              timer: Timer.Context,
                                              retryOp: RetryOperation.Callback) extends Runnable {

  import CassandraTraceRecordWriteResultListener._

  /**
    * this is invoked when the cassandra aysnc write completes.
    * We measure the time write operation takes and records any warnings or errors
    */
  override def run(): Unit = {
    try {
      timer.close()

      val result = asyncResult.get()
      if (result != null &&
        result.getExecutionInfo != null &&
        result.getExecutionInfo.getWarnings != null &&
        !result.getExecutionInfo.getWarnings.isEmpty) {
        LOGGER.warn(s"Warning received in cassandra writes {}", result.getExecutionInfo.getWarnings.asScala.mkString(","))
        writeWarnings.mark(result.getExecutionInfo.getWarnings.size())
      }
      if (retryOp != null) retryOp.onResult(result)
    } catch {
      case ex: Exception =>
        LOGGER.error("Fail to write the record to cassandra with exception", ex)
        writeFailures.mark()
        if (retryOp != null) retryOp.onError(ex, retry = true)
    }
  }
}