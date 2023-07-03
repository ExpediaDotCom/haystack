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

package com.expedia.www.haystack.trends.feature.tests.aggregation

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.commons.entities.Interval.Interval
import com.expedia.www.haystack.commons.entities.{Interval, TagKeys}
import com.expedia.www.haystack.trends.aggregation.TrendMetric
import com.expedia.www.haystack.trends.aggregation.entities.TimeWindow
import com.expedia.www.haystack.trends.aggregation.metrics.{CountMetric, CountMetricFactory, HistogramMetric, HistogramMetricFactory}
import com.expedia.www.haystack.trends.config.AppConfiguration
import com.expedia.www.haystack.trends.feature.FeatureSpec

class TrendMetricSpec extends FeatureSpec {

  val SERVICE_NAME = "dummy_service"
  val OPERATION_NAME = "dummy_operation"
  val keys = Map(TagKeys.OPERATION_NAME_KEY -> OPERATION_NAME,
    TagKeys.SERVICE_NAME_KEY -> SERVICE_NAME)

  val alternateMetricKeys = Map(TagKeys.OPERATION_NAME_KEY -> OPERATION_NAME.concat("_2"),
    TagKeys.SERVICE_NAME_KEY -> SERVICE_NAME)


  feature("Creating a TrendMetric") {

    scenario("should get Histogram aggregated MetricPoints post watermarked metrics") {
      val DURATION_METRIC_NAME = "duration"

      Given("some duration MetricData points")
      val intervals: List[Interval] = List(Interval.ONE_MINUTE)
      val currentTime = 1
      val expectedMetric: HistogramMetric = new HistogramMetric(Interval.ONE_MINUTE)
      val firstMetricData: MetricData = getMetricData(DURATION_METRIC_NAME, keys, 1, currentTime)

      When("creating a WindowedMetric and passing first MetricData")
      val trendMetric: TrendMetric = TrendMetric.createTrendMetric(intervals, firstMetricData, HistogramMetricFactory)
      trendMetric.compute(firstMetricData)
      expectedMetric.compute(firstMetricData)

      Then("should return 0 MetricData points if we try to get within (watermark + 1) metrics")
      trendMetric.getComputedMetricPoints(firstMetricData).size shouldBe 0
      trendMetric.shouldLogToStateStore shouldBe true
      var i = TrendMetric.trendMetricConfig(intervals.head)._1
      while (i > 0) {
        val secondMetricData: MetricData = getMetricData(DURATION_METRIC_NAME, keys, 2, currentTime + intervals.head.timeInSeconds * i)
        trendMetric.compute(secondMetricData)
        trendMetric.shouldLogToStateStore shouldBe true
        trendMetric.getComputedMetricPoints(secondMetricData).size shouldEqual 0
        i = i - 1
      }

      When("adding another MetricData after watermark")
      val metricDataAfterWatermark: MetricData = getMetricData(DURATION_METRIC_NAME, keys, 10, currentTime + intervals.head.timeInSeconds * (TrendMetric.trendMetricConfig(intervals.head)._1 + 1))
      trendMetric.compute(metricDataAfterWatermark)
      val aggMetrics = trendMetric.getComputedMetricPoints(metricDataAfterWatermark)
      aggMetrics.size shouldEqual 1 * 7 // HistogramMetric

      Then("values for histogram should same as expected")
      expectedMetric.getRunningHistogram.getMean shouldEqual aggMetrics.find(metricData => containsTagInMetricData(metricData, TagKeys.STATS_KEY, "mean")).get.getValue
      expectedMetric.getRunningHistogram.getMaxValue shouldEqual aggMetrics.find(metricData => containsTagInMetricData(metricData, TagKeys.STATS_KEY, "max")).get.getValue
      expectedMetric.getRunningHistogram.getMinValue shouldEqual aggMetrics.find(metricData => containsTagInMetricData(metricData, TagKeys.STATS_KEY, "min")).get.getValue
      expectedMetric.getRunningHistogram.getValueAtPercentile(99) shouldEqual aggMetrics.find(metricData => containsTagInMetricData(metricData, TagKeys.STATS_KEY, "*_99")).get.getValue
      expectedMetric.getRunningHistogram.getValueAtPercentile(95) shouldEqual aggMetrics.find(metricData => containsTagInMetricData(metricData, TagKeys.STATS_KEY, "*_95")).get.getValue
      expectedMetric.getRunningHistogram.getValueAtPercentile(50) shouldEqual aggMetrics.find(metricData => containsTagInMetricData(metricData, TagKeys.STATS_KEY, "*_50")).get.getValue

      Then("timestamp of the evicted metric should equal the endtime of that window")
      aggMetrics.map(metricPoint => {
        metricPoint.getTimestamp shouldEqual TimeWindow(firstMetricData.getTimestamp, intervals.head).endTime
      })
    }

    scenario("should get count aggregated MetricPoint post watermarked metrics") {
      val COUNT_METRIC_NAME = "span-received"

      Given("some count MetricPoints")
      val intervals: List[Interval] = List(Interval.ONE_MINUTE, Interval.FIVE_MINUTE)
      val currentTime = 1

      val firstMetricData: MetricData = getMetricData(COUNT_METRIC_NAME, keys, 1, currentTime)
      val trendMetric = TrendMetric.createTrendMetric(intervals, firstMetricData, CountMetricFactory)
      trendMetric.compute(firstMetricData)
      val expectedMetric: CountMetric = new CountMetric(Interval.FIVE_MINUTE)
      expectedMetric.compute(firstMetricData)

      var i = TrendMetric.trendMetricConfig(intervals.last)._1
      while (i > 0) {
        val secondMetricData: MetricData = getMetricData(COUNT_METRIC_NAME, keys, 1, currentTime + intervals.last.timeInSeconds * i)
        trendMetric.compute(secondMetricData)
        i = i - 1
      }

      When("adding another MetricPoint after watermark")
      val metricDataAfterWatermark: MetricData = getMetricData(COUNT_METRIC_NAME, keys, 10, currentTime + intervals.last.timeInSeconds * (TrendMetric.trendMetricConfig(intervals.head)._1 + 1))
      trendMetric.compute(metricDataAfterWatermark)
      val aggMetrics = trendMetric.getComputedMetricPoints(metricDataAfterWatermark)

      Then("values for count should same as expected")
      expectedMetric.getCurrentCount shouldEqual aggMetrics.find(metricData => containsTagInMetricData(metricData, TagKeys.INTERVAL_KEY, "FiveMinute")).get.getValue
    }

    scenario("should log to state store for different metrics based on timestamp") {
      val COUNT_METRIC_NAME = "span-received"

      Given("multiple metricPoints for different operations")
      val intervals: List[Interval] = List(Interval.ONE_MINUTE, Interval.FIVE_MINUTE)
      val currentTime = 1

      val firstMetricData: MetricData = getMetricData(COUNT_METRIC_NAME, keys, 1, currentTime)
      val anotherMetricData: MetricData = getMetricData(COUNT_METRIC_NAME, alternateMetricKeys, 1, currentTime)
      val trendMetric = TrendMetric.createTrendMetric(intervals, firstMetricData, CountMetricFactory)
      val anotherTrendMetric = TrendMetric.createTrendMetric(intervals, anotherMetricData, CountMetricFactory)
      trendMetric.compute(firstMetricData)
      trendMetric.shouldLogToStateStore shouldBe true

      anotherTrendMetric.compute(anotherMetricData)
      anotherTrendMetric.shouldLogToStateStore shouldBe true

      When("metricpoints are added to multiple trend metrics")
      val secondMetricData: MetricData = getMetricData(COUNT_METRIC_NAME, keys, 1, currentTime + 1 + AppConfiguration.stateStoreConfig.changeLogDelayInSecs)
      trendMetric.compute(secondMetricData)
      val secondAnotherMetricData = getMetricData(COUNT_METRIC_NAME, alternateMetricKeys, 1, currentTime + 1 + AppConfiguration.stateStoreConfig.changeLogDelayInSecs)
      anotherTrendMetric.compute(secondAnotherMetricData)

      Then("trend metric should log to state store")
      trendMetric.shouldLogToStateStore shouldBe true
      anotherTrendMetric.shouldLogToStateStore shouldBe true

    }


  }


}
