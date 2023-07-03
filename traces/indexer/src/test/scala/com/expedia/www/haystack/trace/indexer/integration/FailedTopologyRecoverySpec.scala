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

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class FailedTopologyRecoverySpec extends BaseIntegrationTestSpec {
  private val MAX_CHILD_SPANS_PER_TRACE = 5
  private val TRACE_ID_3 = "traceid-3"
  private val SPAN_ID_PREFIX = "span-id-"
  private val TRACE_DESCRIPTIONS = List(TraceDescription(TRACE_ID_3, SPAN_ID_PREFIX))

  "Trace Indexing Topology" should {
    s"consume spans from input '${kafka.INPUT_TOPIC}', buffer them together keyed by unique TraceId and write to trace-backend and elastic even if crashed in between" in {
      Given("a set of spans produced async with spanBuffer+kafka configurations")
      val kafkaConfig = kafka.buildConfig
      val esConfig = elastic.buildConfig
      val indexTagsConfig = elastic.indexingConfig
      val backendConfig = traceBackendClient.buildConfig
      val serviceMetadataConfig = elastic.buildServiceMetadataConfig
      val accumulatorConfig = spanAccumulatorConfig.copy(pollIntervalMillis = spanAccumulatorConfig.pollIntervalMillis * 5)
      val startTimestamp = System.currentTimeMillis()
      produceSpansAsync(
        MAX_CHILD_SPANS_PER_TRACE,
        produceInterval = 1.seconds,
        TRACE_DESCRIPTIONS,
        startTimestamp,
        spanAccumulatorConfig.bufferingWindowMillis)

      When(s"kafka-streams topology is started and then stopped forcefully after few sec")
      var topology = new StreamRunner(kafkaConfig, accumulatorConfig, esConfig, backendConfig, serviceMetadataConfig, indexTagsConfig)
      topology.start()
      Thread.sleep(7000)
      topology.close()

      // wait for few sec to close the stream threads
      Thread.sleep(6000)

      Then(s"on restart of the topology, we should be able to read complete trace created in previous run from the '${kafka.OUTPUT_TOPIC}' topic in kafka, trace-backend and elasticsearch")
      topology = new StreamRunner(kafkaConfig, accumulatorConfig, esConfig, backendConfig, serviceMetadataConfig, indexTagsConfig)
      topology.start()

      // produce one more span record with same traceId to trigger punctuate
      produceSpansAsync(
        1,
        produceInterval = 1.seconds,
        TRACE_DESCRIPTIONS,
        startTimestamp + spanAccumulatorConfig.bufferingWindowMillis,
        spanAccumulatorConfig.bufferingWindowMillis,
        startSpanIdxFrom = MAX_CHILD_SPANS_PER_TRACE)

      try {
        val records: util.List[KeyValue[String, SpanBuffer]] =
          IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(kafka.RESULT_CONSUMER_CONFIG, kafka.OUTPUT_TOPIC, 1, MAX_WAIT_FOR_OUTPUT_MS)

        // wait for the elastic search writes to pass through, i guess refresh time has to be adjusted
        Thread.sleep(5000)
        validateKafkaOutput(records.asScala, MAX_CHILD_SPANS_PER_TRACE)
        verifyBackendWrites(TRACE_DESCRIPTIONS, MAX_CHILD_SPANS_PER_TRACE, MAX_CHILD_SPANS_PER_TRACE + 1) // 1 extra record for trigger
        verifyElasticSearchWrites(Seq(TRACE_ID_3))
      } finally {
        topology.close()
      }
    }
  }

  // validate the kafka output
  private def validateKafkaOutput(records: Iterable[KeyValue[String, SpanBuffer]], minChildSpanCount: Int) = {
    // expect only one span buffer
    records.size shouldBe 1
    records.head.key shouldBe TRACE_ID_3
    records.head.value.getChildSpansCount should be >=minChildSpanCount
  }
}
