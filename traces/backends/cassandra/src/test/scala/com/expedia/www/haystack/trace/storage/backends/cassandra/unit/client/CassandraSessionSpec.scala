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

package com.expedia.www.haystack.trace.storage.backends.cassandra.unit.client

import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.{Insert, Select}
import com.expedia.www.haystack.trace.storage.backends.cassandra.client.{CassandraClusterFactory, CassandraSession}
import com.expedia.www.haystack.trace.storage.backends.cassandra.config.entities.{ClientConfiguration, KeyspaceConfiguration, SocketConfiguration}
import org.easymock.EasyMock
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class CassandraSessionSpec extends FunSpec with Matchers with EasyMockSugar {
  describe("Cassandra Session") {
    it("should connect to the cassandra cluster and provide prepared statement for inserts") {
      val keyspaceName = "keyspace-1"
      val tableName = "table-1"

      val factory = mock[CassandraClusterFactory]
      val session = mock[Session]
      val cluster = mock[Cluster]
      val metadata = mock[Metadata]
      val keyspaceMetadata = mock[KeyspaceMetadata]
      val tableMetadata = mock[TableMetadata]
      val insertPrepStatement = mock[PreparedStatement]
      val keyspaceConfig = KeyspaceConfiguration(keyspaceName, tableName, 100, None)

      val config = ClientConfiguration(List("cassandra1"),
        autoDiscoverEnabled = false,
        None,
        None,
        keyspaceConfig,
        SocketConfiguration(10, keepAlive = true, 1000, 1000))

      val captured = EasyMock.newCapture[Insert.Options]()
      expecting {
        factory.buildCluster(config).andReturn(cluster).once()
        cluster.connect().andReturn(session).once()
        keyspaceMetadata.getTable(tableName).andReturn(tableMetadata).once()
        metadata.getKeyspace(keyspaceName).andReturn(keyspaceMetadata).once()
        cluster.getMetadata.andReturn(metadata).once()
        session.getCluster.andReturn(cluster).once()
        session.prepare(EasyMock.capture(captured)).andReturn(insertPrepStatement).anyTimes()
        session.close().once()
        cluster.close().once()
      }

      whenExecuting(factory, cluster, session, metadata, keyspaceMetadata, tableMetadata, insertPrepStatement) {
        val session = new CassandraSession(config, factory)
        session.ensureKeyspace(config.tracesKeyspace)
        val stmt = session.createSpanInsertPreparedStatement(keyspaceConfig)
        stmt shouldBe insertPrepStatement
        captured.getValue.getQueryString() shouldEqual "INSERT INTO \"keyspace-1\".\"table-1\" (id,ts,spans) VALUES (:id,:ts,:spans) USING TTL 100;"
        session.close()
      }
    }

    it("should connect to the cassandra cluster and provide prepared statement for select with traces") {
      val keyspaceName = "keyspace-1"
      val tableName = "table-1"

      val factory = mock[CassandraClusterFactory]
      val session = mock[Session]
      val cluster = mock[Cluster]
      val metadata = mock[Metadata]
      val keyspaceMetadata = mock[KeyspaceMetadata]
      val tableMetadata = mock[TableMetadata]
      val selectPrepStatement = mock[PreparedStatement]
      val keyspaceConfig = KeyspaceConfiguration(keyspaceName, tableName, 100, None)

      val config = ClientConfiguration(List("cassandra1"),
        autoDiscoverEnabled = false,
        None,
        None,
        keyspaceConfig,
        SocketConfiguration(10, keepAlive = true, 1000, 1000))

      val captured = EasyMock.newCapture[Select.Where]()
      expecting {
        factory.buildCluster(config).andReturn(cluster).once()
        cluster.connect().andReturn(session).once()
        keyspaceMetadata.getTable(tableName).andReturn(tableMetadata).once()
        metadata.getKeyspace(keyspaceName).andReturn(keyspaceMetadata).once()
        cluster.getMetadata.andReturn(metadata).once()
        session.getCluster.andReturn(cluster).once()
        session.prepare(EasyMock.capture(captured)).andReturn(selectPrepStatement).anyTimes()
        session.close().once()
        cluster.close().once()
      }

      whenExecuting(factory, cluster, session, metadata, keyspaceMetadata, tableMetadata, selectPrepStatement) {
        val session = new CassandraSession(config, factory)
        session.ensureKeyspace(config.tracesKeyspace)
        val stmt = session.selectRawTracesPreparedStmt
        stmt shouldBe selectPrepStatement
        captured.getValue.getQueryString() shouldEqual "SELECT * FROM \"keyspace-1\".\"table-1\" WHERE id IN :id;"
        session.close()
      }
    }
  }
}
