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

package com.expedia.www.haystack.service.graph.graph.builder.service.utils

import java.lang.Math.{max, min}

import com.expedia.www.haystack.service.graph.graph.builder.model._
import org.scalatest.{FunSpec, Matchers}

class EdgesMergerSpec extends FunSpec with Matchers {

  describe("EdgesMerger.getMergedServiceEdges()") {
    val stats1 = ServiceEdgeStats(1, 10, 100)
    val stats3 = ServiceEdgeStats(3, 30, 300)
    val stats5 = ServiceEdgeStats(5, 50, 500)
    val stats7 = ServiceEdgeStats(7, 70, 700)
    val vertexA: ServiceGraphVertex = ServiceGraphVertex("serviceGraphVertexA", Map.empty)
    val vertexB: ServiceGraphVertex = ServiceGraphVertex("serviceGraphVertexB", Map.empty)
    val vertexC: ServiceGraphVertex = ServiceGraphVertex("serviceGraphVertexC", Map.empty)
    val edgeAB1 = ServiceGraphEdge(vertexA, vertexB, stats1, 1000, 10000)
    val edgeAB3 = ServiceGraphEdge(vertexA, vertexB, stats3, 3000, 30000)
    val edgeAC5 = ServiceGraphEdge(vertexA, vertexC, stats5, 7000, 70000)
    val edgeBC7 = ServiceGraphEdge(vertexB, vertexC, stats7, 15000, 150000)
    it("should create two edges when source matches but destination does not") {
      val mergedEdges = EdgesMerger.getMergedServiceEdges(Seq(edgeAB1, edgeAC5))
      mergedEdges.size should equal(2)
      mergedEdges should contain(edgeAB1)
      mergedEdges should contain(edgeAC5)
    }
    it("should create two edges when destination matches but source does not") {
      val mergedEdges = EdgesMerger.getMergedServiceEdges(Seq(edgeAC5, edgeBC7))
      mergedEdges.size should equal(2)
      mergedEdges should contain(edgeAC5)
      mergedEdges should contain(edgeBC7)
    }
    it("should merge two edges when source and destination match") {
      val mergedEdges = EdgesMerger.getMergedServiceEdges(Seq(edgeAB1, edgeAB3))
      mergedEdges.size should equal(1)
      val mergedEdge: ServiceGraphEdge = mergedEdges.head
      mergedEdge.source should equal(vertexA)
      mergedEdge.destination should equal(vertexB)
      mergedEdge.effectiveFrom should equal(min(edgeAB1.effectiveFrom, edgeAB3.effectiveFrom))
      mergedEdge.effectiveTo should equal(max(edgeAB1.effectiveTo, edgeAB3.effectiveTo))
      mergedEdge.stats.count should equal(edgeAB1.stats.count + edgeAB3.stats.count)
      mergedEdge.stats.lastSeen should equal(max(edgeAB1.stats.lastSeen, edgeAB3.stats.lastSeen))
      mergedEdge.stats.errorCount should equal(edgeAB1.stats.errorCount + edgeAB3.stats.errorCount)
    }
  }

  describe("EdgesMerger.getMergedOperationEdge()") {
    val stats1 = EdgeStats(1, 10, 100)
    val stats3 = EdgeStats(3, 30, 300)
    val stats5 = EdgeStats(5, 50, 500)
    val stats7 = EdgeStats(7, 70, 700)
    val stats9 = EdgeStats(9, 90, 900)
    val edgeAX1: OperationGraphEdge = OperationGraphEdge("sourceA", "destinationX", "operation1", stats1, 1000, 10000)
    val edgeAX3: OperationGraphEdge = OperationGraphEdge("sourceA", "destinationX", "operation3", stats3, 3000, 30000)
    val edgeAY3: OperationGraphEdge = OperationGraphEdge("sourceA", "destinationY", "operation3", stats5, 7000, 70000)
    val edgeBY3a: OperationGraphEdge = OperationGraphEdge("sourceB", "destinationY", "operation3", stats7, 15000, 150000)
    val edgeBY3b: OperationGraphEdge = OperationGraphEdge("sourceB", "destinationY", "operation3", stats7, 31000, 310000)
    it ("should create two edges when source and destination match but operation does not") {
      val mergedEdges = EdgesMerger.getMergedOperationEdges(Seq(edgeAX1, edgeAX3))
      mergedEdges.size should equal(2)
      mergedEdges should contain(edgeAX1)
      mergedEdges should contain(edgeAX3)
    }
    it ("should create two edges when source and operation match but destination does not") {
      val mergedEdges = EdgesMerger.getMergedOperationEdges(Seq(edgeAX3, edgeAY3))
      mergedEdges.size should equal(2)
      mergedEdges should contain(edgeAX3)
      mergedEdges should contain(edgeAY3)
    }
    it ("should merge two edges when source, destination and operation match") {
      val mergedEdges = EdgesMerger.getMergedOperationEdges(Seq(edgeBY3a, edgeBY3b))
      mergedEdges.size should equal(1)
      val mergedEdge = mergedEdges.head
      mergedEdge.source should equal(edgeBY3a.source)
      mergedEdge.destination should equal(edgeBY3b.destination)
      mergedEdge.operation should equal(edgeBY3a.operation)
      mergedEdge.stats.count should equal(edgeBY3a.stats.count + edgeBY3b.stats.count)
      mergedEdge.stats.lastSeen should equal(max(edgeBY3a.stats.lastSeen, edgeBY3b.stats.lastSeen))
      mergedEdge.stats.errorCount should equal(edgeBY3a.stats.errorCount + edgeBY3b.stats.errorCount)
      mergedEdge.effectiveFrom should equal(min(edgeBY3a.effectiveFrom, edgeBY3b.effectiveFrom))
      mergedEdge.effectiveTo should equal(max(edgeBY3a.effectiveTo, edgeBY3b.effectiveTo))
    }
  }
}
