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

class PartialTraceIndexingTopologySpec extends BaseIntegrationTestSpec {
  private val MAX_CHILD_SPANS_PER_TRACE = 5
  private val TRACE_ID = "unique-trace-id"

  "Trace Indexing Topology" should {
    s"consume spans from '${kafka.INPUT_TOPIC}' topic, buffer them together for every unique traceId and write to trace-backend and elastic search" in {
      Given("a set of spans with all configurations")
      val SPAN_ID_PREFIX = "span-id"
      val kafkaConfig = kafka.buildConfig
      val esConfig = elastic.buildConfig
      val indexTagsConfig = elastic.indexingConfig
      val backendConfig = traceBackendClient.buildConfig
      val serviceMetadataConfig = elastic.buildServiceMetadataConfig
      val traceDescription = List(TraceDescription(TRACE_ID, SPAN_ID_PREFIX))

      When(s"spans are produced in '${kafka.INPUT_TOPIC}' topic async, and kafka-streams topology is started")
      produceSpansAsync(
        MAX_CHILD_SPANS_PER_TRACE,
        1.second,
        traceDescription,
        0L,
        spanAccumulatorConfig.bufferingWindowMillis)

      val topology = new StreamRunner(kafkaConfig, spanAccumulatorConfig, esConfig, backendConfig, serviceMetadataConfig, indexTagsConfig)
      topology.start()

      Then(s"we should read one span buffer object from '${kafka.OUTPUT_TOPIC}' topic and the same should be searchable in trace-backend and elastic search")
      try {
        val result: util.List[KeyValue[String, SpanBuffer]] =
          IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(kafka.RESULT_CONSUMER_CONFIG, kafka.OUTPUT_TOPIC, 1, MAX_WAIT_FOR_OUTPUT_MS)
        validateKafkaOutput(result.asScala, MAX_CHILD_SPANS_PER_TRACE, SPAN_ID_PREFIX)

        // give a sleep to let elastic search results become searchable
        Thread.sleep(6000)
        verifyBackendWrites(traceDescription, MAX_CHILD_SPANS_PER_TRACE, MAX_CHILD_SPANS_PER_TRACE)
        verifyElasticSearchWrites(Seq(TRACE_ID))

        repeatTestWithNewerSpanIds()
      } finally {
        topology.close()
      }
    }
  }

  // this test is useful to check if we are not emitting the old spans if the same traceId reappears later
  private def repeatTestWithNewerSpanIds(): Unit = {
    Given(s"a set of new span ids and same traceId '$TRACE_ID'")
    val SPAN_ID_2_PREFIX = "span-id-2"
    When(s"these spans are produced in '${kafka.INPUT_TOPIC}' topic on the currently running topology")
    produceSpansAsync(
      MAX_CHILD_SPANS_PER_TRACE,
      1.seconds,
      List(TraceDescription(TRACE_ID, SPAN_ID_2_PREFIX)),
      spanAccumulatorConfig.bufferingWindowMillis + 100L,
      spanAccumulatorConfig.bufferingWindowMillis)

    Then(s"we should read see newer spans in the buffered object from '${kafka.OUTPUT_TOPIC}' topic")
    val result: util.List[KeyValue[String, SpanBuffer]] =
      IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(kafka.RESULT_CONSUMER_CONFIG, kafka.OUTPUT_TOPIC, 1, MAX_WAIT_FOR_OUTPUT_MS)

    validateKafkaOutput(result.asScala, MAX_CHILD_SPANS_PER_TRACE, SPAN_ID_2_PREFIX)
  }

  // validate the kafka output
  private def validateKafkaOutput(records: Iterable[KeyValue[String, SpanBuffer]],
                                  childSpanCount: Int,
                                  spanIdPrefix: String): Unit = {
    // expect only one span buffer object
    records.size shouldBe 1
    validateChildSpans(records.head.value, TRACE_ID, spanIdPrefix, MAX_CHILD_SPANS_PER_TRACE)
  }
}