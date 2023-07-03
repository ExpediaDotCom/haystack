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
package com.expedia.www.haystack.trends.feature

import java.util
import java.util.Properties

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import com.expedia.www.haystack.commons.entities.encoders.PeriodReplacementEncoder
import com.expedia.www.haystack.trends.config.AppConfiguration
import com.expedia.www.haystack.trends.config.entities.{KafkaConfiguration, KafkaProduceConfiguration, KafkaSinkTopic, StateStoreConfiguration}
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology.AutoOffsetReset
import org.apache.kafka.streams.processor.WallclockTimestampExtractor
import org.easymock.EasyMock
import org.scalatest._
import org.scalatest.easymock.EasyMockSugar
import org.mockito.Mockito._

import scala.collection.JavaConverters._


trait FeatureSpec extends FeatureSpecLike with GivenWhenThen with Matchers with EasyMockSugar {

  def currentTimeInSecs: Long = {
    System.currentTimeMillis() / 1000l
  }

  protected def mockAppConfig: AppConfiguration = {
    val kafkaConsumeTopic = "test-consume"
    val kafkaProduceTopic = "test-produce"
    val kafkaMetricTankProduceTopic = "test-mdm-produce"
    val streamsConfig = new Properties()
    streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app")
    streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "test-kafka-broker")

    val kafkaSinkTopics = List(KafkaSinkTopic("metrics","com.expedia.www.haystack.commons.kstreams.serde.metricdata.MetricDataSerde",true), KafkaSinkTopic("mdm","com.expedia.www.haystack.commons.kstreams.serde.metricdata.MetricTankSerde",true))
    val kafkaConfig = KafkaConfiguration(new StreamsConfig(streamsConfig), KafkaProduceConfiguration(kafkaSinkTopics, None, "mdm", false), kafkaConsumeTopic, AutoOffsetReset.EARLIEST, new WallclockTimestampExtractor, 30000)
    val projectConfiguration = mock[AppConfiguration]

    expecting {
      projectConfiguration.kafkaConfig.andReturn(kafkaConfig).anyTimes()
      projectConfiguration.encoder.andReturn(new PeriodReplacementEncoder).anyTimes()
      projectConfiguration.stateStoreConfig.andReturn(StateStoreConfiguration(128, false, 60, Map())).anyTimes()
      projectConfiguration.additionalTags.andReturn(Map("k1"->"v1", "k2"-> "v2")).anyTimes()
    }
    EasyMock.replay(projectConfiguration)
    projectConfiguration
  }

  protected def getMetricData(metricKey: String, tags: Map[String, String], value: Double, timeStamp: Long): MetricData = {

    val tagsMap = new java.util.LinkedHashMap[String, String] {
      if (tags != null) putAll(tags.asJava)
      put(MetricDefinition.MTYPE, "gauge")
      put(MetricDefinition.UNIT, "short")
    }
    val metricDefinition = new MetricDefinition(metricKey, new TagCollection(tagsMap), TagCollection.EMPTY)
    new MetricData(metricDefinition, value, timeStamp)
  }

  protected def containsTagInMetricData(metricData: MetricData, tagKey: String, tagValue: String): Boolean = {
    val tags = getTagsFromMetricData(metricData)
    tags.containsKey(tagKey) && tags.get(tagKey).equalsIgnoreCase(tagValue)
  }

  protected def getTagsFromMetricData(metricData: MetricData): util.Map[String, String] = {
    metricData.getMetricDefinition.getTags.getKv
  }
}
