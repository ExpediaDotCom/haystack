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
package com.expedia.www.haystack.trends

import com.expedia.metrics.MetricData
import com.expedia.open.tracing.Span
import com.expedia.www.haystack.commons.entities.encoders.Encoder
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trends.transformer.MetricDataTransformer

import scala.util.matching.Regex

trait MetricDataGenerator extends MetricsSupport {

  private val SpanValidationErrors = metricRegistry.meter("span.validation.failure")
  private val BlackListedSpans = metricRegistry.meter("span.validation.black.listed")
  private val metricPointGenerationTimer = metricRegistry.timer("metricpoint.generation.time")

  /**
    * This function is responsible for generating all the metric points which can be created given a span
    *
    * @param span            incoming span
    * @param transformers    list of transformers to be applied
    * @param encoder         encoder object
    * @param serviceOnlyFlag tells if metric data should be generated for serviceOnly, default is true
    * @return
    */
  def generateMetricDataList(span: Span,
                             transformers: Seq[MetricDataTransformer],
                             encoder: Encoder,
                             serviceOnlyFlag: Boolean = true): Seq[MetricData] = {
    val timer = metricPointGenerationTimer.time()
    val metricPoints = transformers.flatMap(transformer => transformer.mapSpan(span, serviceOnlyFlag, encoder))
    timer.close()
    metricPoints
  }

  /**
    * This function validates a span and makes sure that the span has the necessary data to generate meaningful metrics
    * This layer is supposed to do generic validations which would impact all the transformers.
    * Validation specific to the transformer can be done in the transformer itself
    *
    * @param span incoming span
    * @return Try object which should return either the span as is or a validation exception
    */
  def isValidSpan(span: Span, blackListedServices: List[Regex]): Boolean = {
    if (span.getServiceName.isEmpty || span.getOperationName.isEmpty) {
      SpanValidationErrors.mark()
      return false
    }

    val isBlacklisted = blackListedServices.exists {
      regexp =>
        regexp.pattern.matcher(span.getServiceName).find()
    }

    if (isBlacklisted) BlackListedSpans.mark()
    !isBlacklisted
  }
}
