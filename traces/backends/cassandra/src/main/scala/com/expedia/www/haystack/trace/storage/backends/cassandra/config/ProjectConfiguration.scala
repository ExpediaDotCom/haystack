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

package com.expedia.www.haystack.trace.storage.backends.cassandra.config

import com.datastax.driver.core.ConsistencyLevel
import com.expedia.www.haystack.commons.config.ConfigurationLoader
import com.expedia.www.haystack.commons.retries.RetryOperation
import com.expedia.www.haystack.trace.storage.backends.cassandra.config.entities._
import com.typesafe.config.Config
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._

class ProjectConfiguration {
  private val config = ConfigurationLoader.loadConfigFileWithEnvOverrides()

  val healthStatusFilePath: String = config.getString("health.status.path")

  val serviceConfig: ServiceConfiguration = {
    val serviceConfig = config.getConfig("service")

    val ssl = serviceConfig.getConfig("ssl")
    val sslConfig = SslConfiguration(ssl.getBoolean("enabled"), ssl.getString("cert.path"), ssl.getString("private.key.path"))

    ServiceConfiguration(serviceConfig.getInt("port"), sslConfig, serviceConfig.getInt("max.message.size"))
  }
  /**
    *
    * cassandra configuration object
    */
  val cassandraConfig: CassandraConfiguration = {

    def toConsistencyLevel(level: String) = ConsistencyLevel.values().find(_.toString.equalsIgnoreCase(level)).get

    def consistencyLevelOnErrors(cs: Config) = {
      val consistencyLevelOnErrors = cs.getStringList("on.error.consistency.level")
      val consistencyLevelOnErrorList = scala.collection.mutable.ListBuffer[(Class[_], ConsistencyLevel)]()

      var idx = 0
      while (idx < consistencyLevelOnErrors.size()) {
        val errorClass = consistencyLevelOnErrors.get(idx)
        val level = consistencyLevelOnErrors.get(idx + 1)
        consistencyLevelOnErrorList.+=((Class.forName(errorClass), toConsistencyLevel(level)))
        idx = idx + 2
      }

      consistencyLevelOnErrorList.toList
    }

    def keyspaceConfig(kConfig: Config, ttl: Int): KeyspaceConfiguration = {
      val autoCreateSchemaField = "auto.create.schema"
      val autoCreateSchema = if (kConfig.hasPath(autoCreateSchemaField)
        && StringUtils.isNotEmpty(kConfig.getString(autoCreateSchemaField))) {
        Some(kConfig.getString(autoCreateSchemaField))
      } else {
        None
      }

      KeyspaceConfiguration(kConfig.getString("name"), kConfig.getString("table.name"), ttl, autoCreateSchema)
    }

    val cs = config.getConfig("cassandra")

    val awsConfig: Option[AwsNodeDiscoveryConfiguration] =
      if (cs.hasPath("auto.discovery.aws")) {
        val aws = cs.getConfig("auto.discovery.aws")
        val tags = aws.getConfig("tags")
          .entrySet()
          .asScala
          .map(elem => elem.getKey -> elem.getValue.unwrapped().toString)
          .toMap
        Some(AwsNodeDiscoveryConfiguration(aws.getString("region"), tags))
      } else {
        None
      }

    val credentialsConfig: Option[CredentialsConfiguration] =
      if (cs.hasPath("credentials")) {
        Some(CredentialsConfiguration(cs.getString("credentials.username"), cs.getString("credentials.password")))
      } else {
        None
      }

    val socketConfig = cs.getConfig("connections")

    val socket = SocketConfiguration(
      socketConfig.getInt("max.per.host"),
      socketConfig.getBoolean("keep.alive"),
      socketConfig.getInt("conn.timeout.ms"),
      socketConfig.getInt("read.timeout.ms"))

    val consistencyLevel = toConsistencyLevel(cs.getString("consistency.level"))

    CassandraConfiguration(
      clientConfig = ClientConfiguration(
        if (cs.hasPath("endpoints")) cs.getString("endpoints").split(",").toList else Nil,
        cs.getBoolean("auto.discovery.enabled"),
        awsConfig,
        credentialsConfig,
        keyspaceConfig(cs.getConfig("keyspace"), cs.getInt("ttl.sec")),
        socket),
      consistencyLevel = consistencyLevel,
      retryConfig = RetryOperation.Config(
        cs.getInt("retries.max"),
        cs.getLong("retries.backoff.initial.ms"),
        cs.getDouble("retries.backoff.factor")),
      consistencyLevelOnErrors(cs))
  }

}
