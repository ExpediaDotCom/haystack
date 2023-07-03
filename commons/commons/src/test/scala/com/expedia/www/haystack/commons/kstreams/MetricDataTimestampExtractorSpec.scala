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

package com.expedia.www.haystack.commons.kstreams

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import com.expedia.www.haystack.commons.unit.UnitTestSpec
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.util.Random

class MetricDataTimestampExtractorSpec extends UnitTestSpec {

  "MetricDataTimestampExtractor" should {

    "extract timestamp from MetricData" in {

      Given("a metric data with some timestamp")
      val currentTimeInSecs = computeCurrentTimeInSecs
      val metricData = getMetricData(currentTimeInSecs)
      val metricDataTimestampExtractor = new MetricDataTimestampExtractor
      val record: ConsumerRecord[AnyRef, AnyRef] = new ConsumerRecord("dummy-topic", 1, 1, "dummy-key", metricData)

      When("extract timestamp")
      val epochTime = metricDataTimestampExtractor.extract(record, System.currentTimeMillis())

      Then("extracted time should equal metric point time in milliseconds")
      epochTime shouldEqual currentTimeInSecs * 1000
    }
  }

  private def getMetricData(timeStamp : Long): MetricData = {
    val metricDefinition = new MetricDefinition("duration")
    new MetricData(metricDefinition, Random.nextDouble(), timeStamp)
  }

}
