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

package com.expedia.www.haystack.trace.storage.backends.cassandra.integration

import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.{Date, UUID}

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{Cluster, ResultSet, Session, SimpleStatement}
import com.expedia.open.tracing.Span
import com.expedia.open.tracing.backend.{StorageBackendGrpc, TraceRecord}
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.storage.backends.cassandra.Service
import com.expedia.www.haystack.trace.storage.backends.cassandra.client.CassandraTableSchema
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.health.v1.HealthGrpc
import org.scalatest._

import scala.collection.JavaConverters._

trait BaseIntegrationTestSpec extends FunSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  protected var client: StorageBackendGrpc.StorageBackendBlockingStub = _

  protected var healthCheckClient: HealthGrpc.HealthBlockingStub = _
  private val CASSANDRA_ENDPOINT = "cassandra"
  private val CASSANDRA_KEYSPACE = "haystack"
  private val CASSANDRA_TABLE = "traces"

  private val executors = Executors.newSingleThreadExecutor()

  private var cassandraSession: Session = _

  override def beforeAll() {
    executors.submit(new Runnable {
      override def run(): Unit = Service.main(null)
    })
    //waiting for the service to start up

    Thread.sleep(5000)
    // setup cassandra
    cassandraSession = Cluster
      .builder()
      .addContactPoints(CASSANDRA_ENDPOINT)
      .build()
      .connect(CASSANDRA_KEYSPACE)
    deleteCassandraTableRows()
    client = StorageBackendGrpc.newBlockingStub(ManagedChannelBuilder.forAddress("localhost", 8090)
      .usePlaintext(true)
      .build())

    healthCheckClient = HealthGrpc.newBlockingStub(ManagedChannelBuilder.forAddress("localhost", 8090)
      .usePlaintext(true)
      .build())
  }

  private def deleteCassandraTableRows(): Unit = {
    cassandraSession.execute(new SimpleStatement(s"TRUNCATE $CASSANDRA_TABLE"))
  }

  protected def putTraceInCassandra(traceId: String = UUID.randomUUID().toString,
                                    spanId: String = UUID.randomUUID().toString,
                                    serviceName: String = "",
                                    operationName: String = "",
                                    tags: Map[String, String] = Map.empty,
                                    startTime: Long = System.currentTimeMillis() * 1000,
                                    sleep: Boolean = true): Unit = {
    insertTraceInCassandra(traceId, spanId, serviceName, operationName, tags, startTime)
    // wait for few sec to let ES refresh its index
    if (sleep) Thread.sleep(5000)
  }

  protected def createTraceRecord(traceId: String = UUID.randomUUID().toString,
                                 ): TraceRecord = {
    val spans = "random span".getBytes
    TraceRecord
      .newBuilder()
      .setTraceId(traceId)
      .setTimestamp(System.currentTimeMillis())
      .setSpans(ByteString.copyFrom(spans)).build()
  }

  private def insertTraceInCassandra(traceId: String,
                                     spanId: String,
                                     serviceName: String,
                                     operationName: String,
                                     tags: Map[String, String],
                                     startTime: Long): ResultSet = {
    val spanBuffer = createSpanBufferWithSingleSpan(traceId, spanId, serviceName, operationName, tags, startTime)
    writeToCassandra(spanBuffer, traceId)
  }

  private def writeToCassandra(spanBuffer: SpanBuffer, traceId: String) = {

    cassandraSession.execute(QueryBuilder
      .insertInto(CASSANDRA_TABLE)
      .value(CassandraTableSchema.ID_COLUMN_NAME, traceId)
      .value(CassandraTableSchema.TIMESTAMP_COLUMN_NAME, new Date())
      .value(CassandraTableSchema.SPANS_COLUMN_NAME, ByteBuffer.wrap(spanBuffer.toByteArray)))
  }

  private def createSpanBufferWithSingleSpan(traceId: String,
                                             spanId: String,
                                             serviceName: String,
                                             operationName: String,
                                             tags: Map[String, String],
                                             startTime: Long) = {
    val spanTags = tags.map(tag => com.expedia.open.tracing.Tag.newBuilder().setKey(tag._1).setVStr(tag._2).build())

    SpanBuffer
      .newBuilder()
      .setTraceId(traceId)
      .addChildSpans(Span
        .newBuilder()
        .setTraceId(traceId)
        .setSpanId(spanId)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .setStartTime(startTime)
        .addAllTags(spanTags.asJava)
        .build())
      .build()
  }
}
