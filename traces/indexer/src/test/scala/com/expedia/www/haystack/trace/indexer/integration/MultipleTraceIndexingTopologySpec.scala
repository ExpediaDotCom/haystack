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

class MultipleTraceIndexingTopologySpec extends BaseIntegrationTestSpec {
  private val MAX_CHILD_SPANS_PER_TRACE = 5
  private val TRACE_ID_9 = "traceid-9"
  private val TRACE_ID_5 = "traceid-5"
  private val SPAN_ID_PREFIX_1 = TRACE_ID_9 + "span-id-"
  private val SPAN_ID_PREFIX_2 = TRACE_ID_5 + "span-id-"

  "Trace Indexing Topology" should {
    s"consume spans from input '${kafka.INPUT_TOPIC}' and buffer them together for every unique traceId and write to trace-backend and elastic search" in {
      Given("a set of spans with two different traceIds and project configurations")
      val kafkaConfig = kafka.buildConfig
      val esConfig = elastic.buildConfig
      val indexTagsConfig = elastic.indexingConfig
      val backendConfig = traceBackendClient.buildConfig
      val serviceMetadataConfig = elastic.buildServiceMetadataConfig

      When(s"spans are produced in '${kafka.INPUT_TOPIC}' topic async, and kafka-streams topology is started")
      val traceDescriptions = List(TraceDescription(TRACE_ID_5, SPAN_ID_PREFIX_2),TraceDescription(TRACE_ID_9, SPAN_ID_PREFIX_1))

      produceSpansAsync(MAX_CHILD_SPANS_PER_TRACE,
        1.seconds,
        traceDescriptions,
        startRecordTimestamp = 0,
        maxRecordTimestamp = spanAccumulatorConfig.bufferingWindowMillis)

      val topology = new StreamRunner(kafkaConfig, spanAccumulatorConfig, esConfig, backendConfig, serviceMetadataConfig, indexTagsConfig)
      topology.start()

      Then(s"we should read two span buffers with different traceIds from '${kafka.OUTPUT_TOPIC}' topic and same should be read from trace-backend and elastic search")
      try {
        val result: util.List[KeyValue[String, SpanBuffer]] =
          IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(kafka.RESULT_CONSUMER_CONFIG, kafka.OUTPUT_TOPIC, 2, MAX_WAIT_FOR_OUTPUT_MS)

        validateKafkaOutput(result.asScala,MAX_CHILD_SPANS_PER_TRACE)

        Thread.sleep(6000)
        verifyBackendWrites(traceDescriptions, MAX_CHILD_SPANS_PER_TRACE, MAX_CHILD_SPANS_PER_TRACE)
        verifyElasticSearchWrites(Seq(TRACE_ID_9, TRACE_ID_5))
      } finally {
        topology.close()
      }
    }
  }

  // validate the kafka output
  private def validateKafkaOutput(records: Iterable[KeyValue[String, SpanBuffer]], childSpanCount: Int): Unit = {
    records.size shouldBe 2

    // both traceIds should be present as different span buffer objects
    records.map(_.key) should contain allOf (TRACE_ID_9, TRACE_ID_5)

    records.foreach(record => {
      record.key match {
        case TRACE_ID_9 => validateChildSpans(record.value, TRACE_ID_9, SPAN_ID_PREFIX_1, childSpanCount)
        case TRACE_ID_5 => validateChildSpans(record.value, TRACE_ID_5, SPAN_ID_PREFIX_2, childSpanCount)
      }
    })
  }
}
