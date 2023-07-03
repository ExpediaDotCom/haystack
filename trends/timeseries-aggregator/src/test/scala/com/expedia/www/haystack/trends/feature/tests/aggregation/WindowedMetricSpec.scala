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
import com.expedia.www.haystack.commons.entities.Interval
import com.expedia.www.haystack.commons.entities.Interval.Interval
import com.expedia.www.haystack.trends.aggregation.WindowedMetric
import com.expedia.www.haystack.trends.aggregation.metrics.{CountMetric, HistogramMetric, HistogramMetricFactory}
import com.expedia.www.haystack.trends.feature.FeatureSpec

class WindowedMetricSpec extends FeatureSpec {

  val DURATION_METRIC_NAME = "duration"
  val SERVICE_NAME = "dummy_service"
  val OPERATION_NAME = "dummy_operation"
  val keys = Map(TagKeys.OPERATION_NAME_KEY -> OPERATION_NAME,
    TagKeys.SERVICE_NAME_KEY -> SERVICE_NAME)

  object TagKeys {
    val OPERATION_NAME_KEY = "operationName"
    val SERVICE_NAME_KEY = "serviceName"
  }

  feature("Creating a WindowedMetric") {

    scenario("should get aggregated MetricData List post watermarked metrics") {

      Given("some duration MetricData")
      val durations: List[Long] = List(10, 140)
      val intervals: List[Interval] = List(Interval.ONE_MINUTE, Interval.FIFTEEN_MINUTE)

      val metricDataList: List[MetricData] = durations.map(duration => getMetricData(DURATION_METRIC_NAME, keys, duration, currentTimeInSecs))

      When("creating a WindowedMetric and passing some MetricData and aggregation type as Histogram")
      val windowedMetric: WindowedMetric = WindowedMetric.createWindowedMetric(metricDataList.head, HistogramMetricFactory, watermarkedWindows = 1, Interval.ONE_MINUTE)

      metricDataList.indices.foreach(i => if (i > 0) {
        windowedMetric.compute(metricDataList(i))
      })

      val expectedMetric: HistogramMetric = new HistogramMetric(Interval.ONE_MINUTE)
      metricDataList.foreach(metricData => expectedMetric.compute(metricData))

      Then("should return 0 Metric Data Points if we try to get it before interval")
      val aggregatedMetricPointsBefore: List[MetricData] = windowedMetric.getComputedMetricDataList(metricDataList.last)
      aggregatedMetricPointsBefore.size shouldBe 0

      When("adding a MetricData outside of first Interval")
      val newMetricPointAfterFirstInterval: MetricData = getMetricData(DURATION_METRIC_NAME, keys, 80, currentTimeInSecs + intervals.head.timeInSeconds)

      windowedMetric.compute(newMetricPointAfterFirstInterval)

      val aggregatedMetricPointsAfterFirstInterval: List[MetricData] = windowedMetric.getComputedMetricDataList(metricDataList.last)

      //Have to fix dev code and then all the validation test
      Then("should return the metric data for the previous interval")


      When("adding a MetricData outside of second interval now")
      expectedMetric.compute(newMetricPointAfterFirstInterval)
      val newMetricPointAfterSecondInterval: MetricData = getMetricData(DURATION_METRIC_NAME, keys, 80, currentTimeInSecs + intervals(1).timeInSeconds)
      windowedMetric.compute(newMetricPointAfterSecondInterval)
      val aggregatedMetricPointsAfterSecondInterval: List[MetricData] = windowedMetric.getComputedMetricDataList(metricDataList.last)

      //Have to fix dev code and then all the validation test
      Then("should return the metric points for the second interval")
    }

    scenario("should skip aggregated MetricData List for duration values greater than permissible value post watermarked metrics") {

      Given("duration MetricData with duration values greater than permissible value")
      val durations: List[Double] = List(4.576661E9, 5.57661E9)
      val intervals: List[Interval] = List(Interval.ONE_MINUTE, Interval.FIFTEEN_MINUTE)

      val metricDataList: List[MetricData] = durations.map(duration => getMetricData(DURATION_METRIC_NAME, keys, duration, currentTimeInSecs))

      When("creating a WindowedMetric and passing some MetricData and aggregation type as Histogram")
      val windowedMetric: WindowedMetric = WindowedMetric.createWindowedMetric(metricDataList.head, HistogramMetricFactory, watermarkedWindows = 1, Interval.ONE_MINUTE)

      metricDataList.indices.foreach(i => if (i > 0) {
        windowedMetric.compute(metricDataList(i))
      })

      val expectedMetric: HistogramMetric = new HistogramMetric(Interval.ONE_MINUTE)
      metricDataList.foreach(metricData => expectedMetric.compute(metricData))

      Then("should return 0 Metric Data Points if we try to get it before interval")
      val aggregatedMetricPointsBefore: List[MetricData] = windowedMetric.getComputedMetricDataList(metricDataList.last)
      aggregatedMetricPointsBefore.size shouldBe 0

      When("adding a MetricData outside of first Interval")
      val newMetricPointAfterFirstInterval: MetricData = getMetricData(DURATION_METRIC_NAME, keys, 80, currentTimeInSecs + intervals.head.timeInSeconds)

      windowedMetric.compute(newMetricPointAfterFirstInterval)

      val aggregatedMetricPointsAfterFirstInterval: List[MetricData] = windowedMetric.getComputedMetricDataList(metricDataList.last)

      Then("should return the empty metric data for the previous interval")
      aggregatedMetricPointsAfterFirstInterval.length shouldBe 0


      When("adding a MetricData outside of second interval now")
      expectedMetric.compute(newMetricPointAfterFirstInterval)
      val newMetricPointAfterSecondInterval: MetricData = getMetricData(DURATION_METRIC_NAME, keys, 80, currentTimeInSecs + intervals(1).timeInSeconds)
      windowedMetric.compute(newMetricPointAfterSecondInterval)
      val aggregatedMetricPointsAfterSecondInterval: List[MetricData] = windowedMetric.getComputedMetricDataList(metricDataList.last)

      //Have to fix dev code and then all the validation test
      Then("should return the metric points for the second interval")
    }

    scenario("should get aggregated MetricData points post maximum Interval") {

      Given("some duration MetricData points")
      val durations: List[Long] = List(10, 140, 250)
      val intervals: List[Interval] = List(Interval.ONE_MINUTE, Interval.FIFTEEN_MINUTE, Interval.ONE_HOUR)

      val metricDataList: List[MetricData] = durations.map(duration => getMetricData(DURATION_METRIC_NAME, keys, duration, currentTimeInSecs))


      When("creating a WindowedMetric and passing some MetricData points")
      val windowedMetric: WindowedMetric = WindowedMetric.createWindowedMetric(metricDataList.head, HistogramMetricFactory, watermarkedWindows = 1, Interval.ONE_MINUTE)

      metricDataList.indices.foreach(i => if (i > 0) {
        windowedMetric.compute(metricDataList(i))
      })

      When("adding a MetricData point outside of max Interval")
      val newMetricPointAfterMaxInterval: MetricData = getMetricData(DURATION_METRIC_NAME, keys, 80, currentTimeInSecs + intervals.last.timeInSeconds)
      windowedMetric.compute(newMetricPointAfterMaxInterval)
      val aggregatedMetricDataPointsAfterMaxInterval: List[MetricData] = windowedMetric.getComputedMetricDataList(metricDataList.last)

      Then("should return valid values for all count intervals")

      val expectedOneMinuteMetric: CountMetric = new CountMetric(Interval.ONE_MINUTE)
      metricDataList.foreach(metricPoint => expectedOneMinuteMetric.compute(metricPoint))

      val expectedFifteenMinuteMetric: CountMetric = new CountMetric(Interval.FIFTEEN_MINUTE)
      metricDataList.foreach(metricPoint => expectedFifteenMinuteMetric.compute(metricPoint))

      val expectedOneHourMetric: CountMetric = new CountMetric(Interval.ONE_HOUR)
      metricDataList.foreach(metricPoint => expectedOneHourMetric.compute(metricPoint))
    }
  }

}
