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
package com.expedia.www.haystack.trends.feature.tests.transformer

import com.expedia.www.haystack.commons.entities.TagKeys
import com.expedia.www.haystack.commons.entities.encoders.PeriodReplacementEncoder
import com.expedia.www.haystack.trends.feature.FeatureSpec
import com.expedia.www.haystack.trends.transformer.SpanDurationMetricDataTransformer

class SpanDurationMetricDataTransformerSpec extends FeatureSpec with SpanDurationMetricDataTransformer {

  feature("metricData transformer for creating duration metricData") {
    scenario("should have duration value in metricData for given duration in span " +
      "and when service level generation is enabled") {

      Given("a valid span object")
      val duration = System.currentTimeMillis
      val span = generateTestSpan(duration)

      When("metricPoint is created using transformer")
      val metricDataList = mapSpan(span, true)

      Then("should only have 2 metricPoint")
      metricDataList.length shouldEqual 2

      Then("same duration should be in metricPoint value")
      metricDataList.head.getValue shouldEqual duration


      Then("the metric name should be duration")
      metricDataList.head.getMetricDefinition.getKey shouldEqual DURATION_METRIC_NAME

      Then("returned keys should be as expected")
      getMetricDataTags(metricDataList.head).get(TagKeys.SERVICE_NAME_KEY) shouldEqual encoder.encode(span.getServiceName)
      getMetricDataTags(metricDataList.head).get(TagKeys.OPERATION_NAME_KEY) shouldEqual encoder.encode(span.getOperationName)
      getMetricDataTags(metricDataList.reverse.head).get(TagKeys.SERVICE_NAME_KEY) shouldEqual encoder.encode(span.getServiceName)
      getMetricDataTags(metricDataList.reverse.head).get(TagKeys.OPERATION_NAME_KEY) shouldEqual null

    }

    scenario("should have duration value in metricPoint for given duration in span " +
      "and when service level generation is disabled") {

      Given("a valid span object")
      val duration = System.currentTimeMillis
      val span = generateTestSpan(duration)

      When("metricData is created using transformer")
      val metricDataList = mapSpan(span, false)

      Then("should only have 1 metricPoint")
      metricDataList.length shouldEqual 1

      Then("same duration should be in metricPoint value")
      metricDataList.head.getValue shouldEqual duration


      Then("the metric name should be duration")
      metricDataList.head.getMetricDefinition.getKey shouldEqual DURATION_METRIC_NAME

      Then("returned keys should be as expected")
      getMetricDataTags(metricDataList.head).get(TagKeys.SERVICE_NAME_KEY) shouldEqual encoder.encode(span.getServiceName)
      getMetricDataTags(metricDataList.head).get(TagKeys.OPERATION_NAME_KEY) shouldEqual encoder.encode(span.getOperationName)
    }
  }
}
