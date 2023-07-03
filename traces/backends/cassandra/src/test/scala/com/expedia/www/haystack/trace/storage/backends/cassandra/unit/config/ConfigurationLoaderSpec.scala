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
package com.expedia.www.haystack.trace.storage.backends.cassandra.unit.config

import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.exceptions.UnavailableException
import com.expedia.www.haystack.trace.storage.backends.cassandra.config.ProjectConfiguration
import com.expedia.www.haystack.trace.storage.backends.cassandra.config.entities.ServiceConfiguration
import com.expedia.www.haystack.trace.storage.backends.cassandra.unit.BaseUnitTestSpec

class ConfigurationLoaderSpec extends BaseUnitTestSpec {
  describe("ConfigurationLoader") {
    val project = new ProjectConfiguration()
    it("should load the service config from base.conf") {
      val serviceConfig: ServiceConfiguration = project.serviceConfig
      serviceConfig.port shouldBe 8090
      serviceConfig.ssl.enabled shouldBe false
      serviceConfig.ssl.certChainFilePath shouldBe "/ssl/cert"
      serviceConfig.ssl.privateKeyPath shouldBe "/ssl/private-key"
    }
    it("should load the cassandra config from base.conf and few properties overridden from env variable") {
      val cassandraWriteConfig = project.cassandraConfig
      val clientConfig = cassandraWriteConfig.clientConfig

      cassandraWriteConfig.consistencyLevel shouldEqual ConsistencyLevel.ONE
      clientConfig.autoDiscoverEnabled shouldBe false
      // this will fail if run inside an editor, we override this config using env variable inside pom.xml
      clientConfig.endpoints should contain allOf("cass1", "cass2")
      clientConfig.tracesKeyspace.autoCreateSchema shouldBe Some("cassandra_cql_schema_1")
      clientConfig.tracesKeyspace.name shouldBe "haystack"
      clientConfig.tracesKeyspace.table shouldBe "traces"
      clientConfig.tracesKeyspace.recordTTLInSec shouldBe 86400

      clientConfig.awsNodeDiscovery shouldBe empty
      clientConfig.socket.keepAlive shouldBe true
      clientConfig.socket.maxConnectionPerHost shouldBe 100
      clientConfig.socket.readTimeoutMills shouldBe 5000
      clientConfig.socket.connectionTimeoutMillis shouldBe 10000
      cassandraWriteConfig.retryConfig.maxRetries shouldBe 10
      cassandraWriteConfig.retryConfig.backOffInMillis shouldBe 250
      cassandraWriteConfig.retryConfig.backoffFactor shouldBe 2

      // test consistency level on error
      val writeError = new UnavailableException(ConsistencyLevel.ONE, 0, 0)
      cassandraWriteConfig.writeConsistencyLevel(writeError) shouldEqual ConsistencyLevel.ANY
      cassandraWriteConfig.writeConsistencyLevel(new RuntimeException(writeError)) shouldEqual ConsistencyLevel.ANY
      cassandraWriteConfig.writeConsistencyLevel(null) shouldEqual ConsistencyLevel.ONE
      cassandraWriteConfig.writeConsistencyLevel(new RuntimeException) shouldEqual ConsistencyLevel.ONE
    }

  }
}
