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

import com.expedia.metrics.MetricData
import com.expedia.open.tracing.Span
import com.expedia.open.tracing.Tag.TagType
import com.expedia.www.haystack.commons.entities.TagKeys

import scala.collection.JavaConverters._


/**
  * This transformer generates a success or a failure metric y
  */
trait SpanStatusMetricDataTransformer extends MetricDataTransformer {
  private val spanSuccessMetricPoints = metricRegistry.meter("metricpoint.span.success")
  private val spanFailuresMetricPoints = metricRegistry.meter("metricpoint.span.failure")

  val SUCCESS_METRIC_NAME = "success-span"
  val FAILURE_METRIC_NAME = "failure-span"
  val MTYPE = "gauge"
  val UNIT = "short"

  override def mapSpan(span: Span, serviceOnlyFlag: Boolean): List[MetricData] = {
    var metricName: String = null

    if (isError(span)) {
      spanFailuresMetricPoints.mark()
      metricName = FAILURE_METRIC_NAME
    } else {
      spanSuccessMetricPoints.mark()
      metricName = SUCCESS_METRIC_NAME
    }

    var metricDataList = List(getMetricData(metricName, createCommonMetricTags(span), MTYPE, UNIT, 1, getDataPointTimestamp(span)))

    if (serviceOnlyFlag) {
      metricDataList = metricDataList :+ getMetricData(metricName, createServiceOnlyMetricTags(span), MTYPE, UNIT, 1, getDataPointTimestamp(span))
    }
    metricDataList
  }

  protected def isError(span: Span): Boolean = {
    val value = span.getTagsList.asScala.find(tag => tag.getKey.equalsIgnoreCase(TagKeys.ERROR_KEY)).map(x => {
      if (TagType.BOOL == x.getType) {
        return x.getVBool
      } else if (TagType.STRING == x.getType) {
        return !"false".equalsIgnoreCase(x.getVStr)
      }
      return true
    })
    value.getOrElse(false)
  }
}

object SpanStatusMetricDataTransformer extends SpanStatusMetricDataTransformer
