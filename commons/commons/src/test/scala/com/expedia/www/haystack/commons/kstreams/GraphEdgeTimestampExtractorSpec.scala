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

package com.expedia.www.haystack.commons.kstreams

import com.expedia.www.haystack.commons.entities.{GraphEdge, GraphVertex}
import com.expedia.www.haystack.commons.unit.UnitTestSpec
import org.apache.kafka.clients.consumer.ConsumerRecord

class GraphEdgeTimestampExtractorSpec extends UnitTestSpec {

  "GraphEdgeTimestampExtractor" should {

    "extract timestamp from GraphEdge" in {

      Given("a GraphEdge with some timestamp")
      val time = System.currentTimeMillis()
      val graphEdge = GraphEdge(GraphVertex("svc1"), GraphVertex("svc2"), "oper1", time)
      val extractor = new GraphEdgeTimestampExtractor
      val record: ConsumerRecord[AnyRef, AnyRef] = new ConsumerRecord("dummy-topic", 1, 1, "dummy-key", graphEdge)

      When("extract timestamp")
      val epochTime = extractor.extract(record, System.currentTimeMillis())

      Then("extracted time should equal GraphEdge time in milliseconds")
      epochTime shouldEqual time
    }
  }
}
