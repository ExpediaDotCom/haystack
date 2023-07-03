package com.expedia.www.haystack.trends.integration.tests

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.trends.integration.IntegrationTestSpec
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils

import scala.collection.JavaConverters._

class WatermarkingSpec extends IntegrationTestSpec {

  "TimeSeriesAggregatorTopology" should {
    "watermark metrics for aggregate count type metricPoints from input topic" in {
      Given("a set of metricPoints with type metric and kafka specific configurations")
      val METRIC_NAME = "success-span"
      // CountMetric
      val expectedOneMinAggregatedPoints: Int = 3
      // Why one less -> won't be generated for  last (MAX_METRICPOINTS * 60)th second metric point
      val expectedFiveMinAggregatedPoints: Int = 1
      val expectedFifteenMinAggregatedPoints: Int = 0
      val expectedOneHourAggregatedPoints: Int = 0
      val expectedTotalAggregatedPoints: Int = expectedOneMinAggregatedPoints + expectedFiveMinAggregatedPoints + expectedFifteenMinAggregatedPoints + expectedOneHourAggregatedPoints
      val streamsRunner = createStreamRunner()


      When("metricPoints are produced in 'input' topic async, and kafka-streams topology is started")
      produceMetricData(METRIC_NAME, 1l, 1l)
      produceMetricData(METRIC_NAME, 65l, 2l)
      produceMetricData(METRIC_NAME, 2l, 3l)
      produceMetricData(METRIC_NAME, 130l, 4l)
      produceMetricData(METRIC_NAME, 310l, 5l)
      produceMetricData(METRIC_NAME, 610l, 6l)
      streamsRunner.start()

      Then("we should read all aggregated metricData from 'output' topic")
      val waitTimeMs = 15000
      val result: List[KeyValue[String, MetricData]] =
        IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived[String, MetricData](RESULT_CONSUMER_CONFIG, OUTPUT_TOPIC, expectedTotalAggregatedPoints, waitTimeMs).asScala.toList
      validateAggregatedMetricPoints(result, expectedOneMinAggregatedPoints, expectedFiveMinAggregatedPoints, expectedFifteenMinAggregatedPoints, expectedOneHourAggregatedPoints)
    }
  }
}
