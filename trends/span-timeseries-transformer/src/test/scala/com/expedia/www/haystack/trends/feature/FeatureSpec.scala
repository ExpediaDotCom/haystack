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

import java._
import java.util.Properties

import com.expedia.metrics.MetricData
import com.expedia.open.tracing.Span
import com.expedia.www.haystack.commons.entities.encoders.Base64Encoder
import com.expedia.www.haystack.trends.config.AppConfiguration
import com.expedia.www.haystack.trends.config.entities.{KafkaConfiguration, TransformerConfiguration}
import org.apache.kafka.streams.StreamsConfig
import org.easymock.EasyMock
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FeatureSpecLike, GivenWhenThen, Matchers}


trait FeatureSpec extends FeatureSpecLike with GivenWhenThen with Matchers with EasyMockSugar {

  protected val METRIC_TYPE = "gauge"

  def generateTestSpan(duration: Long): Span = {
    val operationName = "testSpan"
    val serviceName = "testService"
    Span.newBuilder()
      .setDuration(duration)
      .setOperationName(operationName)
      .setServiceName(serviceName)
      .build()
  }

  protected def mockAppConfig: AppConfiguration = {
    val kafkaConsumeTopic = "test-consume"
    val kafkaProduceTopic = "test-produce"
    val streamsConfig = new Properties()
    streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-app")
    streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "test-kafka-broker")
    val kafkaConfig = KafkaConfiguration(new StreamsConfig(streamsConfig), kafkaProduceTopic, kafkaConsumeTopic, null, null, 0l)
    val transformerConfig = TransformerConfiguration(new Base64Encoder, enableMetricPointServiceLevelGeneration = true, List())
    val appConfiguration = mock[AppConfiguration]

    expecting {
      appConfiguration.kafkaConfig.andReturn(kafkaConfig).anyTimes()
      appConfiguration.transformerConfiguration.andReturn(transformerConfig).anyTimes()
    }
    EasyMock.replay(appConfiguration)
    appConfiguration
  }

  protected def getMetricDataTags(metricData : MetricData): util.Map[String, String] = {
    metricData.getMetricDefinition.getTags.getKv
  }

}
