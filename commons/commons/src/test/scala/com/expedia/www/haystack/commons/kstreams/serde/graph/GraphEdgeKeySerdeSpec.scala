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

package com.expedia.www.haystack.commons.kstreams.serde.graph

import com.expedia.www.haystack.commons.entities.{GraphEdge, GraphVertex}
import com.expedia.www.haystack.commons.unit.UnitTestSpec

class GraphEdgeKeySerdeSpec extends UnitTestSpec {
  "GraphEdge Key serializer" should {
    "should serialize a GraphEdge" in {
      Given("a GraphEdge serializer")
      val serializer = (new GraphEdgeKeySerde).serializer()

      And("a valid GraphEdge is provided")
      val edge = GraphEdge(GraphVertex("sourceSvc"), GraphVertex("destinationSvc"),
        "operation", 1)

      When("GraphEdge serializer is used to serialize the GraphEdge")
      val bytes = serializer.serialize("graph-nodes", edge)

      Then("it should serialize the object")
      new String(bytes) shouldEqual "{\"source\":{\"name\":\"sourceSvc\",\"tags\":{}},\"destination\":{\"name\":\"destinationSvc\",\"tags\":{}},\"operation\":\"operation\",\"sourceTimestamp\":0}"
    }
  }

  "GraphEdge Key deserializer" should {
    "should deserialize a GraphEdge" in {
      Given("a GraphEdge deserializer")
      val serializer = (new GraphEdgeKeySerde).serializer()
      val deserializer = (new GraphEdgeKeySerde).deserializer()

      And("a valid GraphEdge is provided")
      val edge = GraphEdge(GraphVertex("sourceSvc"), GraphVertex("destinationSvc"),
        "operation", System.currentTimeMillis())

      When("GraphEdge deserializer is used on valid array of bytes")
      val bytes = serializer.serialize("graph-nodes", edge)
      val dataWithoutSourceTimestamp = "{\"source\":{\"name\":\"sourceSvc\",\"tags\":{}},\"destination\":{\"name\":\"destinationSvc\",\"tags\":{}},\"operation\":\"operation\"}"

      val serializedEdge_1 = deserializer.deserialize("graph-nodes", bytes)
      val serializedEdge_2 = deserializer.deserialize("graph-nodes", dataWithoutSourceTimestamp.getBytes("utf-8"))

      Then("it should deserialize correctly")
      serializedEdge_1.source.name should be("sourceSvc")
      serializedEdge_1.destination.name should be("destinationSvc")
      serializedEdge_1.operation shouldEqual "operation"
      serializedEdge_1.source.tags.size shouldBe 0
      serializedEdge_1.destination.tags.size shouldBe 0
      serializedEdge_2.sourceTimestamp should not be 0l
    }
  }
}
