package com.expedia.www.haystack.trends.feature.tests.aggregation.metrics

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.commons.entities.Interval.Interval
import com.expedia.www.haystack.commons.entities.{Interval, TagKeys}
import com.expedia.www.haystack.trends.aggregation.metrics.{CountMetric, Metric}
import com.expedia.www.haystack.trends.aggregation.entities._
import com.expedia.www.haystack.trends.feature.FeatureSpec
import scala.collection.JavaConverters._

class CountMetricSpec extends FeatureSpec {

  val DURATION_METRIC_NAME = "duration"
  val SUCCESS_METRIC_NAME = "success-spans"
  val INVALID_METRIC_NAME = "invalid_metric"
  val SERVICE_NAME = "dummy_service"
  val OPERATION_NAME = "dummy_operation"

  val keys = Map(TagKeys.OPERATION_NAME_KEY -> OPERATION_NAME,
    TagKeys.SERVICE_NAME_KEY -> SERVICE_NAME)

  scenario("should compute the correct count for valid similar metric data points") {

    Given("some 'total-spans' metric data points")
    val interval: Interval = Interval.FIFTEEN_MINUTE

    val metricDataList = List(
      getMetricData(SUCCESS_METRIC_NAME, keys, 2, currentTimeInSecs),
      getMetricData(SUCCESS_METRIC_NAME, keys, 4, currentTimeInSecs),
      getMetricData(SUCCESS_METRIC_NAME, keys, 5, currentTimeInSecs))


    When("get metric is constructed")
    val metric: Metric = new CountMetric(interval)

    When("MetricData are processed")
    metricDataList.map(metricData => metric.compute(metricData))

    val countMetricDataList: List[MetricData] = metric.mapToMetricDataList(metricDataList.last.getMetricDefinition.getKey, getTagsFromMetricData(metricDataList.last), metricDataList.last.getTimestamp)


    Then("it should return a single aggregated metric data")
    countMetricDataList.size shouldBe 1
    val countMetric = countMetricDataList.head

    Then("aggregated metric name be the original metric name")
    metricDataList.foreach(metricData => {
      countMetric.getMetricDefinition.getKey shouldEqual metricData.getMetricDefinition.getKey
    })

    Then("aggregated metric should contain of original metric tags")
    metricDataList.foreach(metricData => {
      getTagsFromMetricData(metricData).asScala.foreach(tag => {
        val aggregatedMetricTag = countMetric.getMetricDefinition.getTags.getKv.get(tag._1)
        aggregatedMetricTag should not be None
        aggregatedMetricTag shouldBe tag._2
      })
    })

    Then("aggregated metric name should be the same as the metricpoint name")
    countMetricDataList
      .map(countMetricPoint =>
        countMetricPoint.getMetricDefinition.getKey shouldEqual countMetric.getMetricDefinition.getKey)

    Then("aggregated metric should count metric type in tags")
    getTagsFromMetricData(countMetric).get(TagKeys.STATS_KEY) should not be None
    getTagsFromMetricData(countMetric).get(TagKeys.STATS_KEY) shouldEqual StatValue.COUNT.toString

    Then("aggregated metric should contain the correct interval name in tags")
    getTagsFromMetricData(countMetric).get(TagKeys.INTERVAL_KEY) should not be None
    getTagsFromMetricData(countMetric).get(TagKeys.INTERVAL_KEY) shouldEqual interval.name

    Then("should return valid aggregated value for count")
    val totalSum = metricDataList.foldLeft(0f)((currentValue, point) => {
      currentValue + point.getValue.toFloat
    })
    totalSum shouldEqual countMetric.getValue


  }


}
