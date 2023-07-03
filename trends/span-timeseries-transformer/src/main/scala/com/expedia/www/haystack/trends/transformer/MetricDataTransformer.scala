/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.expedia.www.haystack.trends.transformer

import java.util

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import com.expedia.open.tracing.Span
import com.expedia.www.haystack.commons.entities.TagKeys._
import com.expedia.www.haystack.commons.entities.encoders.{Encoder, PeriodReplacementEncoder}
import com.expedia.www.haystack.commons.metrics.MetricsSupport


trait MetricDataTransformer extends MetricsSupport {

  protected val PRODUCT = "haystack"
  protected var encoder: Encoder = new PeriodReplacementEncoder

  def mapSpan(span: Span, serviceOnlyFlag: Boolean, encoder: Encoder): List[MetricData] = {
    this.encoder = encoder
    mapSpan(span, serviceOnlyFlag)
  }

  protected def mapSpan(span: Span, serviceOnlyFlag: Boolean): List[MetricData]

  protected def getDataPointTimestamp(span: Span): Long = span.getStartTime / 1000000

  protected def getMetricData(metricName: String,
                              metricTags: util.LinkedHashMap[String, String],
                              metricType: String,
                              metricUnit: String,
                              value: Double,
                              timestamp: Long): MetricData = {
    val tags = new util.LinkedHashMap[String, String] {
      putAll(metricTags)
      put(MetricDefinition.MTYPE, metricType)
      put(MetricDefinition.UNIT, metricUnit)
      put(PRODUCT_KEY, PRODUCT)
    }
    val metricDefinition = new MetricDefinition(metricName, new TagCollection(tags), TagCollection.EMPTY)
    val metricData = new MetricData(metricDefinition, value, timestamp)
    metricData
  }

  /**
    * This function creates the common metric tags from a span object.
    * Every metric point must have the operationName and ServiceName in its tags, the individual transformer
    * can add more tags to the metricPoint.
    *
    * @param span incoming span
    * @return metric tags in the form of HashMap of string,string
    */
  protected def createCommonMetricTags(span: Span): util.LinkedHashMap[String, String] = {
    new util.LinkedHashMap[String, String] {
      put(SERVICE_NAME_KEY, encoder.encode(span.getServiceName))
      put(OPERATION_NAME_KEY, encoder.encode(span.getOperationName))
    }
  }

  protected def createServiceOnlyMetricTags(span: Span): util.LinkedHashMap[String, String] = {
    new util.LinkedHashMap[String, String] {
      put(SERVICE_NAME_KEY, encoder.encode(span.getServiceName))
    }
  }
}

object MetricDataTransformer {
  val allTransformers = List(SpanDurationMetricDataTransformer, SpanStatusMetricDataTransformer)
}

