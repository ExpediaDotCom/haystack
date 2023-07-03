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

package com.expedia.www.haystack.trends.kstream.serde

import java.util

import com.expedia.www.haystack.commons.entities.Interval
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trends.aggregation.metrics.{AggregationType, CountMetricFactory, HistogramMetricFactory, Metric}
import com.expedia.www.haystack.trends.aggregation.{TrendMetric, WindowedMetric}
import com.expedia.www.haystack.trends.aggregation.entities.TimeWindow
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import org.msgpack.core.MessagePack
import org.msgpack.value.ValueFactory

import scala.collection.JavaConverters._
import scala.collection.mutable


object WindowedMetricSerde extends Serde[WindowedMetric] with MetricsSupport {

  private val SERIALIZED_METRIC_KEY = "serializedMetric"
  private val START_TIME_KEY = "startTime"
  private val END_TIME_KEY = "endTime"

  private val aggregationTypeKey = "aggregationType"
  private val metricsKey = "metrics"

  override def close(): Unit = ()

  override def deserializer(): Deserializer[WindowedMetric] = {
    new Deserializer[WindowedMetric] {
      override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

      override def close(): Unit = ()

      /**
        * converts the messagepack encoded bytes into windowedMetric object
        *
        * @param data serialized bytes of windowedMetric
        * @return
        */
      override def deserialize(topic: String, data: Array[Byte]): WindowedMetric = {
        val unpacker = MessagePack.newDefaultUnpacker(data)
        val serializedWindowedMetric = unpacker.unpackValue().asMapValue().map()
        val aggregationType = AggregationType.withName(serializedWindowedMetric.get(ValueFactory.newString(aggregationTypeKey)).asStringValue().toString)

        val metricFactory = aggregationType match {
          case AggregationType.Histogram => HistogramMetricFactory
          case AggregationType.Count => CountMetricFactory
        }

        val windowedMetricMap = mutable.TreeMap[TimeWindow, Metric]()
        serializedWindowedMetric.get(ValueFactory.newString(metricsKey)).asArrayValue().asScala.map(mapValue => {
          val map = mapValue.asMapValue().map()
          val startTime = map.get(ValueFactory.newString(START_TIME_KEY)).asIntegerValue().asLong()
          val endTime = map.get(ValueFactory.newString(END_TIME_KEY)).asIntegerValue().asLong()
          val window = TimeWindow(startTime, endTime)
          val metric = metricFactory.getMetricSerde.deserialize(map.get(ValueFactory.newString(SERIALIZED_METRIC_KEY)).asBinaryValue().asByteArray())
          windowedMetricMap.put(window, metric)
        })

        val intervalVal = windowedMetricMap.firstKey.endTime - windowedMetricMap.firstKey.startTime
        val interval = Interval.fromVal(intervalVal)
        val metric = WindowedMetric.restoreWindowedMetric(windowedMetricMap, metricFactory, TrendMetric.trendMetricConfig(interval)._1, interval)
        metric
      }
    }
  }


  override def serializer(): Serializer[WindowedMetric] = {
    new Serializer[WindowedMetric] {
      override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

      /**
        * converts the windowedMetric object to encoded bytes
        *
        * @param topic       topic associated with data
        * @param windowedMetric windowedMetric object
        * @return
        */
      override def serialize(topic: String, windowedMetric: WindowedMetric): Array[Byte] = {

        val packer = MessagePack.newDefaultBufferPacker()

        val serializedMetrics = windowedMetric.windowedMetricsMap.map {
          case (timeWindow, metric) =>
            ValueFactory.newMap(Map(
              ValueFactory.newString(START_TIME_KEY) -> ValueFactory.newInteger(timeWindow.startTime),
              ValueFactory.newString(END_TIME_KEY) -> ValueFactory.newInteger(timeWindow.endTime),
              ValueFactory.newString(SERIALIZED_METRIC_KEY) -> ValueFactory.newBinary(windowedMetric.getMetricFactory.getMetricSerde.serialize(metric))
            ).asJava)
        }
        val windowedMetricMessagePack = Map(
          ValueFactory.newString(metricsKey) -> ValueFactory.newArray(serializedMetrics.toList.asJava),
          ValueFactory.newString(aggregationTypeKey) -> ValueFactory.newString(windowedMetric.getMetricFactory.getAggregationType.toString)
        )
        packer.packValue(ValueFactory.newMap(windowedMetricMessagePack.asJava))
        val data = packer.toByteArray
        data
      }

      override def close(): Unit = ()
    }
  }

  override def configure(map: util.Map[String, _], b: Boolean): Unit = ()
}
