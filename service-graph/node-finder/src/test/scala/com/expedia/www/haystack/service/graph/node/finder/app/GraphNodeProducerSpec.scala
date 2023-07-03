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
package com.expedia.www.haystack.service.graph.node.finder.app

import com.expedia.www.haystack.TestSpec
import com.expedia.www.haystack.commons.entities.GraphEdge
import org.apache.kafka.streams.processor.ProcessorContext
import org.easymock.EasyMock._

class GraphNodeProducerSpec extends TestSpec {
  describe("producing graph nodes") {
    it("should emit a valid graph node for a give complete SpanPair") {
      Given("a valid SpanPair instance")
      val spanPair = validSpanPair(Map("testtag" -> "true"))
      val context = mock[ProcessorContext]
      val graphNodeProducer = new GraphNodeProducer
      val captured = newCapture[GraphEdge]()
      When("process is called on GraphNodeProducer with it")
      expecting {
        context.forward(anyString(), capture[GraphEdge](captured)).once()
        context.commit().once()
      }
      replay(context)
      graphNodeProducer.init(context)
      graphNodeProducer.process(spanPair.getId, spanPair)
      val edge = captured.getValue
      Then("it should produce a valid GraphNode object")
      verify(context)
      edge.source.name should be("foo-service")
      edge.destination.name should be("baz-service")
      edge.operation should be("bar")
      edge.source.tags.get("testtag") shouldBe Some("true")
      edge.destination.tags.get("testtag") shouldBe Some("true")
    }
    it("should emit no graph nodes for invalid light spans") {
      Given("an incomplete SpanPair instance")
      val spanPair = invalidSpanPair()
      val context = mock[ProcessorContext]
      val graphNodeProducer = new GraphNodeProducer
      When("process is called on GraphNodeProducer with it")
      expecting {
        context.commit().once()
      }
      replay(context)
      graphNodeProducer.init(context)
      graphNodeProducer.process(spanPair.getId, spanPair)
      Then("it should produce no graph node in the context")
      verify(context)
    }
  }
  describe("graph node producer supplier") {
    it("should supply a valid producer") {
      Given("a supplier instance")
      val supplier = new GraphNodeProducerSupplier
      When("a producer is request")
      val producer = supplier.get()
      Then("should yield a valid producer")
      producer should not be null
    }
  }
}
