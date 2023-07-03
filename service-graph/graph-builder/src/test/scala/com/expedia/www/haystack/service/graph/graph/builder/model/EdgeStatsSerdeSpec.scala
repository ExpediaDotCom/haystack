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

package com.expedia.www.haystack.service.graph.graph.builder.model

import com.expedia.www.haystack.service.graph.graph.builder.TestSpec

class EdgeStatsSerdeSpec extends TestSpec {

  describe("EdgeStateSerde ") {
    it("should serialize EdgeState") {
      Given("a valid EdgeState object")
      val edgeStats = EdgeStats(0, 0, 0)

      And("EdgeState serializer")
      val serializer = new EdgeStatsSerde().serializer()

      When("EdgeState is serialized")
      val bytes = serializer.serialize("", edgeStats)

      Then("it should generate valid byte stream")
      bytes.nonEmpty should be(true)
    }

    it("should deserialize serialized EdgeState") {
      Given("a valid EdgeState object")
      val edgeStats = EdgeStats(1, 1, 1)

      And("serialized EdgeState")
      val serializer = new EdgeStatsSerde().serializer()
      val bytes = serializer.serialize("", edgeStats)

      And("EdgeState deserializer")
      val deserializer = new EdgeStatsSerde().deserializer()

      When("EdgeState byte is deserialized")
      val deserializedEdgeStats = deserializer.deserialize("", bytes)

      Then("it should generate valid byte stream")
      deserializedEdgeStats should not be null
      deserializedEdgeStats.count should be(1)
      deserializedEdgeStats.lastSeen should be(1)
      deserializedEdgeStats.errorCount should be(1)
    }
  }
}
