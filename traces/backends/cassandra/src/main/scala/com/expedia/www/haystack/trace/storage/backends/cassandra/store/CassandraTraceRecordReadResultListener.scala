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

package com.expedia.www.haystack.trace.storage.backends.cassandra.store

import com.codahale.metrics.{Meter, Timer}
import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.datastax.driver.core.{ResultSet, ResultSetFuture, Row}
import com.expedia.open.tracing.api.Trace
import com.expedia.open.tracing.backend.TraceRecord
import com.expedia.www.haystack.trace.storage.backends.cassandra.client.CassandraTableSchema
import com.google.protobuf.ByteString
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

object CassandraTraceRecordReadResultListener {
  protected val LOGGER: Logger = LoggerFactory.getLogger(classOf[CassandraTraceRecordReadResultListener])
}

class CassandraTraceRecordReadResultListener(asyncResult: ResultSetFuture,
                                             timer: Timer.Context,
                                             failure: Meter,
                                             promise: Promise[Seq[TraceRecord]]) extends Runnable {

  import CassandraTraceRecordReadResultListener._

  override def run(): Unit = {
    timer.close()

    Try(asyncResult.get)
      .flatMap(tryGetTraceRows)
      .flatMap(mapTraceRecords)
    match {
      case Success(records) =>
        promise.success(records)
      case Failure(ex) =>
        if (fatalError(ex)) {
          LOGGER.error("Fatal error in reading from cassandra, tearing down the app", ex)
        } else {
          LOGGER.error("Failed in reading the record from cassandra", ex)
        }
        failure.mark()
        promise.failure(ex)
    }
  }

  private def fatalError(ex: Throwable): Boolean = {
    if (ex.isInstanceOf[NoHostAvailableException]) true else ex.getCause != null && fatalError(ex.getCause)
  }

  private def tryGetTraceRows(resultSet: ResultSet): Try[Seq[Row]] = {
    val rows = resultSet.all().asScala
    if (rows.isEmpty) Failure(new RuntimeException()) else Success(rows)
  }

  private def mapTraceRecords(rows: Seq[Row]): Try[List[TraceRecord]] = {
    Try {
      rows.map(row => {
        val spanBytes = row.getBytes(CassandraTableSchema.SPANS_COLUMN_NAME).array()
        val timeStamp = row.getLong(CassandraTableSchema.TIMESTAMP_COLUMN_NAME)
        val traceId = row.getString(CassandraTableSchema.ID_COLUMN_NAME)
        val record = TraceRecord.newBuilder()
          .setSpans(ByteString.copyFrom(spanBytes))
          .setTimestamp(timeStamp)
          .setTraceId(traceId)
          .build()
        record
      }).toList
    }
  }
}
