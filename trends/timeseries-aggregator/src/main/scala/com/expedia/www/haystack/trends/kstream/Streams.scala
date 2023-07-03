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

package com.expedia.www.haystack.trends.kstream

import java.util.function.Supplier

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.commons.kstreams.serde.metricdata.{MetricDataSerde, MetricTankSerde}
import com.expedia.www.haystack.trends.aggregation.TrendMetric
import com.expedia.www.haystack.trends.config.AppConfiguration
import com.expedia.www.haystack.trends.kstream.processor.{AdditionalTagsProcessorSupplier, ExternalKafkaProcessorSupplier, MetricAggProcessorSupplier}
import com.expedia.www.haystack.trends.kstream.store.HaystackStoreBuilder
import org.apache.kafka.common.serialization.{Serde, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.state.{KeyValueStore, StoreBuilder}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters

class Streams(appConfiguration: AppConfiguration) extends Supplier[Topology] {

  private val LOGGER = LoggerFactory.getLogger(classOf[Streams])
  private val TOPOLOGY_SOURCE_NAME = "metricpoint-source"
  private val TOPOLOGY_EXTERNAL_SINK_NAME = "metricpoint-aggegated-sink-external"
  private val TOPOLOGY_INTERNAL_SINK_NAME = "metric-data-aggegated-sink-internal"
  private val TOPOLOGY_AGGREGATOR_PROCESSOR_NAME = "metricpoint-aggregator-process"
  private val TOPOLOGY_ADDITIONAL_TAGS_PROCESSOR_NAME = "additional-tags-process"
  private val TOPOLOGY_AGGREGATOR_TREND_METRIC_STORE_NAME = "trend-metric-store"
  private val kafkaConfig = appConfiguration.kafkaConfig

  private def initialize(topology: Topology): Topology = {

    //add source - topic where the raw metricpoints are pushed by the span-timeseries-transformer
    topology.addSource(
      kafkaConfig.autoOffsetReset,
      TOPOLOGY_SOURCE_NAME,
      kafkaConfig.timestampExtractor,
      new StringDeserializer,
      new MetricTankSerde().deserializer(),
      kafkaConfig.consumeTopic)


    //The processor which performs aggregations on the metrics
    topology.addProcessor(
      TOPOLOGY_AGGREGATOR_PROCESSOR_NAME,
      new MetricAggProcessorSupplier(TOPOLOGY_AGGREGATOR_TREND_METRIC_STORE_NAME, appConfiguration.encoder),
      TOPOLOGY_SOURCE_NAME)


    //key-value, state store associated with each kstreams task(partition)
    // which keeps the trend-metrics which are currently being computed in memory
    topology.addStateStore(createTrendMetricStateStore(), TOPOLOGY_AGGREGATOR_PROCESSOR_NAME)

    // topology to add additional tags if any
    topology.addProcessor(TOPOLOGY_ADDITIONAL_TAGS_PROCESSOR_NAME, new AdditionalTagsProcessorSupplier(appConfiguration.additionalTags), TOPOLOGY_AGGREGATOR_PROCESSOR_NAME)

    if (appConfiguration.kafkaConfig.producerConfig.enableExternalKafka) {
      topology.addProcessor(
        TOPOLOGY_EXTERNAL_SINK_NAME,
        new ExternalKafkaProcessorSupplier(appConfiguration.kafkaConfig.producerConfig),
        TOPOLOGY_ADDITIONAL_TAGS_PROCESSOR_NAME
        )
    }

    // adding sinks
    appConfiguration.kafkaConfig.producerConfig.kafkaSinkTopics.foreach(sinkTopic => {
      if(sinkTopic.enabled){
        val serde = Class.forName(sinkTopic.serdeClassName).newInstance().asInstanceOf[Serde[MetricData]]
        topology.addSink(
          s"${TOPOLOGY_INTERNAL_SINK_NAME}-${sinkTopic.topic}",
          sinkTopic.topic,
          new StringSerializer,
          serde.serializer(),
          TOPOLOGY_ADDITIONAL_TAGS_PROCESSOR_NAME)
      }
    })

    topology
  }


  private def createTrendMetricStateStore(): StoreBuilder[KeyValueStore[String, TrendMetric]] = {

    val stateStoreConfiguration = appConfiguration.stateStoreConfig

    val storeBuilder = new HaystackStoreBuilder(TOPOLOGY_AGGREGATOR_TREND_METRIC_STORE_NAME, stateStoreConfiguration.stateStoreCacheSize)

    if (stateStoreConfiguration.enableChangeLogging) {
      storeBuilder
        .withLoggingEnabled(JavaConverters.mapAsJavaMap(stateStoreConfiguration.changeLogTopicConfiguration))

    } else {
      storeBuilder
        .withLoggingDisabled()
    }
  }


  override def get(): Topology = {
    val topology = new Topology
    initialize(topology)
  }
}
