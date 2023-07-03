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

package com.expedia.www.haystack.commons.kstreams.serde.metricdata

import java.nio.ByteBuffer
import java.util

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import com.expedia.www.haystack.commons.entities.TagKeys._
import com.expedia.www.haystack.commons.entities.{Interval, TagKeys}
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import org.apache.commons.codec.digest.DigestUtils
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import org.msgpack.core.MessagePack.Code
import org.msgpack.core.{MessagePack, MessagePacker}
import org.msgpack.value.impl.ImmutableLongValueImpl
import org.msgpack.value.{Value, ValueFactory}

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap

/**
  * This class takes a metric data object and serializes it into a messagepack encoded bytestream
  * which can be directly consumed by metrictank. The serialized data is finally streamed to kafka
  */
class MetricTankSerde() extends Serde[MetricData] with MetricsSupport {

  override def deserializer(): MetricDataDeserializer = {
    new MetricDataDeserializer()
  }

  override def serializer(): MetricDataSerializer = {
    new MetricDataSerializer()
  }

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()
}

class MetricDataDeserializer() extends Deserializer[MetricData] with MetricsSupport {

  private val metricPointDeserFailureMeter = metricRegistry.meter("metricpoint.deser.failure")
  private val TAG_DELIMETER = "="
  private val metricKey = "Metric"
  private val valueKey = "Value"
  private val timeKey = "Time"
  private val typeKey = "Mtype"
  private val tagsKey = "Tags"
  private val idKey = "Id"

  override def configure(map: java.util.Map[String, _], b: Boolean): Unit = ()

  /**
    * converts the messagepack bytes into MetricPoint object
    *
    * @param data serialized bytes of MetricPoint
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
        metricPointDeserFailureMeter.mark()
        null
    }
  }

  private def createMetricNameFromMetricKey(metricKey: String): String = {
    metricKey.split("\\.").last
  }


  private def createTags(metricData: util.Map[Value, Value]): Map[String, String] = {
    ListMap(metricData.get(ValueFactory.newString(tagsKey)).asArrayValue().list().asScala.map(tag => {
      val kvPairs = tag.toString.split("=")
      (kvPairs(0), kvPairs(1))
    }): _*)
  }


  override def close(): Unit = ()
}

class MetricDataSerializer() extends Serializer[MetricData] with MetricsSupport {
  private val metricPointSerFailureMeter = metricRegistry.meter("metricpoint.ser.failure")
  private val metricPointSerSuccessMeter = metricRegistry.meter("metricpoint.ser.success")
  private val DEFAULT_ORG_ID = 1
  private[commons] val DEFAULT_INTERVAL_IN_SEC = 60
  private val idKey = "Id"
  private val orgIdKey = "OrgId"
  private val nameKey = "Name"
  private val metricKey = "Metric"
  private val valueKey = "Value"
  private val timeKey = "Time"
  private val typeKey = "Mtype"
  private val tagsKey = "Tags"
  private[commons] val intervalKey = "Interval"

  override def configure(map: java.util.Map[String, _], b: Boolean): Unit = ()

  override def serialize(topic: String, metricData: MetricData): Array[Byte] = {
    try {
      val packer = MessagePack.newDefaultBufferPacker()

      val metricDataMap = Map[Value, Value](
        ValueFactory.newString(idKey) -> ValueFactory.newString(s"${getId(metricData)}"),
        ValueFactory.newString(nameKey) -> ValueFactory.newString(metricData.getMetricDefinition.getKey),
        ValueFactory.newString(orgIdKey) -> ValueFactory.newInteger(getOrgId(metricData)),
        ValueFactory.newString(intervalKey) -> new ImmutableSignedLongValueImpl(retrieveInterval(metricData)),
        ValueFactory.newString(metricKey) -> ValueFactory.newString(metricData.getMetricDefinition.getKey),
        ValueFactory.newString(valueKey) -> ValueFactory.newFloat(metricData.getValue),
        ValueFactory.newString(timeKey) -> new ImmutableSignedLongValueImpl(metricData.getTimestamp),
        ValueFactory.newString(typeKey) -> ValueFactory.newString(retrieveType(metricData)),
        ValueFactory.newString(tagsKey) -> ValueFactory.newArray(retrieveTags(metricData).asJava)
      )
      packer.packValue(ValueFactory.newMap(metricDataMap.asJava))
      val data = packer.toByteArray
      metricPointSerSuccessMeter.mark()
      data
    } catch {
      case ex: Exception =>
        /* may be log and add metric */
        metricPointSerFailureMeter.mark()
        null
    }
  }

  //Retrieves the interval in case its present in the tags else uses the default interval
  private def retrieveInterval(metricData: MetricData): Int = {
    getMetricTags(metricData).asScala.get(TagKeys.INTERVAL_KEY).map(stringInterval => Interval.fromName(stringInterval).timeInSeconds).getOrElse(DEFAULT_INTERVAL_IN_SEC)
  }

  private def retrieveType(metricData: MetricData): String = {
    getMetricTags(metricData).get(MetricDefinition.MTYPE)
  }

  private def retrieveTags(metricData: MetricData): List[Value] = {
    getMetricTags(metricData).asScala.map(tuple => {
      ValueFactory.newString(s"${tuple._1}=${tuple._2}")
    }).toList
  }

  private def getId(metricData: MetricData): String = {
    s"${getOrgId(metricData)}.${DigestUtils.md5Hex(getKey(metricData))}"
  }

  private def getKey(metricData: MetricData): String = {
    val metricTags = getMetricTags(metricData).asScala.foldLeft("")((tag, tuple) => {
      tag + s"${tuple._1}.${tuple._2}."
    })
    s"$metricTags${metricData.getMetricDefinition.getKey}"
  }

  private def getOrgId(metricData: MetricData): Int = {
    getMetricTags(metricData).getOrDefault(ORG_ID_KEY, DEFAULT_ORG_ID.toString).toInt
  }

  private def getMetricTags(metricData: MetricData) : util.Map[String, String] = {
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
