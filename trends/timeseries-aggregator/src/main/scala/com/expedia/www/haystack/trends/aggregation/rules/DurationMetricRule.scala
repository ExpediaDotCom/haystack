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

import com.expedia.metrics.{MetricData, MetricDefinition}
import com.expedia.www.haystack.trends.aggregation.metrics.AggregationType
import com.expedia.www.haystack.trends.aggregation.metrics.AggregationType.AggregationType

/**
  * This Rule applies a Histogram aggregation type when the incoming metric point's name is duration and is of type gauge
  */
trait DurationMetricRule extends MetricRule {
  override def isMatched(metricData: MetricData): Option[AggregationType] = {
    if (metricData.getMetricDefinition.getKey.toLowerCase.contains("duration") && containsTag(metricData,MetricDefinition.MTYPE, MTYPE_GAUGE)) {
      Some(AggregationType.Histogram)
    } else {
      super.isMatched(metricData)
    }
  }
}
