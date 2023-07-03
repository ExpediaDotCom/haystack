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

package com.expedia.www.haystack.trends.aggregation.rules

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.trends.aggregation.metrics.AggregationType.AggregationType


/**
  * This Metric Rule engine applies all the metric rules it extends from right to left(http://jim-mcbeath.blogspot.in/2009/08/scala-class-linearization.html).
  * it returns None if none of the rules are applicable.
  * to add another rule, create a rule trait and add it to the with clause in the engine.
  * If multiple rules match the rightmost rule is applied
  */
trait MetricRuleEngine extends LatencyMetricRule with DurationMetricRule with FailureMetricRule with SuccessMetricRule {

  def findMatchingMetric(metricData: MetricData): Option[AggregationType] = {
    isMatched(metricData)
  }
}
