package com.expedia.www.haystack.trends.kstream.serde.metric

import com.expedia.www.haystack.commons.entities.Interval
import com.expedia.www.haystack.commons.entities.Interval.Interval
import com.expedia.www.haystack.trends.aggregation.metrics.{CountMetric, Metric}
import org.msgpack.core.MessagePack
import org.msgpack.value.{Value, ValueFactory}

import scala.collection.JavaConverters._

/**
  * Serde which lets us serialize and deserilize the count metric, this is used when we serialize/deserialize the windowedMetric which can internally contain count or histogram metric
  * It uses messagepack to pack the object into bytes
  */
object CountMetricSerde extends MetricSerde {

  private val currentCountKey = "currentCount"
  private val intervalKey = "interval"


  override def serialize(metric: Metric): Array[Byte] = {

    val countMetric = metric.asInstanceOf[CountMetric]
    val packer = MessagePack.newDefaultBufferPacker()
    val metricData = Map[Value, Value](
      ValueFactory.newString(currentCountKey) -> ValueFactory.newInteger(countMetric.getCurrentCount),
      ValueFactory.newString(intervalKey) -> ValueFactory.newString(countMetric.getMetricInterval.name)
    )
    packer.packValue(ValueFactory.newMap(metricData.asJava))
    packer.toByteArray
  }

  override def deserialize(data: Array[Byte]): Metric = {
    val metric = MessagePack.newDefaultUnpacker(data).unpackValue().asMapValue().map()
    val currentCount:Long = metric.get(ValueFactory.newString(currentCountKey)).asIntegerValue().asLong()
    val interval:Interval = Interval.fromName(metric.get(ValueFactory.newString(intervalKey)).asStringValue().toString)
    new CountMetric(interval,currentCount)
  }


}
