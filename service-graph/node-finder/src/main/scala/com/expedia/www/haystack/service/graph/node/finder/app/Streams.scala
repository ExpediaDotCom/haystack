/*
 *
 *     Copyright 2018 Expedia, Inc.
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
package com.expedia.www.haystack.service.graph.node.finder.app

import java.util.function.Supplier

import com.expedia.www.haystack.commons.graph.GraphEdgeTagCollector
import com.expedia.www.haystack.commons.kstreams.serde.SpanSerde
import com.expedia.www.haystack.commons.kstreams.serde.graph.GraphEdgeKeySerde
import com.expedia.www.haystack.commons.kstreams.serde.graph.GraphEdgeValueSerde
import com.expedia.www.haystack.commons.kstreams.serde.metricdata.{MetricDataSerializer, MetricTankSerde}
import com.expedia.www.haystack.service.graph.node.finder.app.metadata.MetadataProducerSupplier
import com.expedia.www.haystack.service.graph.node.finder.app.metadata.MetadataStoreUpdateProcessorSupplier
import com.expedia.www.haystack.service.graph.node.finder.config.KafkaConfiguration
import com.expedia.www.haystack.service.graph.node.finder.model.MetadataStoreBuilder
import com.expedia.www.haystack.service.graph.node.finder.model.ServiceNodeMetadataSerde
import com.netflix.servo.util.VisibleForTesting
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.Topology

class Streams(kafkaConfiguration: KafkaConfiguration) extends Supplier[Topology] {

  private val PROTO_SPANS = "proto-spans"
  private val SPAN_ACCUMULATOR = "span-accumulator"
  private val LATENCY_PRODUCER = "latency-producer"
  private val GRAPH_NODE_PRODUCER = "nodes-n-edges-producer"
  private val METRIC_SINK = "metric-sink"
  private val GRAPH_NODE_SINK = "graph-nodes-sink"

  private val METADATA_STORE_PROCESSOR = "metadata-store-processor"
  private val METADATA_SOURCE_NODE = "metadata-source-node"
  private val METADATA_PRODUCER = "metadata-producer"
  private val METADATA_SINK = "metadata-sink"

  override def get(): Topology = initialize(new Topology)

  /**
    * This provides a topology that is shown in the flow chart below
    *
    *                    +---------------+
    *                    |               |
    *                    |  proto-spans  |
    *                    |               |
    *                    +-------+-------+
    *                            |
    *                  +---------V----------+
    *                  |                    |
    *             +----+  span-accumulator  +----+
    *             |    |                    |    |
    *             |    +--------------------+    |
    *             |                              |
    *   +---------V---------+       +------------V------------+
    *   |                   |       |                         |
    *   |  latency-producer |       |  nodes-n-edges-producer |
    *   |                   |       |                         |
    *   +---------+---------+       +------------+------------+
    *             |                              |
    *    +--------V--------+           +---------V---------+
    *    |                 |           |                   |
    *    |   metric-sink   |           |  graph-nodes-sink |
    *    |                 |           |                   |
    *    +-----------------+           +-------------------+
    *
    * Source:
    *
    *   proto-spans  :   Reads a Kafka topic of spans serialized in protobuf format
    *
    *   Processors:
    *
    *     span-accumulator       : Aggregates incoming spans for specified time to find matching client-server spans
    *     latency-producer       : Computes and emits network latency from the span pairs produced by span-accumulator
    *     nodes-n-edges-producer : This processor produces, from the span pairs produced by span-accumulator, a simple
    *                              graph relationship between the services in the form: service --(operation)--> service
    *   Sinks:
    *
    *     metric-sink      : Output of latency-producer (MetricPoint) is serialized using MessagePack and sent to a Kafka topic
    *     graph-nodes-sink : Output of nodes-n-edges-producer is serialized a json string and sent to a Kafka topic
    *
    * @return the Topology
    */
  @VisibleForTesting
  def initialize(topology: Topology): Topology = {
    //add source
    addSource(PROTO_SPANS, topology)

    //add span accumulator. This step will aggregate spans
    //by message id. This will emit spans with client-server
    //relationship after specified number of seconds
    addAccumulator(SPAN_ACCUMULATOR, topology, PROTO_SPANS)

    //add latency producer. This is downstream of accumulator
    //this will parse a span with client-server relationship and
    //emit a metric point on the latency for that service-operation pair
    addLatencyProducer(LATENCY_PRODUCER, topology, SPAN_ACCUMULATOR)

    //add graph node producer. This is downstream of accumulator
    //for each client-server span emitted by the accumulator, this will
    //produce a service - operation - service data point for building
    //the edges between the nodes in a graph
    addGraphNodeProducer(GRAPH_NODE_PRODUCER, topology, SPAN_ACCUMULATOR)

    //add sink for latency producer
    addMetricSink(METRIC_SINK, kafkaConfiguration.metricsTopic, topology, LATENCY_PRODUCER)

    //add sink for graph node producer
    addGraphNodeSink(GRAPH_NODE_SINK, kafkaConfiguration.serviceCallTopic, topology, GRAPH_NODE_PRODUCER)

    //add metadata processor and a sink for metadata store
    addMetadataProducer(METADATA_PRODUCER, topology, SPAN_ACCUMULATOR)
    addMetadataStoreSink(METADATA_SINK, topology, METADATA_PRODUCER)

    //return the topology built
    topology
  }

  private def addSource(stepName: String, topology: Topology) : Unit = {
    //add a source
    topology.addSource(
      kafkaConfiguration.autoOffsetReset,
      stepName,
      kafkaConfiguration.timestampExtractor,
      new StringDeserializer,
      (new SpanSerde).deserializer(),
      kafkaConfiguration.protoSpanTopic)
  }

  private def addAccumulator(accumulatorName: String, topology: Topology, sourceName: String) : Unit = {
    val tags =
      if (kafkaConfiguration.collectorTags != null)
        kafkaConfiguration.collectorTags.toSet[String]
      else
        Set[String]()

    topology.addProcessor(
      accumulatorName,
      new SpanAccumulatorSupplier(kafkaConfiguration.metadataConfig.topic, kafkaConfiguration.accumulatorInterval,
        new GraphEdgeTagCollector(tags)),
      sourceName
    )

    topology.addGlobalStore(MetadataStoreBuilder.storeBuilder(kafkaConfiguration.metadataConfig),
      METADATA_SOURCE_NODE,
      Serdes.String().deserializer(),
      new ServiceNodeMetadataSerde().deserializer(),
      kafkaConfiguration.metadataConfig.topic,
      METADATA_STORE_PROCESSOR,
      new MetadataStoreUpdateProcessorSupplier(kafkaConfiguration.metadataConfig.topic))
  }

  private def addLatencyProducer(latencyProducerName: String,
                                 topology: Topology,
                                 accumulatorName: String) : Unit = {
    topology.addProcessor(
      latencyProducerName,
      new LatencyProducerSupplier(),
      accumulatorName
    )
  }

  private def addGraphNodeProducer(graphNodeProducerName: String,
                                   topology: Topology,
                                   accumulatorName: String) = {
    topology.addProcessor(
      graphNodeProducerName,
      new GraphNodeProducerSupplier(),
      accumulatorName
    )
  }

  private def addMetricSink(metricSinkName: String,
                            metricsTopic: String,
                            topology: Topology,
                            latencyProducerName: String): Unit = {
    topology.addSink(
      metricSinkName,
      metricsTopic,
      new StringSerializer,
      new MetricDataSerializer,
      latencyProducerName
    )
  }

  private def addGraphNodeSink(graphNodeSinkName: String,
                               serviceCallTopic: String,
                               topology: Topology,
                               graphNodeProducerName: String): Unit = {
    topology.addSink(
      graphNodeSinkName,
      serviceCallTopic,
      new GraphEdgeKeySerde().serializer(),
      new GraphEdgeValueSerde().serializer(),
      graphNodeProducerName
    )
  }

  private def addMetadataStoreSink(sinkName: String,
                                   topology: Topology,
                                   producerName: String): Unit = {
    topology.addSink(
      sinkName,
      kafkaConfiguration.metadataConfig.topic,
      Serdes.String().serializer(),
      new ServiceNodeMetadataSerde().serializer(),
      producerName
    )
  }

  private def addMetadataProducer(processorName: String,
                                  topology: Topology,
                                  producerName: String): Unit = {
    topology.addProcessor(
      processorName,
      new MetadataProducerSupplier(kafkaConfiguration.metadataConfig.topic),
      producerName
    )
  }
}
