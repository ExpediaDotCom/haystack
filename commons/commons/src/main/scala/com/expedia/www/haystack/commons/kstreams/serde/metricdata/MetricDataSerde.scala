/*
 *  Copyright 2019 Expedia, Inc.
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

package com.expedia.www.haystack.commons.kstreams.serde.metricdata

import java.nio.ByteBuffer
import java.util

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import org.msgpack.core.MessagePack.Code
import org.msgpack.core.{MessagePack, MessagePacker}
import org.msgpack.value.impl.ImmutableLongValueImpl
import org.msgpack.value.{Value, ValueFactory}

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap

/**
  * This class takes a metric data object and serializes it into a messagepack encoded bytestream
  * which is metrics 2.0 format. The serialized data is finally streamed to kafka
  */
class MetricDataSerde() extends Serde[MetricData] with MetricsSupport {

  override def deserializer(): MetricDeserializer = {
    new MetricDeserializer()
  }

  override def serializer(): MetricSerializer = {
    new MetricSerializer()
  }

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()
}

class MetricDeserializer() extends Deserializer[MetricData] with MetricsSupport {

  private val metricDataDeserFailureMeter = metricRegistry.meter("metricdata.deser.failure")
  private val TAG_DELIMETER = "="
  private val metricKey = "Metric"
  private val valueKey = "Value"
  private val timeKey = "Time"
  private val tagsKey = "Tags"


  override def configure(map: java.util.Map[String, _], b: Boolean): Unit = ()

  /**
    * converts the messagepack bytes into MetricData object
    *
    * @param data serialized bytes of MetricData
    * @return
    */
  override def deserialize(topic: String, data: Array[Byte]): MetricData = {
    try {
      val unpacker = MessagePack.newDefaultUnpacker(data)
      val metricData = unpacker.unpackValue().asMapValue().map()
      val key = metricData.get(ValueFactory.newString(metricKey)).asStringValue().toString
      val tags = createTags(metricData)
      val metricDefinition = new MetricDefinition(key, new TagCollection(tags.asJava), TagCollection.EMPTY)
      new MetricData(metricDefinition, metricData.get(ValueFactory.newString(valueKey)).asFloatValue().toDouble,
        metricData.get(ValueFactory.newString(timeKey)).asIntegerValue().toLong)
    } catch {
      case ex: Exception =>
        /* may be log and add metric */
        metricDataDeserFailureMeter.mark()
        null
    }
  }


  private def createTags(metricData: util.Map[Value, Value]): Map[String, String] = {
    ListMap(metricData.get(ValueFactory.newString(tagsKey)).asArrayValue().list().asScala.map(tag => {
      val kvPairs = tag.toString.split(TAG_DELIMETER)
      (kvPairs(0), kvPairs(1))
    }): _*)
  }


  override def close(): Unit = ()
}

class MetricSerializer() extends Serializer[MetricData] with MetricsSupport {
  private val metricDataSerFailureMeter = metricRegistry.meter("metricdata.ser.failure")
  private val metricDataSerSuccessMeter = metricRegistry.meter("metricdata.ser.success")
  private val metricKey = "Metric"
  private val valueKey = "Value"
  private val timeKey = "Time"
  private val tagsKey = "Tags"

  override def configure(map: java.util.Map[String, _], b: Boolean): Unit = ()

  override def serialize(topic: String, metricData: MetricData): Array[Byte] = {
    try {
      val packer = MessagePack.newDefaultBufferPacker()

      val metricDataMap = Map[Value, Value](
        ValueFactory.newString(metricKey) -> ValueFactory.newString(metricData.getMetricDefinition.getKey),
        ValueFactory.newString(valueKey) -> ValueFactory.newFloat(metricData.getValue),
        ValueFactory.newString(timeKey) -> new ImmutableSignedLongValueImpl(metricData.getTimestamp),
        ValueFactory.newString(tagsKey) -> ValueFactory.newArray(retrieveTags(metricData).asJava)
      )
      packer.packValue(ValueFactory.newMap(metricDataMap.asJava))
      val data = packer.toByteArray
      metricDataSerSuccessMeter.mark()
      data
    } catch {
      case ex: Exception =>
        /* may be log and add metric */
        metricDataSerFailureMeter.mark()
        null
    }
  }

  private def retrieveTags(metricData: MetricData): List[Value] = {
    getMetricTags(metricData).asScala.map(tuple => {
      ValueFactory.newString(s"${tuple._1}=${tuple._2}")
    }).toList
  }

  private def getMetricTags(metricData: MetricData): util.Map[String, String] = {
    metricData.getMetricDefinition.getTags.getKv
  }

  override def close(): Unit = ()

  /**
    * This is a value extention class for signed long type. The java client for messagepack packs positive longs as unsigned
    * and there is no way to force a signed long who's numberal value is positive.
    * Metric Tank schema requres a signed long type for the timestamp key.
    *
    * @param long
    */
  class ImmutableSignedLongValueImpl(long: Long) extends ImmutableLongValueImpl(long) {

    override def writeTo(pk: MessagePacker) {
      val buffer = ByteBuffer.allocate(java.lang.Long.BYTES + 1)
      buffer.put(Code.INT64)
      buffer.putLong(long)
      pk.addPayload(buffer.array())
    }
  }

}
