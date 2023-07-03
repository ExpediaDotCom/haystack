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

package com.expedia.www.haystack.trends.aggregation

import java.util.concurrent.TimeUnit

import com.codahale.metrics.{Histogram, Meter}
import com.expedia.metrics.MetricData
import com.expedia.www.haystack.commons.entities.Interval.Interval
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trends.aggregation.entities.TimeWindow
import com.expedia.www.haystack.trends.aggregation.metrics.{Metric, MetricFactory}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.Try

/**
  * This class contains a metric for each time window being computed for a single interval
  *
  * @param windowedMetricsMap map containing  sorted timewindows and metrics for an Interval
  * @param metricFactory      factory which is used to create new metrics when required
  */
class WindowedMetric private(var windowedMetricsMap: mutable.TreeMap[TimeWindow, Metric], metricFactory: MetricFactory, numberOfWatermarkedWindows: Int, interval: Interval) extends MetricsSupport {

  private val disorderedMetricPointMeter: Meter = metricRegistry.meter("metricpoints.disordered")
  private val timeInTopicMetricPointHistogram: Histogram = metricRegistry.histogram("metricpoints.timeInTopic")
  private var computedMetrics = List[(Long, Metric)]()
  private val LOGGER = LoggerFactory.getLogger(this.getClass)

  def getMetricFactory: MetricFactory = {
    metricFactory
  }

  /**
    * function to compute the incoming metric data
    * it updates all the metrics for the windows within which the incoming metric data lies for an interval
    *
    * @param incomingMetricData - incoming metric data
    */
  def compute(incomingMetricData: MetricData): Unit = {
    timeInTopicMetricPointHistogram.update(TimeUnit.SECONDS.toMillis(incomingMetricData.getTimestamp()) - System.currentTimeMillis())

    val incomingMetricPointTimeWindow = TimeWindow.apply(incomingMetricData.getTimestamp, interval)

    val matchedWindowedMetric = windowedMetricsMap.get(incomingMetricPointTimeWindow)

    if (matchedWindowedMetric.isDefined) {
      // an existing metric
      matchedWindowedMetric.get.compute(incomingMetricData)
    } else {
      // incoming metric is a new metric
      if (incomingMetricPointTimeWindow.compare(windowedMetricsMap.firstKey) > 0) {
        // incoming metric's time is more that minimum (first) time window
        createNewMetric(incomingMetricPointTimeWindow, incomingMetricData)
        evictMetric()
      } else {
        // disordered metric
        disorderedMetricPointMeter.mark()
      }
    }
  }

  private def createNewMetric(incomingMetricPointTimeWindow: TimeWindow, incomingMetricData: MetricData) = {
    val newMetric = metricFactory.createMetric(interval)
    newMetric.compute(incomingMetricData)
    windowedMetricsMap.put(incomingMetricPointTimeWindow, newMetric)
  }

  private def evictMetric() = {
    if (windowedMetricsMap.size > (numberOfWatermarkedWindows + 1)) {
      val evictInterval = windowedMetricsMap.firstKey
      windowedMetricsMap.remove(evictInterval).foreach { evictedMetric =>
        computedMetrics = (evictInterval.endTime, evictedMetric) :: computedMetrics
      }
    }
  }

  /**
    * returns list of metricData which are evicted and their window is closes
    *
    * @return list of evicted metricData
    */
  def getComputedMetricDataList(incomingMetricData: MetricData): List[MetricData] = {
    val metricDataList = computedMetrics.flatMap {
      case (publishTime, metric) =>
        metric.mapToMetricDataList(incomingMetricData.getMetricDefinition.getKey, incomingMetricData.getMetricDefinition.getTags.getKv, publishTime)
    }
    computedMetrics = List[(Long, Metric)]()
    metricDataList
  }
}

/**
  * Windowed metric factory which can create a new windowed metric or restore an existing windowed metric for an interval
  */
object WindowedMetric {

  private val LOGGER = LoggerFactory.getLogger(this.getClass)

  def createWindowedMetric(firstMetricData: MetricData, metricFactory: MetricFactory, watermarkedWindows: Int, interval: Interval): WindowedMetric = {
    val windowedMetricMap = mutable.TreeMap[TimeWindow, Metric]()
    windowedMetricMap.put(TimeWindow.apply(firstMetricData.getTimestamp, interval), metricFactory.createMetric(interval))
    new WindowedMetric(windowedMetricMap, metricFactory, watermarkedWindows, interval)
  }

  def restoreWindowedMetric(windowedMetricsMap: mutable.TreeMap[TimeWindow, Metric], metricFactory: MetricFactory, watermarkedWindows: Int, interval: Interval): WindowedMetric = {
    new WindowedMetric(windowedMetricsMap, metricFactory, watermarkedWindows, interval)
  }
}
