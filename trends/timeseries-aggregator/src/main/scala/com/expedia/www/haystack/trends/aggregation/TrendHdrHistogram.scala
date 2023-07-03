/*
 *
 *     Copyright 2019 Expedia, Inc.
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

package com.expedia.www.haystack.trends.aggregation

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import com.expedia.www.haystack.trends.config.entities.HistogramUnit.HistogramUnit
import com.expedia.www.haystack.trends.config.entities.{HistogramMetricConfiguration, HistogramUnit}
import org.HdrHistogram.Histogram

/**
  * Wrapper over hdr Histogram. Takes care of unit mismatch of histogram and the other systems.
  *
  * @param hdrHistogram : instance of hdr Histogram
  * @param unit : unit of the recorded values, can be millis, micros or seconds
  */
case class TrendHdrHistogram(private val hdrHistogram: Histogram, unit: HistogramUnit) {

  def this(histogramConfig: HistogramMetricConfiguration) = this(
    new Histogram(histogramConfig.maxValue, histogramConfig.precision), histogramConfig.unit)

  def recordValue(valInMicros: Long): Unit = {
    val metricDataValue = fromMicros(valInMicros)
    hdrHistogram.recordValue(metricDataValue)
  }

  def getMinValue: Long = toMicros(hdrHistogram.getMinValue)

  def getMaxValue: Long = toMicros(hdrHistogram.getMaxValue)

  def getMean: Long = toMicros(hdrHistogram.getMean.toLong)

  def getStdDeviation: Long = toMicros(hdrHistogram.getStdDeviation.toLong)

  def getTotalCount: Long = hdrHistogram.getTotalCount

  def getHighestTrackableValue: Long = hdrHistogram.getHighestTrackableValue

  def getHighesTrackableValueInMicros: Long = toMicros(hdrHistogram.getHighestTrackableValue)

  def getValueAtPercentile(percentile: Double): Long = toMicros(hdrHistogram.getValueAtPercentile(percentile))

  def getEstimatedFootprintInBytes: Int = hdrHistogram.getEstimatedFootprintInBytes

  def encodeIntoByteBuffer(buffer: ByteBuffer): Int = {
    hdrHistogram.encodeIntoByteBuffer(buffer)
  }

  private def fromMicros(value: Long): Long = {
    unit match {
      case HistogramUnit.MILLIS => TimeUnit.MICROSECONDS.toMillis(value)
      case HistogramUnit.SECONDS => TimeUnit.MICROSECONDS.toSeconds(value)
      case _ => value
    }
  }

  private def toMicros(value: Long): Long = {
    unit match {
      case HistogramUnit.MILLIS => TimeUnit.MILLISECONDS.toMicros(value.toLong)
      case HistogramUnit.SECONDS => TimeUnit.SECONDS.toMicros(value.toLong)
      case _ => value
    }
  }
}