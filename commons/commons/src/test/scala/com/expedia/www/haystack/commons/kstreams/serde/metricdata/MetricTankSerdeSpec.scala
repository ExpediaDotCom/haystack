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

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import com.expedia.www.haystack.commons.entities.{Interval, TagKeys}
import com.expedia.www.haystack.commons.unit.UnitTestSpec
import org.msgpack.core.MessagePack
import org.msgpack.value.ValueFactory

import scala.util.Random

class MetricTankSerdeSpec extends UnitTestSpec {
  val statusFile = "/tmp/app-health.status"
  val DURATION_METRIC_NAME = "duration"
  val SERVICE_NAME = "dummy_service"
  val OPERATION_NAME = "dummy_operation"
  val TOPIC_NAME = "dummy"


  val metricTags = Map(TagKeys.OPERATION_NAME_KEY -> OPERATION_NAME,
    TagKeys.SERVICE_NAME_KEY -> SERVICE_NAME)

  "MetricTank serde for metric data" should {

    "serialize and deserialize metric data using messagepack" in {

      Given("metric data")
      val tags = new java.util.LinkedHashMap[String, String] {
        put("serviceName", SERVICE_NAME)
        put("operationName", OPERATION_NAME)
        put(MetricDefinition.MTYPE, "gauge")
        put(MetricDefinition.UNIT, "short")
      }
      val metricData = getMetricData(tags)
      val metricTankSerde = new MetricTankSerde()

      When("its serialized using the metricTank Serde")
      val serializedBytes = metricTankSerde.serializer().serialize(TOPIC_NAME, metricData)

      Then("it should be encoded as message pack")
      val unpacker = MessagePack.newDefaultUnpacker(serializedBytes)
      unpacker should not be null

      metricTankSerde.close()
    }

    "serialize metric data with the right metric interval if present" in {

      Given("metric data with a 5 minute interval")
      val metricTankSerde = new MetricTankSerde()

      val tags = new java.util.LinkedHashMap[String, String] {
        put("serviceName", SERVICE_NAME)
        put("operationName", OPERATION_NAME)
        put(MetricDefinition.MTYPE, "gauge")
        put(MetricDefinition.UNIT, "short")
        put("interval", Interval.FIVE_MINUTE.name.toString)
      }
      val metricData = getMetricData(tags)

      When("its serialized using the metricTank Serde")
      val serializedBytes = metricTankSerde.serializer().serialize(TOPIC_NAME, metricData)
      val unpacker = MessagePack.newDefaultUnpacker(serializedBytes)
      Then("it should be able to unpack the content")
      unpacker should not be null

      Then("it unpacked content should be a valid map")
      val deserializedMetricData = unpacker.unpackValue().asMapValue().map()
      deserializedMetricData should not be null

      Then("interval key should be set as 300 seconds")
      deserializedMetricData.get(ValueFactory.newString(metricTankSerde.serializer().intervalKey)).asIntegerValue().asInt() shouldBe 300

      metricTankSerde.close()
    }

    "serialize metricpoint with the default interval if not present" in {

      Given("metric point without the interval tag")
      val metricTankSerde = new MetricTankSerde()
      val tags = new java.util.LinkedHashMap[String, String] {
        put("serviceName", SERVICE_NAME)
        put("operationName", OPERATION_NAME)
        put(MetricDefinition.MTYPE, "gauge")
        put(MetricDefinition.UNIT, "short")
      }
      val metricData = getMetricData(tags)

      When("its serialized using the metricTank Serde")
      val serializedBytes = metricTankSerde.serializer().serialize(TOPIC_NAME, metricData)
      val unpacker = MessagePack.newDefaultUnpacker(serializedBytes)
      Then("it should be able to unpack the content")
      unpacker should not be null

      Then("it unpacked content should be a valid map")
      val deserializedMetricData = unpacker.unpackValue().asMapValue().map()
      deserializedMetricData should not be null

      Then("interval key should be set as default metric interval in seconds")
      deserializedMetricData.get(ValueFactory.newString(metricTankSerde.serializer().intervalKey)).asIntegerValue().asInt() shouldBe metricTankSerde.serializer().DEFAULT_INTERVAL_IN_SEC

      metricTankSerde.close()
    }


    "serialize and deserialize simple metric points without loosing data" in {

      Given("metric point")
      val metricTankSerde = new MetricTankSerde()
      val tags = new java.util.LinkedHashMap[String, String] {
        put("serviceName", SERVICE_NAME)
        put("operationName", OPERATION_NAME)
        put(MetricDefinition.MTYPE, "gauge")
        put(MetricDefinition.UNIT, "short")
      }
      val metricData = getMetricData(tags)

      When("its serialized in the metricTank Format")
      val serializedBytes = metricTankSerde.serializer().serialize(TOPIC_NAME, metricData)
      val deserializedMetricPoint = metricTankSerde.deserializer().deserialize(TOPIC_NAME, serializedBytes)

      Then("it should be encoded as message pack")
      metricData shouldEqual deserializedMetricPoint

      metricTankSerde.close()
    }
  }

  "serializer returns null for any exception" in {

    Given("MetricTankSerde and a null metric data")
    val metricTankSerde = new MetricTankSerde()
    val metricData = null

    When("its serialized using the metricTank Serde")
    val serializedBytes = metricTankSerde.serializer().serialize(TOPIC_NAME, metricData)

    Then("serializer should return null")
    serializedBytes shouldBe null
    metricTankSerde.close()
  }


  private def getMetricData(tags: java.util.LinkedHashMap[String, String]): MetricData = {
    val metricDefinition = new MetricDefinition(DURATION_METRIC_NAME, new TagCollection(tags), TagCollection.EMPTY)
    new MetricData(metricDefinition, Random.nextDouble(), System.currentTimeMillis() / 1000)
  }

}
