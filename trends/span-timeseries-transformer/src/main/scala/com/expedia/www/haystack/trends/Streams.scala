/*
 *
 *     Copyright 2017 Expedia, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */

package com.expedia.www.haystack.trends

import java.util.function.Supplier

import com.expedia.metrics.MetricData
import com.expedia.open.tracing.Span
import com.expedia.www.haystack.commons.kstreams.serde.SpanSerde
import com.expedia.www.haystack.commons.kstreams.serde.metricdata.MetricTankSerde
import com.expedia.www.haystack.commons.util.MetricDefinitionKeyGenerator._
import com.expedia.www.haystack.trends.config.entities.{KafkaConfiguration, TransformerConfiguration}
import com.expedia.www.haystack.trends.transformer.MetricDataTransformer.allTransformers
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams._
import org.apache.kafka.streams.kstream.Produced

import scala.collection.JavaConverters._

class Streams(kafkaConfig: KafkaConfiguration, transformConfig: TransformerConfiguration) extends Supplier[Topology]
  with MetricDataGenerator {

  private[trends] def initialize(builder: StreamsBuilder): Topology = {
    val consumed = Consumed.`with`(kafkaConfig.autoOffsetReset)
      .withKeySerde(new StringSerde)
      .withValueSerde(new SpanSerde)
      .withTimestampExtractor(kafkaConfig.timestampExtractor)

    builder
      .stream(kafkaConfig.consumeTopic, consumed)
      .filter((_: String, span: Span) => isValidSpan(span, transformConfig.blacklistedServices))
      .flatMap[String, MetricData]((_: String, span: Span) => mapToMetricDataKeyValue(span))
      .to(kafkaConfig.produceTopic, Produced.`with`(new StringSerde(), new MetricTankSerde()))

    builder.build()
  }

  private def mapToMetricDataKeyValue(span: Span): java.lang.Iterable[KeyValue[String, MetricData]] = {
    val metricData: Seq[MetricData] = generateMetricDataList(span,
      allTransformers,
      transformConfig.encoder,
      transformConfig.enableMetricPointServiceLevelGeneration)

    metricData.map {
      md => new KeyValue[String, MetricData](generateKey(md.getMetricDefinition), md)
    }.asJavaCollection
  }

  override def get(): Topology = {
    val builder = new StreamsBuilder()
    initialize(builder)
  }
}
