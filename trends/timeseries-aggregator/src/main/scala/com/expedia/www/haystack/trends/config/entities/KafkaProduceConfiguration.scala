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

package com.expedia.www.haystack.trends.config.entities

import java.util.Properties

/**
  *This configuration specifies if the stream topology writes the aggregated metrics to an external kafka cluster
  * @param kafkaSinkTopics - list of all sinks along with the serdes.
  * @param props - Kafka producer configuration
  * @param enableExternalKafka - enable/disable external kafka sink
  */
case class KafkaProduceConfiguration(kafkaSinkTopics: List[KafkaSinkTopic], props: Option[Properties], externalKafkaTopic: String, enableExternalKafka: Boolean)

case class KafkaSinkTopic(topic: String, serdeClassName:String, enabled: Boolean)
