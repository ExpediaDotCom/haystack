package com.expedia.www.haystack.trends.kstream.processor

import java.util

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import com.expedia.www.haystack.commons.util.MetricDefinitionKeyGenerator.generateKey
import org.apache.kafka.streams.processor._

import scala.collection.JavaConverters._

class AdditionalTagsProcessorSupplier(additionalTags: Map[String, String]) extends ProcessorSupplier[String, MetricData] {
  override def get(): Processor[String, MetricData] = new AdditionalTagsProcessor(additionalTags)
}


class AdditionalTagsProcessor(additionalTags: Map[String, String]) extends AbstractProcessor[String, MetricData] {

  override def process(key: String, value: MetricData): Unit = {
    if (additionalTags.isEmpty) {
      context().forward(key, value)
    }
    else {
      val tags = new util.LinkedHashMap[String, String] {
        putAll(value.getMetricDefinition.getTags.getKv)
        putAll(additionalTags.asJava)
      }
      val metricDefinition = new MetricDefinition(value.getMetricDefinition.getKey, new TagCollection(tags), TagCollection.EMPTY)
      val metricData = new MetricData(metricDefinition, value.getValue, value.getTimestamp)
      context.forward(generateKey(metricData.getMetricDefinition), metricData)
    }
  }
}
