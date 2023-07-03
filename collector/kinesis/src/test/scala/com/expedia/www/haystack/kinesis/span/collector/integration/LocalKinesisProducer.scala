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

package com.expedia.www.haystack.kinesis.span.collector.integration

import java.nio.ByteBuffer
import java.util.UUID

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.amazonaws.services.kinesis.model.ResourceNotFoundException
import com.amazonaws.{ClientConfiguration, Protocol}
import com.expedia.www.haystack.kinesis.span.collector.integration.config.TestConfiguration

trait LocalKinesisProducer {

  private val client = {
    val endpointConfig = new AwsClientBuilder.EndpointConfiguration(s"http://${TestConfiguration.remoteKinesisHost}:${TestConfiguration.kinesisPort}", "us-west-2")
    val clientConfig = new ClientConfiguration().withProtocol(Protocol.HTTP)

    AmazonKinesisClientBuilder
      .standard()
      .withClientConfiguration(clientConfig)
      .withEndpointConfiguration(endpointConfig)
      .build()
  }

  protected def createStreamIfNotExists(): Unit = {
    try {
      client.describeStream(TestConfiguration.kinesisStreamName)
    } catch {
      case _: ResourceNotFoundException =>
        println(s"Creating kinesis stream ${TestConfiguration.kinesisStreamName}")
        client.createStream(TestConfiguration.kinesisStreamName, 1)
    }
  }

  def produceRecordsToKinesis(records: List[Array[Byte]]): Unit = {
    records.foreach(record => {
      client.putRecord(TestConfiguration.kinesisStreamName, ByteBuffer.wrap(record), UUID.randomUUID().toString)
    })
  }

  protected def shutdownKinesisClient(): Unit = if(client != null) client.shutdown()
}
