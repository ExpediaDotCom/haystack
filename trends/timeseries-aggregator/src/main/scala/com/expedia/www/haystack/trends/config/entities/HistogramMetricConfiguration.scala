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
package com.expedia.www.haystack.trends.config.entities

import com.expedia.www.haystack.trends.config.entities.HistogramUnit.HistogramUnit


/**
  *This configuration helps create the HistogramMetric.
  *
  * @param precision - Decimal precision required for the histogram,allowable precision of histogram must be 0 <= value <= 5
  * @param maxValue - maximum value for the incoming metric (should always be > than the maximum value you're expecting for a metricpoint)
  * @param unit - unit of the value that will be given to histogram (can be micros, millis, seconds)
  */
case class HistogramMetricConfiguration(precision: Int, maxValue: Int, unit: HistogramUnit)

object HistogramUnit extends Enumeration {
  type HistogramUnit = Value
  val MILLIS, MICROS, SECONDS = Value

  def from(unit: String): HistogramUnit = {
    unit.toLowerCase() match {
      case "millis" => HistogramUnit.MILLIS
      case "micros" => HistogramUnit.MICROS
      case "seconds" => HistogramUnit.SECONDS
      case _ => throw new RuntimeException(
        String.format("Fail to understand the histogram unit %s, should be one of [millis, micros or seconds]", unit))
    }
  }

  def default: HistogramUnit = HistogramUnit.MICROS
}
