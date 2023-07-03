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
package com.expedia.www.haystack.trends.integration.tests

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.trends.integration.IntegrationTestSpec
import org.apache.kafka.clients.admin.{AdminClient, Config}
import org.apache.kafka.common.config.ConfigResource
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils
import org.scalatest.Sequential

import scala.collection.JavaConverters._
import scala.concurrent.duration._

@Sequential
class StateStoreSpec extends IntegrationTestSpec {

  private val MAX_METRICPOINTS = 62
  private val numberOfWatermarkedWindows = 1

  "TimeSeriesAggregatorTopology" should {


    "have state store (change log) configuration be set by the topology" in {
      Given("a set of metricPoints with type metric and state store specific configurations")
      val METRIC_NAME = "success-span"
      // CountMetric
      val streamsRunner = createStreamRunner()

      When("metricPoints are produced in 'input' topic async, and kafka-streams topology is started")
      produceMetricPointsAsync(3, 10.milli, METRIC_NAME, 3 * 60)
      streamsRunner.start()

      Then("we should see the state store topic created with specified properties")
      val waitTimeMs = 15000
      IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived[String, MetricData](RESULT_CONSUMER_CONFIG, OUTPUT_TOPIC, 1, waitTimeMs).asScala.toList
      val adminClient = AdminClient.create(STREAMS_CONFIG)
      val configResource = new ConfigResource(ConfigResource.Type.TOPIC, CHANGELOG_TOPIC)
      val describeConfigResult: java.util.Map[ConfigResource, Config] = adminClient.describeConfigs(java.util.Arrays.asList(configResource)).all().get()
      describeConfigResult.get(configResource).get(stateStoreConfigs.head._1).value() shouldBe stateStoreConfigs.head._2
    }
  }

}
