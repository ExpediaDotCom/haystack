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

/**
  * This Transformer reads a span and creates a duration metric point with the value as the
  */
trait SpanDurationMetricDataTransformer extends MetricDataTransformer {

  private val spanDurationMetricPoints = metricRegistry.meter("metricpoint.span.duration")

  val DURATION_METRIC_NAME = "duration"
  val MTYPE = "gauge"
  val UNIT = "microseconds"

  override def mapSpan(span: Span, serviceOnlyFlag: Boolean): List[MetricData] = {
    spanDurationMetricPoints.mark()

    var metricDataList = List(getMetricData(DURATION_METRIC_NAME, createCommonMetricTags(span), MTYPE, UNIT, span.getDuration, getDataPointTimestamp(span)))
    if (serviceOnlyFlag) {
      metricDataList = metricDataList :+
        getMetricData(DURATION_METRIC_NAME, createServiceOnlyMetricTags(span), MTYPE, UNIT, span.getDuration, getDataPointTimestamp(span))
    }
    metricDataList
  }

}

object SpanDurationMetricDataTransformer extends SpanDurationMetricDataTransformer

