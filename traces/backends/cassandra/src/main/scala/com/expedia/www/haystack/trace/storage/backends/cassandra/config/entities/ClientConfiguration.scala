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

package com.expedia.www.haystack.trace.storage.backends.cassandra.config.entities

import com.datastax.driver.core.ConsistencyLevel
import com.expedia.www.haystack.commons.retries.RetryOperation
import org.apache.commons.lang3.StringUtils


/** define the keyspace and table information in cassandra
  *
  * @param name              : name of cassandra keyspace
  * @param table             : name of cassandra table
  * @param recordTTLInSec    : ttl of record in sec
  * @param autoCreateSchema  : apply cql and create keyspace and tables if not exist, optional
  */
case class KeyspaceConfiguration(name: String,
                                 table: String,
                                 recordTTLInSec: Int = -1,
                                 autoCreateSchema: Option[String] = None) {
  require(StringUtils.isNotEmpty(name))
  require(StringUtils.isNotEmpty(table))
}

/**
  * defines the configuration parameters for cassandra client
  *
  * @param endpoints                : list of cassandra endpoints
  * @param autoDiscoverEnabled      : if autodiscovery is enabled, then 'endpoints' config parameter will be ignored
  * @param awsNodeDiscovery         : discovery configuration for aws, optional. This is applied only if autoDiscoverEnabled is true
  * @param tracesKeyspace           : cassandra keyspace for traces
  * @param socket                   : socket configuration like maxConnections, timeouts and keepAlive
  */
case class ClientConfiguration(endpoints: List[String],
                               autoDiscoverEnabled: Boolean,
                               awsNodeDiscovery: Option[AwsNodeDiscoveryConfiguration],
                               plaintextCredentials: Option[CredentialsConfiguration],
                               tracesKeyspace: KeyspaceConfiguration,
                               socket: SocketConfiguration)

/**
  * @param consistencyLevel: consistency level of writes
  * @param retryConfig retry configuration if writes fail
  * @param consistencyLevelOnError: downgraded consistency level on write error
  */
case class CassandraConfiguration(clientConfig: ClientConfiguration,
                                  consistencyLevel: ConsistencyLevel,
                                  retryConfig: RetryOperation.Config,
                                  consistencyLevelOnError: List[(Class[_], ConsistencyLevel)]) {
  def writeConsistencyLevel(error: Throwable): ConsistencyLevel = {
    if (error == null) {
      consistencyLevel
    } else {
      consistencyLevelOnError
        .find(errorClass => errorClass._1.isAssignableFrom(error.getClass))
        .map(_._2).getOrElse(writeConsistencyLevel(error.getCause))
    }
  }
}
