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

package com.expedia.www.haystack.trace.storage.backends.cassandra.client

import java.nio.ByteBuffer
import java.util.Date

import com.datastax.driver.core._
import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.expedia.www.haystack.trace.storage.backends.cassandra.config.entities.{ClientConfiguration, KeyspaceConfiguration}
import org.slf4j.LoggerFactory
import com.expedia.www.haystack.trace.storage.backends.cassandra.client.CassandraTableSchema._

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object CassandraSession {
  private val LOGGER = LoggerFactory.getLogger(classOf[CassandraSession])

  def connect(config: ClientConfiguration,
              factory: ClusterFactory): (Cluster, Session) = this.synchronized {
    def tryConnect(): (Cluster, Session) = {
      val cluster = factory.buildCluster(config)
      Try(cluster.connect()) match {
        case Success(session) => (cluster, session)
        case Failure(e: NoHostAvailableException) =>
          LOGGER.warn("Failed to connect to cassandra. Will try again", e)
          Thread.sleep(5000)
          tryConnect()
        case Failure(e) => throw e
      }
    }

    tryConnect()
  }
}

class CassandraSession(config: ClientConfiguration, factory: ClusterFactory) {
  import CassandraSession._

  /**
    * builds a session object to interact with cassandra cluster
    * Also ensure that keyspace and table names exists in cassandra.
    */
  lazy val (cluster, session) = connect(config, factory)

  def ensureKeyspace(keyspace: KeyspaceConfiguration): Unit = {
    LOGGER.info("ensuring kespace exists with {}", keyspace)
    CassandraTableSchema.ensureExists(keyspace.name, keyspace.table, keyspace.autoCreateSchema, session)
  }

  lazy val selectRawTracesPreparedStmt: PreparedStatement = {
    import QueryBuilder.bindMarker
    session.prepare(
      QueryBuilder
        .select()
        .from(config.tracesKeyspace.name, config.tracesKeyspace.table)
        .where(QueryBuilder.in(ID_COLUMN_NAME, bindMarker(ID_COLUMN_NAME))))
  }


  def createSpanInsertPreparedStatement(keyspace: KeyspaceConfiguration): PreparedStatement = {
    import QueryBuilder.{bindMarker, ttl}

    val insert = QueryBuilder
      .insertInto(keyspace.name, keyspace.table)
      .value(ID_COLUMN_NAME, bindMarker(ID_COLUMN_NAME))
      .value(TIMESTAMP_COLUMN_NAME, bindMarker(TIMESTAMP_COLUMN_NAME))
      .value(SPANS_COLUMN_NAME, bindMarker(SPANS_COLUMN_NAME))
      .using(ttl(keyspace.recordTTLInSec))

    session.prepare(insert)
  }

  /**
    * close the session and client
    */
  def close(): Unit = {
    Try(session.close())
    Try(cluster.close())
  }


  /**
    * create bound statement for writing to cassandra table
    *
    * @param traceId              trace id
    * @param spanBufferBytes      data bytes of spanBuffer that belong to a given trace id
    * @param consistencyLevel     consistency level for cassandra write
    * @param insertTraceStatement prepared statement to use
    * @return
    */
  def newTraceInsertBoundStatement(traceId: String,
                                   spanBufferBytes: Array[Byte],
                                   consistencyLevel: ConsistencyLevel,
                                   insertTraceStatement: PreparedStatement): Statement = {
    new BoundStatement(insertTraceStatement)
      .setString(ID_COLUMN_NAME, traceId)
      .setTimestamp(TIMESTAMP_COLUMN_NAME, new Date())
      .setBytes(SPANS_COLUMN_NAME, ByteBuffer.wrap(spanBufferBytes))
      .setConsistencyLevel(consistencyLevel)
  }


  /**
    * create new select statement for retrieving Raw Traces data for traceIds
    *
    * @param traceIds list of trace id
    * @return statement for select query for traceIds
    */
  def newSelectRawTracesBoundStatement(traceIds: List[String]): Statement = {
    new BoundStatement(selectRawTracesPreparedStmt).setList(ID_COLUMN_NAME, traceIds.asJava)
  }

  /**
    * executes the statement async and return the resultset future
    *
    * @param statement prepared statement to be executed
    * @return future object of ResultSet
    */
  def executeAsync(statement: Statement): ResultSetFuture = session.executeAsync(statement)
}
