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
import com.expedia.www.haystack.trends.aggregation.TrendMetric
import com.expedia.www.haystack.trends.aggregation.metrics.{AggregationType, CountMetricFactory, HistogramMetricFactory}
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import org.msgpack.core.MessagePack
import org.msgpack.value.ValueFactory
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.Try

object TrendMetricSerde extends Serde[TrendMetric] with MetricsSupport {

  private val LOGGER = LoggerFactory.getLogger(this.getClass)

  private val trendMetricStatsDeserFailureMeter = metricRegistry.meter("trendmetric.deser.failure")
  private val trendMetricStatsSerSuccessMeter = metricRegistry.meter("trendmetric.ser.success")
  private val trendMetricStatsDeserSuccessMeter = metricRegistry.meter("trendmetric.deser.success")
  private val INTERVAL_KEY: String = "interval"
  private val TREND_METRIC_KEY: String = "trendMetric"
  private val AGGREGATION_TYPE_KEY = "aggregationType"
  private val METRICS_KEY = "metrics"

  override def deserializer(): Deserializer[TrendMetric] = {
    new Deserializer[TrendMetric] {
      override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

      override def close(): Unit = ()

      /**
        * converts the messagepack encoded bytes into trendMetric object
        *
        * @param topic topic associated with data
        * @param data serialized bytes of trendMetric
        * @return
        */
      override def deserialize(topic: String, data: Array[Byte]): TrendMetric = {
        Try {
          val unpacker = MessagePack.newDefaultUnpacker(data)
          val serializedWindowedMetric = unpacker.unpackValue().asMapValue().map()
          val aggregationType = AggregationType.withName(serializedWindowedMetric.get(ValueFactory.newString(AGGREGATION_TYPE_KEY)).asStringValue().toString)

          val metricFactory = aggregationType match {
            case AggregationType.Histogram => HistogramMetricFactory
            case AggregationType.Count => CountMetricFactory
          }

          val trendMetricMap = serializedWindowedMetric.get(ValueFactory.newString(METRICS_KEY)).asArrayValue().asScala.map(mapValue => {
            val map = mapValue.asMapValue().map()
            val intervalVal = map.get(ValueFactory.newString(INTERVAL_KEY)).asIntegerValue().asLong()
            val interval = Interval.fromVal(intervalVal)

            val windowedMetricByteArray = map.get(ValueFactory.newString(TREND_METRIC_KEY)).asBinaryValue().asByteArray()
            val windowedMetric = WindowedMetricSerde.deserializer().deserialize(topic, windowedMetricByteArray)

            interval -> windowedMetric
          }).toMap

          val metric = TrendMetric.restoreTrendMetric(trendMetricMap, metricFactory)
          trendMetricStatsDeserSuccessMeter.mark()
          metric

        }.recover {
          case ex: Exception =>
            LOGGER.error("failed to deserialize trend metric with exception", ex)
            trendMetricStatsDeserFailureMeter.mark()
            throw ex
        }.toOption.orNull
      }
    }
  }

  override def serializer(): Serializer[TrendMetric] = {
    new Serializer[TrendMetric] {
      override def configure(map: util.Map[String, _], b: Boolean): Unit = ()

      /**
        * converts the trendMetric object to encoded bytes
        *
        * @param topic       topic associated with data
        * @param trendMetric trendMetric object
        * @return
        */
      override def serialize(topic: String, trendMetric: TrendMetric): Array[Byte] = {

        val packer = MessagePack.newDefaultBufferPacker()

        if (trendMetric == null) {
          LOGGER.error("TrendMetric is null")
          null
        } else if (trendMetric.trendMetricsMap == null) {
          LOGGER.error("TrendMetric map is null")
          null
        }
        else {
          val serializedTrendMetric = trendMetric.trendMetricsMap.map {
            case (interval, windowedMetric) =>
              ValueFactory.newMap(Map(
                ValueFactory.newString(INTERVAL_KEY) -> ValueFactory.newInteger(interval.timeInSeconds),
                ValueFactory.newString(TREND_METRIC_KEY) -> ValueFactory.newBinary(WindowedMetricSerde.serializer().serialize(topic, windowedMetric))
              ).asJava)
          }

          val windowedMetricMessagePack = Map(
            ValueFactory.newString(METRICS_KEY) -> ValueFactory.newArray(serializedTrendMetric.toList.asJava),
            ValueFactory.newString(AGGREGATION_TYPE_KEY) -> ValueFactory.newString(trendMetric.getMetricFactory.getAggregationType.toString)
          )
          packer.packValue(ValueFactory.newMap(windowedMetricMessagePack.asJava))
          val data = packer.toByteArray
          trendMetricStatsSerSuccessMeter.mark()
          data
        }
      }

      override def close(): Unit = ()
    }
  }

  override def close(): Unit = ()

  override def configure(map: util.Map[String, _], b: Boolean): Unit = ()
}
