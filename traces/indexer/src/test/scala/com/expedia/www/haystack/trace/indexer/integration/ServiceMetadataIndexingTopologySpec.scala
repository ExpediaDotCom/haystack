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

package com.expedia.www.haystack.trace.indexer.integration

import java.util

import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.indexer.StreamRunner
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils

import scala.concurrent.duration._

class ServiceMetadataIndexingTopologySpec extends BaseIntegrationTestSpec {
  private val MAX_CHILD_SPANS_PER_TRACE = 5
  private val TRACE_ID_6 = "traceid-6"
  private val TRACE_ID_7 = "traceid-7"
  private val SPAN_ID_PREFIX_1 = TRACE_ID_6 + "span-id-"
  private val SPAN_ID_PREFIX_2 = TRACE_ID_7 + "span-id-"

  "Trace Indexing Topology" should {
    s"consume spans from input '${kafka.INPUT_TOPIC}' and buffer them together for every service operation combination and write to elastic search elastic" in {
      Given("a set of spans with different serviceNames and a project configurations")
      val kafkaConfig = kafka.buildConfig
      val esConfig = elastic.buildConfig
      val indexTagsConfig = elastic.indexingConfig
      val backendConfig = traceBackendClient.buildConfig
      val serviceMetadataConfig = elastic.buildServiceMetadataConfig

      When(s"spans are produced in '${kafka.INPUT_TOPIC}' topic async, and kafka-streams topology is started")
      val traceDescriptions = List(TraceDescription(TRACE_ID_6, SPAN_ID_PREFIX_1), TraceDescription(TRACE_ID_7, SPAN_ID_PREFIX_2))

      produceSpansAsync(MAX_CHILD_SPANS_PER_TRACE,
        1.seconds,
        traceDescriptions,
        0,
        spanAccumulatorConfig.bufferingWindowMillis)

      val topology = new StreamRunner(kafkaConfig, spanAccumulatorConfig, esConfig, backendConfig, serviceMetadataConfig, indexTagsConfig)
      topology.start()

      Then(s"we should read two multiple service operation combinations in elastic search")
      try {
        val result: util.List[KeyValue[String, SpanBuffer]] =
          IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(kafka.RESULT_CONSUMER_CONFIG, kafka.OUTPUT_TOPIC, 2, MAX_WAIT_FOR_OUTPUT_MS)
        Thread.sleep(6000)
        verifyOperationNames()
      } finally {
        topology.close()
      }
    }
  }
}
