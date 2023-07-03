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

package com.expedia.www.haystack.trace.indexer.writers.kafka

import java.util.Properties

import com.codahale.metrics.Meter
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trace.commons.packer.PackedMessage
import com.expedia.www.haystack.trace.indexer.metrics.AppMetricNames
import com.expedia.www.haystack.trace.indexer.writers.TraceWriter
import org.apache.kafka.clients.producer._
import org.slf4j.LoggerFactory

import scala.util.Try

object KafkaWriter extends MetricsSupport {
  protected val kafkaProducerFailures: Meter = metricRegistry.meter(AppMetricNames.KAFKA_PRODUCE_FAILURES)
}

class KafkaWriter(producerConfig: Properties, topic: String) extends TraceWriter {
  private val LOGGER = LoggerFactory.getLogger(classOf[KafkaWriter])

  private val producer = new KafkaProducer[String, Array[Byte]](producerConfig)

  override def writeAsync(traceId: String, packedSpanBuffer: PackedMessage[SpanBuffer], isLastSpanBuffer: Boolean): Unit = {
    val record = new ProducerRecord[String, Array[Byte]](topic, traceId, packedSpanBuffer.packedDataBytes)
    producer.send(record, (_: RecordMetadata, exception: Exception) => {
      if (exception != null) {
        LOGGER.error(s"Fail to write the span buffer record to kafka topic=$topic", exception)
        KafkaWriter.kafkaProducerFailures.mark()
      }
    })
  }

  override def close(): Unit = Try(producer.close())
}
