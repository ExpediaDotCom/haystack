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
package com.expedia.www.haystack.service.graph.node.finder.config

import org.apache.commons.lang3.StringUtils
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology.AutoOffsetReset
import org.apache.kafka.streams.processor.TimestampExtractor

/**
  * Case class holding required configuration for the node finder kstreams app
  * @param streamsConfig valid instance of StreamsConfig
  * @param metricsTopic topic name for latency metrics
  * @param serviceCallTopic topic name for service call relationship information
  * @param protoSpanTopic topic from where Spans serialized in protobuf to be consumed
  * @param autoOffsetReset Offset type for the kstreams app to start with
  * @param timestampExtractor instance of timestamp extractor
  * @param accumulatorInterval interval to aggregate spans to look for client and server spans
  * @param closeTimeoutInMs time for closing a kafka topic
  * @param metadataConfig configuration for metadata kakfa topic
  * @param collectorTags Tags to be collected when generating graph edges
  */
case class KafkaConfiguration(streamsConfig: StreamsConfig,
                              metricsTopic: String,
                              serviceCallTopic: String,
                              protoSpanTopic: String,
                              autoOffsetReset: AutoOffsetReset,
                              timestampExtractor: TimestampExtractor,
                              accumulatorInterval: Int,
                              closeTimeoutInMs: Long,
                              metadataConfig: NodeMetadataConfiguration,
                              collectorTags: List[String]) {
  require(streamsConfig != null)
  require(StringUtils.isNotBlank(metricsTopic))
  require(StringUtils.isNotBlank(serviceCallTopic))
  require(StringUtils.isNotBlank(protoSpanTopic))
  require(autoOffsetReset != null)
  require(timestampExtractor != null)
  require(closeTimeoutInMs > 0)
  require(collectorTags != null)
}
