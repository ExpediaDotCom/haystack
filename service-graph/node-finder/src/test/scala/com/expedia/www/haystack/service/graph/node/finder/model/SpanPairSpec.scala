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
package com.expedia.www.haystack.service.graph.node.finder.model

import java.util.UUID

import com.expedia.metrics.{MetricDefinition, TagCollection}
import com.expedia.www.haystack.TestSpec
import com.expedia.www.haystack.commons.entities._
import com.expedia.www.haystack.service.graph.node.finder.utils.SpanType

import scala.collection.JavaConverters._

class SpanPairSpec extends TestSpec {
  describe("a complete span") {
    it("should return a valid graphEdge for non-open tracing compliant spans") {
      Given("a complete spanlite")
      val spanId = UUID.randomUUID().toString
      val parentSpanId = UUID.randomUUID().toString
      val clientTime = System.currentTimeMillis()

      val clientSpan = newLightSpan(spanId, parentSpanId, "foo-service", "bar", clientTime, 1000,  SpanType.CLIENT)
      val serverSpan = newLightSpan(spanId, parentSpanId, "baz-service", "bar", SpanType.SERVER)
      val spanPair = SpanPairBuilder.createSpanPair(clientSpan, serverSpan)

      When("get graphEdge is called")
      val graphEdge = spanPair.getGraphEdge

      Then("it should return a valid graphEdge")
      spanPair.isComplete should be(true)
      graphEdge.get should be(GraphEdge(GraphVertex("foo-service"), GraphVertex("baz-service"), "bar", clientTime))
    }

    it("should return a valid graphEdge for open tracing compliant spans") {
      Given("a complete spanlite")
      val spanId = UUID.randomUUID().toString
      val clientTime = System.currentTimeMillis()

      val clientSpan = newLightSpan(spanId, UUID.randomUUID().toString, "foo-service", "bar", clientTime, 1000, SpanType.OTHER)
      val serverSpan = newLightSpan(UUID.randomUUID().toString, spanId, "baz-service", "bar", SpanType.OTHER)
      val spanPair = SpanPairBuilder.createSpanPair(clientSpan, serverSpan)

      When("get graphEdge is called")
      val graphEdge = spanPair.getGraphEdge

      Then("it should return a valid graphEdge")
      spanPair.isComplete should be(true)
      graphEdge.get should be(GraphEdge(GraphVertex("foo-service"), GraphVertex("baz-service"), "bar", clientTime))
    }
    it("should return valid metricPoints") {
      Given("a complete spanlite")
      val clientSend = System.currentTimeMillis()
      val serverReceive = clientSend + 500
      val spanId = UUID.randomUUID().toString
      val clientSpan = newLightSpan(spanId, UUID.randomUUID().toString,  "baz-service", "bar",
        serverReceive, 500, SpanType.SERVER, Map())
      val serverSpan = newLightSpan(spanId, UUID.randomUUID().toString,  "foo-service", "bar",
        clientSend, 1500, SpanType.CLIENT, Map())
      val spanPair = SpanPairBuilder.createSpanPair(clientSpan, serverSpan)

      When("get Latency is called")
      val metricPoint = spanPair.getLatency.get

      Then("it should return a valid latency pairs")
      val tags = new TagCollection(Map(
        TagKeys.SERVICE_NAME_KEY -> "foo-service",
        TagKeys.OPERATION_NAME_KEY -> "bar",
        MetricDefinition.UNIT -> "ms",
        MetricDefinition.MTYPE -> "gauge"
      ).asJava)

      spanPair.isComplete should be(true)
      metricPoint.getMetricDefinition.getKey should be ("latency")
      metricPoint.getValue should be (1)
      metricPoint.getTimestamp should be (clientSend / 1000)
      metricPoint.getMetricDefinition.getTags should equal (tags)
    }
  }

}
