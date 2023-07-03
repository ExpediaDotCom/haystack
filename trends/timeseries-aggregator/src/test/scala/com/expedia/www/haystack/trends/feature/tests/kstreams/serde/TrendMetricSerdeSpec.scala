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
package com.expedia.www.haystack.trends.feature.tests.kstreams.serde

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.commons.entities.Interval.Interval
import com.expedia.www.haystack.commons.entities.{Interval, TagKeys}
import com.expedia.www.haystack.trends.aggregation.TrendMetric
import com.expedia.www.haystack.trends.aggregation.metrics.{CountMetric, CountMetricFactory, HistogramMetric, HistogramMetricFactory}
import com.expedia.www.haystack.trends.feature.FeatureSpec
import com.expedia.www.haystack.trends.kstream.serde.TrendMetricSerde

class TrendMetricSerdeSpec extends FeatureSpec {

  val DURATION_METRIC_NAME = "duration"
  val SUCCESS_METRIC_NAME = "success-spans"
  val SERVICE_NAME = "dummy_service"
  val TOPIC_NAME = "dummy"
  val OPERATION_NAME = "dummy_operation"
  val keys = Map(TagKeys.OPERATION_NAME_KEY -> OPERATION_NAME,
    TagKeys.SERVICE_NAME_KEY -> SERVICE_NAME)
  val currentTime = 0

  feature("Serializing/Deserializing Trend Metric") {

    scenario("should be able to serialize and deserialize a valid trend metric computing histograms") {
      val durations: List[Long] = List(10, 140)
      val intervals: List[Interval] = List(Interval.ONE_MINUTE, Interval.FIFTEEN_MINUTE)

      val metricPoints: List[MetricData] = durations.map(duration => getMetricData(DURATION_METRIC_NAME, keys, duration, currentTime))

      When("creating a TrendMetric and passing some MetricPoints and aggregation type as Histogram")
      val trendMetric: TrendMetric = TrendMetric.createTrendMetric(intervals, metricPoints.head, HistogramMetricFactory)
      metricPoints.indices.foreach(i => if (i > 0) {
        trendMetric.compute(metricPoints(i))
      })

      When("the trend metric is serialized and then deserialized back")
      val serializedMetric = TrendMetricSerde.serializer().serialize(TOPIC_NAME, trendMetric)
      val deserializedMetric = TrendMetricSerde.deserializer().deserialize(TOPIC_NAME, serializedMetric)

      Then("Then it should deserialize the metric back in the same state")
      deserializedMetric should not be null
      trendMetric.trendMetricsMap.map {
        case (interval, windowedMetric) =>
          deserializedMetric.trendMetricsMap.get(interval) should not be None
          windowedMetric.windowedMetricsMap.map {
            case (timeWindow, metric) =>
              val histogram = metric.asInstanceOf[HistogramMetric]
              val deserializedHistogram = deserializedMetric.trendMetricsMap(interval).windowedMetricsMap(timeWindow).asInstanceOf[HistogramMetric]
              histogram.getMetricInterval shouldEqual deserializedHistogram.getMetricInterval
              histogram.getRunningHistogram.getTotalCount shouldEqual deserializedHistogram.getRunningHistogram.getTotalCount
              histogram.getRunningHistogram.getMaxValue shouldEqual deserializedHistogram.getRunningHistogram.getMaxValue
              histogram.getRunningHistogram.getValueAtPercentile(50) shouldEqual deserializedHistogram.getRunningHistogram.getValueAtPercentile(50)
          }
      }
    }

    scenario("should be able to serialize and deserialize a valid trend metric computing counts") {

      Given("some count Metric points")
      val counts: List[Long] = List(10, 140)
      val intervals: List[Interval] = List(Interval.ONE_MINUTE, Interval.FIFTEEN_MINUTE)
      val metricPoints: List[MetricData] = counts.map(count => getMetricData(SUCCESS_METRIC_NAME, keys, count, currentTime))


      When("creating a TrendMetric and passing some MetricPoints and aggregation type as Count")
      val trendMetric: TrendMetric = TrendMetric.createTrendMetric(intervals, metricPoints.head, CountMetricFactory)
      metricPoints.indices.foreach(i => if (i > 0) {
        trendMetric.compute(metricPoints(i))
      })

      When("the trend metric is serialized and then deserialized back")
      val serializer = TrendMetricSerde.serializer()
      val deserializer = TrendMetricSerde.deserializer()
      val serializedMetric = serializer.serialize(TOPIC_NAME, trendMetric)
      val deserializedMetric = deserializer.deserialize(TOPIC_NAME, serializedMetric)


      Then("Then it should deserialize the metric back in the same state")
      deserializedMetric should not be null
      trendMetric.trendMetricsMap.map {
        case (interval, windowedMetric) =>
          deserializedMetric.trendMetricsMap.get(interval) should not be None
          windowedMetric.windowedMetricsMap.map {
            case (timeWindow, metric) =>

              val countMetric = metric.asInstanceOf[CountMetric]
              val deserializedCountMetric = deserializedMetric.trendMetricsMap(interval).windowedMetricsMap(timeWindow).asInstanceOf[CountMetric]
              countMetric.getMetricInterval shouldEqual deserializedCountMetric.getMetricInterval
              countMetric.getCurrentCount shouldEqual deserializedCountMetric.getCurrentCount
          }
      }
      serializer.close()
      deserializer.close()
      TrendMetricSerde.close()
    }
  }
}
