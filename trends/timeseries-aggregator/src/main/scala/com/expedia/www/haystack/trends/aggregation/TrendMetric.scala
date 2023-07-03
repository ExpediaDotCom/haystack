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

import com.codahale.metrics.{Meter, Timer}
import com.expedia.metrics.MetricData
import com.expedia.www.haystack.commons.entities.Interval
import com.expedia.www.haystack.commons.entities.Interval.Interval
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trends.aggregation.TrendMetric._
import com.expedia.www.haystack.trends.aggregation.metrics.MetricFactory
import com.expedia.www.haystack.trends.config.AppConfiguration
import org.slf4j.LoggerFactory

import scala.util.Try

/**
  * This class contains a windowedMetric for each interval being computed. The number of time windows at any moment is = no. of intervals
  * depends upon interval, numberOfWatermarkWindows and in which timeWindow incoming metric lies
  *
  * @param trendMetricsMap map containing intervals and windowedMetrics
  * @param metricFactory   factory which is used to create new metrics when required
  */
class TrendMetric private(var trendMetricsMap: Map[Interval, WindowedMetric], metricFactory: MetricFactory) extends MetricsSupport {

  private val trendMetricComputeTimer: Timer = metricRegistry.timer("trendmetric.compute.time")
  private val metricPointComputeFailureMeter: Meter = metricRegistry.meter("metricpoints.compute.failure")
  private var currentEpochTimeInSec: Long = 0
  private var shouldLog = true

  def getMetricFactory: MetricFactory = {
    metricFactory
  }

  /**
    * function to compute the incoming metric data
    * it updates all the metrics for the windows within which the incoming metric point lies
    *
    * @param incomingMetricData - incoming metric data
    */
  def compute(incomingMetricData: MetricData): Unit = {
    val timerContext = trendMetricComputeTimer.time()
    Try {
      //discarding values which are less than 0 assuming they are invalid metric points
      trendMetricsMap.foreach(trendMetrics => {
        val windowedMetric = trendMetrics._2
        windowedMetric.compute(incomingMetricData)
      })
    }.recover {
      case failure: Throwable =>
        metricPointComputeFailureMeter.mark()
        LOGGER.error(s"Failed to compute metricpoint : $incomingMetricData with exception ", failure)
        failure
    }

    // check whether time to log to state store
    if ((incomingMetricData.getTimestamp - currentEpochTimeInSec) > AppConfiguration.stateStoreConfig.changeLogDelayInSecs) {
      currentEpochTimeInSec = incomingMetricData.getTimestamp
      shouldLog = true
    }

    timerContext.close()
  }

  /**
    * returns list of metricPoints which are evicted and their window is closes
    *
    * @return list of evicted metricPoints
    */
  def getComputedMetricPoints(incomingMetricData: MetricData): List[MetricData] = {
    List(trendMetricsMap.flatMap {
      case (_, windowedMetric) =>
        windowedMetric.getComputedMetricDataList(incomingMetricData)
    }).flatten
  }

  /**
    * flag to tell whether we need to log to state store
    *
    * @return flag to indicate should we log
    */
  def shouldLogToStateStore: Boolean = {
    if (shouldLog) {
      shouldLog = false
      return true
    }
    false
  }
}

object TrendMetric {

  private val LOGGER = LoggerFactory.getLogger(this.getClass)


  // config for watermark windows & tick per interval
  val trendMetricConfig = Map(
    Interval.ONE_MINUTE -> (1, 1),
    Interval.FIVE_MINUTE -> (1, 1),
    Interval.FIFTEEN_MINUTE -> (0, 1),
    Interval.ONE_HOUR -> (0, 1))

  def createTrendMetric(intervals: List[Interval],
                        firstMetricData: MetricData,
                        metricFactory: MetricFactory): TrendMetric = {
    //  this enable to log data to state store for the very first time
    val trendMetricMap = createMetricsForEachInterval(intervals, firstMetricData, metricFactory)
    new TrendMetric(trendMetricMap, metricFactory)
  }

  def restoreTrendMetric(trendMetricMap: Map[Interval, WindowedMetric],
                         metricFactory: MetricFactory): TrendMetric = {
    new TrendMetric(trendMetricMap, metricFactory)
  }

  private def createMetricsForEachInterval(intervals: List[Interval],
                                           metricData: MetricData,
                                           metricFactory: MetricFactory): Map[Interval, WindowedMetric] = {
    intervals.map(interval => {
      val windowedMetric = WindowedMetric.createWindowedMetric(metricData, metricFactory, trendMetricConfig(interval)._1, interval)
      interval -> windowedMetric
    }).toMap
  }
}
