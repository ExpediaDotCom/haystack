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

package com.expedia.www.haystack.commons.metrics

import com.codahale.metrics.Gauge
import com.expedia.www.haystack.commons.unit.UnitTestSpec
import com.expedia.www.haystack.commons.metrics.MetricsRegistries._

class MetricRegistySpec extends UnitTestSpec with MetricsSupport {

  "MetricRegisty extension" should {

    "return the same gauge metric if its created more than once" in {
      Given("gauge metric")
      val metricName = "testMetric"
      val gaugeMetric = new Gauge[Long] {
        override def getValue: Long = this.hashCode()
      }

      When("its registered more than once")
      val firstAttempt = metricRegistry.getOrAddGauge(metricName, gaugeMetric)
      val secondAttempt = metricRegistry.getOrAddGauge(metricName, gaugeMetric)


      Then("the first time it should create a new metric and register in the metrics registry")
      firstAttempt shouldBe true

      Then("the second time it shouldn't create the same metric ")
      secondAttempt shouldBe false
    }
  }
}
