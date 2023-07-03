package com.expedia.www.haystack.trends.feature.tests.kstreams.serde

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.commons.entities.{Interval, TagKeys}
import com.expedia.www.haystack.trends.aggregation.WindowedMetric
import com.expedia.www.haystack.trends.aggregation.metrics.{CountMetric, CountMetricFactory, HistogramMetric, HistogramMetricFactory}
import com.expedia.www.haystack.trends.feature.FeatureSpec
import com.expedia.www.haystack.trends.kstream.serde.WindowedMetricSerde

class WindowedMetricSerdeSpec extends FeatureSpec {

  val DURATION_METRIC_NAME = "duration"
  val SUCCESS_METRIC_NAME = "success-spans"
  val SERVICE_NAME = "dummy_service"
  val TOPIC_NAME = "dummy"
  val OPERATION_NAME = "dummy_operation"
  val keys = Map(TagKeys.OPERATION_NAME_KEY -> OPERATION_NAME,
    TagKeys.SERVICE_NAME_KEY -> SERVICE_NAME)

  feature("Serializing/Deserializing Windowed Metric") {

    scenario("should be able to serialize and deserialize a valid windowed metric computing histograms") {
      val durations: List[Long] = List(10, 140)
      val metricPoints: List[MetricData] = durations.map(duration => getMetricData(DURATION_METRIC_NAME, keys, duration, currentTimeInSecs))

      When("creating a WindowedMetric and passing some MetricPoints and aggregation type as Histogram")
      val windowedMetric: WindowedMetric = WindowedMetric.createWindowedMetric(metricPoints.head, HistogramMetricFactory, 1, Interval.ONE_MINUTE)
      metricPoints.indices.foreach(i => if (i > 0) {
        windowedMetric.compute(metricPoints(i))
      })

      When("the windowed metric is serialized and then deserialized back")
      val serializedMetric = WindowedMetricSerde.serializer().serialize(TOPIC_NAME, windowedMetric)
      val deserializedMetric = WindowedMetricSerde.deserializer().deserialize(TOPIC_NAME, serializedMetric)

      Then("Then it should deserialize the metric back in the same state")
      deserializedMetric should not be null
      windowedMetric.windowedMetricsMap.map {
        case (window, metric) =>
          deserializedMetric.windowedMetricsMap.get(window) should not be None

          val histogram = metric.asInstanceOf[HistogramMetric]
          val deserializedHistogram = deserializedMetric.windowedMetricsMap(window).asInstanceOf[HistogramMetric]
          histogram.getMetricInterval shouldEqual deserializedHistogram.getMetricInterval
          histogram.getRunningHistogram.getTotalCount shouldEqual deserializedHistogram.getRunningHistogram.getTotalCount
          histogram.getRunningHistogram.getMaxValue shouldEqual deserializedHistogram.getRunningHistogram.getMaxValue
          histogram.getRunningHistogram.getMinValue shouldEqual deserializedHistogram.getRunningHistogram.getMinValue
          histogram.getRunningHistogram.getValueAtPercentile(99) shouldEqual deserializedHistogram.getRunningHistogram.getValueAtPercentile(99)
      }
    }

    scenario("should be able to serialize and deserialize a valid windowed metric computing counts") {

      Given("some count Metric points")
      val counts: List[Long] = List(10, 140)
      val metricPoints: List[MetricData] = counts.map(count => getMetricData(SUCCESS_METRIC_NAME, keys, count, currentTimeInSecs))


      When("creating a WindowedMetric and passing some MetricPoints and aggregation type as Count")
      val windowedMetric: WindowedMetric = WindowedMetric.createWindowedMetric(metricPoints.head, CountMetricFactory, 1, Interval.ONE_MINUTE)
      metricPoints.indices.foreach(i => if (i > 0) {
        windowedMetric.compute(metricPoints(i))
      })

      When("the windowed metric is serialized and then deserialized back")
      val serializer = WindowedMetricSerde.serializer()
      val deserializer = WindowedMetricSerde.deserializer()
      val serializedMetric = serializer.serialize(TOPIC_NAME, windowedMetric)
      val deserializedMetric = deserializer.deserialize(TOPIC_NAME, serializedMetric)


      Then("Then it should deserialize the metric back in the same state")
      deserializedMetric should not be null
      windowedMetric.windowedMetricsMap.map {
        case (window, metric) =>
          deserializedMetric.windowedMetricsMap.get(window) should not be None

          val countMetric = metric.asInstanceOf[CountMetric]
          val deserializedCountMetric = deserializedMetric.windowedMetricsMap(window).asInstanceOf[CountMetric]
          countMetric.getMetricInterval shouldEqual deserializedCountMetric.getMetricInterval
          countMetric.getCurrentCount shouldEqual deserializedCountMetric.getCurrentCount
      }
      serializer.close()
      deserializer.close()
      WindowedMetricSerde.close()
    }
  }
}
