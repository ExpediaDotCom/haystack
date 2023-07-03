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

package com.expedia.www.haystack.trends.aggregation.metrics

import java._

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.commons.entities.Interval.Interval
import com.expedia.www.haystack.commons.entities.TagKeys
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trends.aggregation.entities.StatValue.StatValue
import com.expedia.www.haystack.trends.aggregation.metrics.AggregationType.AggregationType
import com.expedia.www.haystack.trends.kstream.serde.metric.MetricSerde

abstract class Metric(interval: Interval) extends MetricsSupport {

  /**
    * function to compute the incoming metric-data
    *
    * @param value - incoming metric data
    * @return : returns the metric (in most cases it should return the same object(this) but returning a metric gives the metric implementation class to create an immutable metric)
    */
  def compute(value: MetricData): Metric

  def getMetricInterval: Interval = {
    interval
  }


  /**
    * This function returns the metric points which contains the current snapshot of the metric
    *
    * @param publishingTimestamp : timestamp in seconds which the consumer wants to be used as the timestamps of these published metricpoints
    * @param metricKey           : the name of the metricData to be generated
    * @param tags                : tags to be associated with the metricData
    * @return list of published metricdata
    */
  def mapToMetricDataList(metricKey: String, tags: util.Map[String, String], publishingTimestamp: Long): List[MetricData]

  protected def appendTags(tags: util.Map[String, String], interval: Interval, statValue: StatValue): util.Map[String, String] = {
    new util.LinkedHashMap[String, String] {
      putAll(tags)
      put(TagKeys.INTERVAL_KEY, interval.name)
      put(TagKeys.STATS_KEY, statValue.toString)
    }
  }

}

/**
  * The enum contains the support aggregation type, which is currently count and histogram
  */
object AggregationType extends Enumeration {
  type AggregationType = Value
  val Count, Histogram = Value
}


/**
  * This trait is supposed to be created by every metric class which lets them create the metric when ever required
  */
trait MetricFactory {
  def createMetric(interval: Interval): Metric

  def getAggregationType: AggregationType

  def getMetricSerde: MetricSerde
}
