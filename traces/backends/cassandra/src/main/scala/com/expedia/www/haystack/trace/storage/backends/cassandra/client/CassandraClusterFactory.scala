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

import com.datastax.driver.core._
import com.datastax.driver.core.policies.{DefaultRetryPolicy, LatencyAwarePolicy, RoundRobinPolicy, TokenAwarePolicy}
import com.datastax.driver.extras.codecs.date.SimpleTimestampCodec
import com.expedia.www.haystack.trace.storage.backends.cassandra.config.entities.{AwsNodeDiscoveryConfiguration, ClientConfiguration, CredentialsConfiguration}

class CassandraClusterFactory extends ClusterFactory {

  private def discoverNodes(nodeDiscoveryConfig: Option[AwsNodeDiscoveryConfiguration]): Seq[String] = {
    nodeDiscoveryConfig match {
      case Some(awsDiscovery) => AwsNodeDiscoverer.discover(awsDiscovery.region, awsDiscovery.tags)
      case _ => Nil
    }
  }


  override def buildCluster(config: ClientConfiguration): Cluster = {
    val contactPoints = if (config.autoDiscoverEnabled) discoverNodes(config.awsNodeDiscovery) else config.endpoints
    require(contactPoints.nonEmpty, "cassandra contact points can't be empty!!!")

    val tokenAwarePolicy = new TokenAwarePolicy(new LatencyAwarePolicy.Builder(new RoundRobinPolicy()).build())
    val authProvider = fetchAuthProvider(config.plaintextCredentials)
    val cluster = Cluster.builder()
      .withClusterName("cassandra-cluster")
      .addContactPoints(contactPoints: _*)
      .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
      .withAuthProvider(authProvider)
      .withSocketOptions(new SocketOptions()
        .setKeepAlive(config.socket.keepAlive)
        .setConnectTimeoutMillis(config.socket.connectionTimeoutMillis)
        .setReadTimeoutMillis(config.socket.readTimeoutMills))
      .withLoadBalancingPolicy(tokenAwarePolicy)
      .withPoolingOptions(new PoolingOptions().setMaxConnectionsPerHost(HostDistance.LOCAL, config.socket.maxConnectionPerHost))
      .build()
    cluster.getConfiguration.getCodecRegistry.register(SimpleTimestampCodec.instance)

    cluster
  }

  private def fetchAuthProvider(plaintextCredentials: Option[CredentialsConfiguration]): AuthProvider = {
    plaintextCredentials match {
      case Some(credentialsConfiguration) => new PlainTextAuthProvider(credentialsConfiguration.username, credentialsConfiguration.password)
      case _ => AuthProvider.NONE
    }
  }
}
