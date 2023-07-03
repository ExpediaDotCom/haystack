package com.expedia.www.haystack.trace.indexer.integration

import java.util

import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.indexer.StreamRunner
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class EvictedSpanBufferSpec extends BaseIntegrationTestSpec {
  private val MAX_CHILD_SPANS = 5
  private val TRACE_ID_1 = "traceid-1"
  private val TRACE_ID_2 = "traceid-2"
  private val SPAN_ID_PREFIX = "span-id-"

  "Trace Indexing Topology" should {
    s"consume spans from input '${kafka.INPUT_TOPIC}', buffer them together for a given traceId  and write to trace-backend and elastic on eviction" in {
      Given("a set of spans produced async with extremely extremely small store size configuration")
      val kafkaConfig = kafka.buildConfig
      val esConfig = elastic.buildConfig
      val indexTagsConfig = elastic.indexingConfig
      val backendConfig = traceBackendClient.buildConfig
      val serviceMetadataConfig = elastic.buildServiceMetadataConfig
      val accumulatorConfig = spanAccumulatorConfig.copy(minTracesPerCache = 1, maxEntriesAllStores = 1)

      produceSpansAsync(MAX_CHILD_SPANS,
        produceInterval = 1.seconds,
        List(TraceDescription(TRACE_ID_1, SPAN_ID_PREFIX), TraceDescription(TRACE_ID_2, SPAN_ID_PREFIX)),
        0L, accumulatorConfig.bufferingWindowMillis)

      When(s"kafka-streams topology is started")
      val topology = new StreamRunner(kafkaConfig, accumulatorConfig, esConfig, backendConfig, serviceMetadataConfig, indexTagsConfig)
      topology.start()

      Then(s"we should get multiple span-buffers bearing only 1 span due to early eviction from store")
      val records: util.List[KeyValue[String, SpanBuffer]] =
        IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(kafka.RESULT_CONSUMER_CONFIG, kafka.OUTPUT_TOPIC, 10, MAX_WAIT_FOR_OUTPUT_MS)

      validateKafkaOutput(records.asScala)
      topology.close()
    }
  }

  // validate the kafka output
  private def validateKafkaOutput(records: Seq[KeyValue[String, SpanBuffer]]): Unit = {
    records.map(_.key).toSet should contain allOf (TRACE_ID_1, TRACE_ID_2)
    records.foreach(rec => rec.value.getChildSpansCount shouldBe 1)
  }
}
