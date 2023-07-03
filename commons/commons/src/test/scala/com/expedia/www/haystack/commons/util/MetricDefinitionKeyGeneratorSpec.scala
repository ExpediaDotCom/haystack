package com.expedia.www.haystack.commons.util

import java.util

import com.expedia.metrics.{MetricDefinition, TagCollection}
import com.expedia.www.haystack.commons.entities.TagKeys.PRODUCT_KEY
import com.expedia.www.haystack.commons.unit.UnitTestSpec

class MetricDefinitionKeyGeneratorSpec extends UnitTestSpec {
  "Metric Definition Key Generator" should {
    "generate a unique key based on key and tags in MetricDefinition" in {
      Given("a Metric Definition")
      val metricDefinition = getMetricDefinition

      When("MetricDefinitionKeyGenerator is called")
      val key = MetricDefinitionKeyGenerator.generateKey(metricDefinition)

      Then("a unique key is generated")
      key should equal("key=duration,mtype=gauge,op=some-op,product=haystack,svc=some-svc,unit=short")
    }
  }

  private def getMetricDefinition: MetricDefinition = {
    val metricTags = new util.LinkedHashMap[String, String] {
      put("svc", "some-svc")
      put("op", "some-op")
    }
    val tags = new util.LinkedHashMap[String, String] {
      putAll(metricTags)
      put(MetricDefinition.MTYPE, "gauge")
      put(MetricDefinition.UNIT, "short")
      put(PRODUCT_KEY, "haystack")
    }
    val tc = new TagCollection(tags)
    new MetricDefinition("duration", tc, TagCollection.EMPTY)
  }
}
