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

import com.expedia.www.haystack.commons.entities.TagKeys.ERROR_KEY
import com.expedia.www.haystack.commons.entities.{GraphEdge, GraphVertex}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

import scala.collection.mutable

class EdgeStatsSpec extends FunSpec with GivenWhenThen with Matchers with MockitoSugar {

  private val vertexName1 = "vertexName1"
  private val vertexName2 = "vertexName2"
  private val mutableTagsa: mutable.Map[String, String] = mutable.Map("mutableKeya" -> "mutableValuea", ERROR_KEY -> "true")
  private val mutableTagsb: mutable.Map[String, String] = mutable.Map("mutableKeyb" -> "mutableValueb", ERROR_KEY -> "true")
  private val immutableTagsA: Map[String, String] = Map("immutableKeyA" -> "immutableValueA", ERROR_KEY -> "true")
  private val immutableTagsB: Map[String, String] = Map("immutableKeyB" -> "immutableValueB", ERROR_KEY -> "true")
  private val graphVertex1A: GraphVertex = GraphVertex(vertexName1, immutableTagsA)
  private val graphVertex2B: GraphVertex = GraphVertex(vertexName2, immutableTagsB)
  private val graphVertexWithNoTags: GraphVertex = GraphVertex(vertexName2, Map.empty)
  private val operationX = "operationX"
  private val sourceTimestamp12 = 12
  private val currentTimeAtStartOfTest = System.currentTimeMillis()
  private val edgeStatsWithNoTags = EdgeStats(count = 0, lastSeen = 0, errorCount = 0)
  private val graphEdgeWithNoTags = GraphEdge(graphVertexWithNoTags, graphVertexWithNoTags, operationX, 0)

  describe("EdgeStats constructor") {
    it("should use empty Maps for tags if no tags were specified") {
      assert(edgeStatsWithNoTags.sourceTags.isEmpty)
      assert(edgeStatsWithNoTags.destinationTags.isEmpty)
    }
  }

  describe("EdgeStats update") {
    {
      val graphEdge = GraphEdge(graphVertex1A, graphVertex2B, operationX, sourceTimestamp12)
      val edgeStats = EdgeStats(count = 0, lastSeen = 0, errorCount = 0,
        sourceTags = mutableTagsa, destinationTags = mutableTagsb)
      val updatedEdgeStats = edgeStats.update(graphEdge)
      it("should collect tags") {
        updatedEdgeStats.sourceTags.contains("mutableKeyb")
        updatedEdgeStats.sourceTags.contains("immutableKeyB")
        updatedEdgeStats.destinationTags.contains("mutableKeya")
        updatedEdgeStats.destinationTags.contains("immutableKeyA")
      }
      it("should clear error keys from the tags") {
        updatedEdgeStats.sourceTags.size shouldEqual 2
        updatedEdgeStats.destinationTags.size shouldEqual 2
      }
      it("should count errors passed in from the graph edge source tags") {
        updatedEdgeStats.errorCount shouldEqual 1
      }
      it("should assume no errors if the source tags map does not contain an error key") {
        edgeStatsWithNoTags.update(graphEdgeWithNoTags).errorCount shouldEqual 0
      }
    }
    it("should calculate last seen from System.currentTimeMillis if source timestamp is 0") {
      val graphEdge = GraphEdge(graphVertex1A, graphVertex2B, operationX, 0)
      val edgeStats = EdgeStats(count = 0, lastSeen = 0, errorCount = 0,
        sourceTags = mutableTagsa, destinationTags = mutableTagsb)
      val updatedEdgeStats = edgeStats.update(graphEdge)
      assert(updatedEdgeStats.lastSeen >= currentTimeAtStartOfTest)
    }
    it("should calculate last seen from source timestamp if source timestamp is not 0") {
      val graphEdge = GraphEdge(graphVertex1A, graphVertex2B, operationX, sourceTimestamp12)
      val edgeStats = EdgeStats(count = 0, lastSeen = 0, errorCount = 0,
        sourceTags = mutableTagsa, destinationTags = mutableTagsb)
      val updatedEdgeStats = edgeStats.update(graphEdge)
      updatedEdgeStats.lastSeen shouldEqual sourceTimestamp12
    }
  }
}
