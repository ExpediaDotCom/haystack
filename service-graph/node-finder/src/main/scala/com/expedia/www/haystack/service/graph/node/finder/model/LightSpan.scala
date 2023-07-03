/*
 *
 *     Copyright 2018 Expedia, Inc.
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

package com.expedia.www.haystack.service.graph.node.finder.model

import com.expedia.www.haystack.service.graph.node.finder.utils.SpanType
import com.expedia.www.haystack.service.graph.node.finder.utils.SpanType.SpanType
import org.apache.commons.lang3.StringUtils

/**
  * Light weight representation of a Span with minimal information required
  *
  * @param spanId        Unique identity of the Span
  * @param parentSpanId  spanId of its Parent span
  * @param time          Timestamp associated with a Span in MilliSeconds (i.e., StartTime)
  * @param serviceName   Service name of the span
  * @param operationName Operation name of the span
  * @param duration      duration of the Span in micro seconds
  * @param spanType      type of the span
  */
case class LightSpan(spanId: String,
                     parentSpanId: String,
                     time: Long, // in epoch millis
                     serviceName: String,
                     operationName: String,
                     duration: Long, // in micros
                     spanType: SpanType,
                     tags: Map[String, String]) extends Equals {
  require(StringUtils.isNotBlank(spanId))
  require(time > 0)
  require(StringUtils.isNotBlank(serviceName))
  require(StringUtils.isNoneBlank(operationName))
  require(spanType != null)

  private val durationInMillis = duration / 1000L
  /**
    * check whether this light span is later than the given cutOffTime
    *
    * @param cutOffTime time in epoch millis to be compared
    * @return true if this span is later than the given cutOffTime time else false
    */
  def isLaterThan(cutOffTime: Long): Boolean = (time + durationInMillis - cutOffTime) > 0

  override def canEqual(that: Any): Boolean = {
    that.isInstanceOf[LightSpan]
  }

  override def equals(that: Any): Boolean = {
    that match {
      case that: LightSpan =>
        that.canEqual(this) &&
          this.spanId == that.spanId &&
          this.parentSpanId == that.parentSpanId &&
          this.serviceName == that.serviceName
      case _ => false
    }
  }

  override def hashCode(): Int = {
    41 * (
      41 * (
        41 + spanId.hashCode
        ) + parentSpanId.hashCode
      ) + serviceName.hashCode
  }
}

/**
  * Builder class for LightSpan
  */
object LightSpanBuilder {

  /**
    * update span type to an existing span
    *
    * @param span     span to be updated
    * @param spanType span type to be updated in a given span
    * @return
    */
  def updateSpanTypeIfAbsent(span: LightSpan, spanType: SpanType): LightSpan = {
    if (span.spanType == SpanType.OTHER) span.copy(spanType = spanType) else span
  }
}
