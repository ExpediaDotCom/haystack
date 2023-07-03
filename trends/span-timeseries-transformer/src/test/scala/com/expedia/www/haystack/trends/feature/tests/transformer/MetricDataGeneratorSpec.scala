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

import com.expedia.metrics.MetricDefinition
import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.commons.entities.TagKeys
import com.expedia.www.haystack.commons.entities.encoders.PeriodReplacementEncoder
import com.expedia.www.haystack.trends.MetricDataGenerator
import com.expedia.www.haystack.trends.feature.FeatureSpec
import com.expedia.www.haystack.trends.transformer.{SpanDurationMetricDataTransformer, SpanStatusMetricDataTransformer}

import scala.collection.JavaConverters._
import scala.util.matching.Regex


class MetricDataGeneratorSpec extends FeatureSpec with MetricDataGenerator {

  private def getMetricDataTransformers = {
    List(SpanDurationMetricDataTransformer, SpanStatusMetricDataTransformer)
  }

  feature("The metricData generator must generate list of metricData given a span object") {

    scenario("any valid span object") {
      val operationName = "testSpan"
      val serviceName = "testService"
      Given("a valid span")
      val span = Span.newBuilder()
        .setDuration(System.currentTimeMillis())
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .setStartTime(System.currentTimeMillis() * 1000) // in micro seconds
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVBool(false))
        .build()
      When("its asked to map to metricPoints")
      val isValid = isValidSpan(span, Nil)
      val metricDataList = generateMetricDataList(span, getMetricDataTransformers, new PeriodReplacementEncoder)

      Then("the number of metricPoints returned should be equal to the number of metricPoint transformers")
      metricDataList should not be empty
      val metricPointTransformers = getMetricDataTransformers
      metricDataList.size shouldEqual metricPointTransformers.size * 2

      Then("each metricPoint should have the timestamps in seconds and which should equal to the span timestamp")
      isValid shouldBe true
      metricDataList.foreach(metricData => {
        metricData.getTimestamp shouldEqual span.getStartTime / 1000000
      })

      Then("each metricPoint should have the metric type as Metric")
      metricDataList.foreach(metricData => {
        getMetricDataTags(metricData).get(MetricDefinition.MTYPE) shouldEqual METRIC_TYPE
      })

    }

    scenario("an invalid span object") {
      val operationName = ""
      val serviceName = ""
      Given("an invalid span")
      val span = Span.newBuilder()
        .setDuration(System.currentTimeMillis())
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVBool(false))
        .build()

      When("its asked to map to metricPoints")
      val isValid = isValidSpan(span, Nil)
      Then("It should return a metricPoint validation exception")
      isValid shouldBe false
      metricRegistry.meter("span.validation.failure").getCount shouldBe 1
    }

    scenario("a span object with a valid service Name") {
      val operationName = "testSpan"
      val serviceName = "testService"

      Given("a valid span")
      val span = Span.newBuilder()
        .setDuration(System.currentTimeMillis())
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVBool(false))
        .build()
      val encoder = new PeriodReplacementEncoder

      When("its asked to map to metricPoints")
      val isValid = isValidSpan(span, Nil)
      val metricDataList = generateMetricDataList(span, getMetricDataTransformers, encoder)

      Then("it should create metricPoints with service name as one its keys")
      isValid shouldBe true
      metricDataList.map(metricData => {
        val tags = getMetricDataTags(metricData).asScala
        tags.get(TagKeys.SERVICE_NAME_KEY) should not be None
        tags.get(TagKeys.SERVICE_NAME_KEY) shouldEqual Some(encoder.encode(serviceName))
      })
    }

    scenario("a span object with a blacklisted service Name") {
      val operationName = "testSpan"
      val blacklistedServiceName = "testService"

      Given("a valid span with a blacklisted service name")
      val span = Span.newBuilder()
        .setDuration(System.currentTimeMillis())
        .setOperationName(operationName)
        .setServiceName(blacklistedServiceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVBool(false))
        .build()

      When("its asked to map to metricPoints")
      val isValid = isValidSpan(span, List(new Regex(blacklistedServiceName)))
      Then("It should return a metricPoint validation exception")

      isValid shouldBe false
      metricRegistry.meter("span.validation.black.listed").getCount shouldBe 1
    }

    scenario("a span object with a blacklisted regex service Name") {
      val serviceName = "testservice"

      Given("a valid span with a blacklisted service name")
      val span = Span.newBuilder().setDuration(System.currentTimeMillis()).setOperationName("testSpan").setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVBool(false)).build()

      When("its asked to map to metricPoints")
      val isValid = isValidSpan(span, List(new Regex("^[a-z]*$")))

      Then("It should return a metricPoint")
      isValid shouldBe false
    }

    scenario("a span object with a non-blacklisted regex service Name") {
      val serviceName = "testService"

      Given("a valid span with a blacklisted service name")
      val span = Span.newBuilder().setDuration(System.currentTimeMillis()).setOperationName("testSpan").setServiceName(serviceName)
        .addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVBool(false)).build()

      When("its asked to map to metricPoints")
      val isValid = isValidSpan(span, List(new Regex("^[a-z]*")))
      val metricDataList = generateMetricDataList(span, getMetricDataTransformers, new PeriodReplacementEncoder, serviceOnlyFlag = false)

      Then("It should return a metricPoint validation exception")
      isValid shouldBe false
      metricDataList should not be empty
    }
  }
}
