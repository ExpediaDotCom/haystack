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

import com.expedia.open.tracing.Tag.TagType
import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.commons.entities.TagKeys
import com.expedia.www.haystack.trends.feature.FeatureSpec
import com.expedia.www.haystack.trends.transformer.SpanStatusMetricDataTransformer

class SpanStatusMetricDataTransformerSpec extends FeatureSpec with SpanStatusMetricDataTransformer {

  feature("metricData transformer for creating status count metricData") {

    scenario("should have a success-spans metricData given span which is successful " +
      "and when service level generation is enabled") {

      Given("a successful span object")
      val operationName = "testSpan"
      val serviceName = "testService"
      val duration = System.currentTimeMillis
      val span = Span.newBuilder()
        .setDuration(duration)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setType(TagType.BOOL).setVBool(false))
        .build()

      When("metricData is created using the transformer")
      val metricDataList = mapSpan(span, true)

      Then("should only have 2 metricData")
      metricDataList.length shouldEqual 2

      Then("the metricData value should be 1")
      metricDataList(0).getValue shouldEqual 1
      metricDataList(1).getValue shouldEqual 1

      Then("metric name should be success-spans")
      metricDataList(0).getMetricDefinition.getKey shouldEqual SUCCESS_METRIC_NAME

      Then("returned keys should be as expected")
      getMetricDataTags(metricDataList.head).get(TagKeys.SERVICE_NAME_KEY) shouldEqual encoder.encode(span.getServiceName)
      getMetricDataTags(metricDataList.head).get(TagKeys.OPERATION_NAME_KEY) shouldEqual encoder.encode(span.getOperationName)
      getMetricDataTags(metricDataList.reverse.head).get(TagKeys.SERVICE_NAME_KEY) shouldEqual encoder.encode(span.getServiceName)
      getMetricDataTags(metricDataList.reverse.head).get(TagKeys.OPERATION_NAME_KEY) shouldEqual null
    }

    scenario("should have a failure-spans metricData given span which is erroneous " +
      "and when service level generation is enabled") {

      Given("a erroneous span object")
      val operationName = "testSpan"
      val serviceName = "testService"
      val duration = System.currentTimeMillis
      val span = Span.newBuilder()
        .setDuration(duration)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setType(TagType.BOOL).setVBool(true))
        .build()

      When("metricData is created using transformer")
      val metricDataList = mapSpan(span, true)

      Then("should only have 2 metricData")
      metricDataList.length shouldEqual 2

      Then("the metricData value should be 1")
      metricDataList(0).getValue shouldEqual 1
      metricDataList(1).getValue shouldEqual 1

      Then("metric name should be failure-spans")
      metricDataList(0).getMetricDefinition.getKey shouldEqual FAILURE_METRIC_NAME
    }

    scenario("should have a failure-span metricData if the error tag is a true string") {
      Given("a failure span object")
      val operationName = "testSpan"
      val serviceName = "testService"
      val duration = System.currentTimeMillis
      val span = Span.newBuilder()
        .setDuration(duration)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVStr("true"))
        .build()

      When("metricData is created using transformer")
      val metricDataList = mapSpan(span, true)

      Then("metric name should be failure-spans")
      metricDataList(0).getMetricDefinition.getKey shouldEqual FAILURE_METRIC_NAME
    }

    scenario("should have a failure-span metricData if the error tag not a false string") {
      Given("a failure span object")
      val operationName = "testSpan"
      val serviceName = "testService"
      val duration = System.currentTimeMillis
      val span = Span.newBuilder()
        .setDuration(duration)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVStr("500"))
        .build()

      When("metricData is created using transformer")
      val metricDataList = mapSpan(span, true)

      Then("metric name should be failure-spans")
      metricDataList(0).getMetricDefinition.getKey shouldEqual FAILURE_METRIC_NAME
    }

    scenario("should have a failure-span metricData if the error tag exists but is not a boolean or string") {
      Given("a failure span object")
      val operationName = "testSpan"
      val serviceName = "testService"
      val duration = System.currentTimeMillis
      val span = Span.newBuilder()
        .setDuration(duration)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setType(TagType.LONG).setVLong(100L))
        .build()

      When("metricData is created using transformer")
      val metricDataList = mapSpan(span, true)

      Then("metric name should be failure-spans")
      metricDataList(0).getMetricDefinition.getKey shouldEqual FAILURE_METRIC_NAME
    }

    scenario("should return a success span when error key is missing in span tags and when service level generation is enabled") {

      Given("a span object which missing error tag")
      val operationName = "testSpan"
      val serviceName = "testService"
      val duration = System.currentTimeMillis
      val span = Span.newBuilder()
        .setDuration(duration)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .build()

      When("metricData is created using transformer")
      val metricDataList = mapSpan(span, true)

      Then("should return metricData List")
      metricDataList.length shouldEqual 2
      metricDataList(0).getMetricDefinition.getKey shouldEqual SUCCESS_METRIC_NAME
      metricDataList(1).getMetricDefinition.getKey shouldEqual SUCCESS_METRIC_NAME
    }

    scenario("should have a success-spans metricData given span which is successful " +
      "and when service level generation is disabled") {

      Given("a successful span object")
      val operationName = "testSpan"
      val serviceName = "testService"
      val duration = System.currentTimeMillis
      val span = Span.newBuilder()
        .setDuration(duration)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setType(TagType.BOOL).setVBool(false))
        .build()

      When("metricData is created using the transformer")
      val metricDataList = mapSpan(span, false)

      Then("should only have 1 metricData")
      metricDataList.length shouldEqual 1

      Then("the metricData value should be 1")
      metricDataList(0).getValue shouldEqual 1

      Then("metric name should be success-spans")
      metricDataList(0).getMetricDefinition.getKey shouldEqual SUCCESS_METRIC_NAME

      Then("returned keys should be as expected")
      getMetricDataTags(metricDataList.head).get(TagKeys.SERVICE_NAME_KEY) shouldEqual encoder.encode(span.getServiceName)
      getMetricDataTags(metricDataList.head).get(TagKeys.OPERATION_NAME_KEY) shouldEqual encoder.encode(span.getOperationName)
    }

    scenario("should have a failure-spans metricData given span  which is erroneous " +
      "and when service level generation is disabled") {

      Given("a erroneous span object")
      val operationName = "testSpan"
      val serviceName = "testService"
      val duration = System.currentTimeMillis
      val span = Span.newBuilder()
        .setDuration(duration)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setType(TagType.BOOL).setVBool(true))
        .build()

      When("metricData is created using transformer")
      val metricDataList = mapSpan(span, false)

      Then("should only have 1 metricData")
      metricDataList.length shouldEqual 1

      Then("the metricData value should be 1")
      metricDataList(0).getValue shouldEqual 1

      Then("metric name should be failure-spans")
      metricDataList(0).getMetricDefinition.getKey shouldEqual FAILURE_METRIC_NAME
    }

    scenario("should return a success span when error key is missing in span tags and when service level generation is disabled") {

      Given("a span object which missing error tag")
      val operationName = "testSpan"
      val serviceName = "testService"
      val duration = System.currentTimeMillis
      val span = Span.newBuilder()
        .setDuration(duration)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .build()

      When("metricData is created using transformer")
      val metricPoints = mapSpan(span, false)

      Then("should return metricData")
      metricPoints.length shouldEqual 1
      metricPoints(0).getMetricDefinition.getKey shouldEqual SUCCESS_METRIC_NAME
    }
  }
}
