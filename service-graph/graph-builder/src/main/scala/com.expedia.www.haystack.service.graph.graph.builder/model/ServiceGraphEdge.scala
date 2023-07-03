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

import scala.collection.mutable
/**
  * A graph edge representing relationship between two services over an operation
  *
  * @param source source service
  * @param destination destination service
  * @param stats stats around the edge
  * @param effectiveFrom start timestamp from which stats are collected
  * @param effectiveTo end timestamp till which stats are collected
  *
  */
case class ServiceGraphEdge(source: ServiceGraphVertex,
                            destination: ServiceGraphVertex,
                            stats: ServiceEdgeStats,
                            effectiveFrom: Long,
                            effectiveTo: Long)  {
  require(source != null)
  require(destination != null)
  require(stats != null)

  def mergeTags(first: Map[String, String], second: Map[String, String]): Map[String, String] = {
    val merged = new mutable.HashMap[String, mutable.HashSet[String]]()

    def merge(tags: Map[String, String]) {
      tags.foreach {
        case (key, value) =>
          val valueSet = merged.getOrElseUpdate(key, new mutable.HashSet[String]())
          valueSet ++= value.split(",")
      }
    }

    merge(first)
    merge(second)

    merged.mapValues(_.mkString(",")).toMap
  }

  def +(other: ServiceGraphEdge): ServiceGraphEdge = {
    val sourceVertex = this.source.copy(tags = mergeTags(other.source.tags, this.source.tags))
    val destinationVertex = this.destination.copy(tags = mergeTags(other.destination.tags, this.destination.tags))
    ServiceGraphEdge(
      sourceVertex,
      destinationVertex,
      this.stats + other.stats,
      Math.min(this.effectiveFrom, other.effectiveFrom),
      Math.max(this.effectiveTo, other.effectiveTo))
  }
}

case class ServiceGraphVertex(name: String, tags: Map[String, String] = Map())

case class ServiceEdgeStats(count: Long,
                            lastSeen: Long,
                            errorCount: Long) {
  def +(other: ServiceEdgeStats): ServiceEdgeStats = {
    ServiceEdgeStats(
      this.count + other.count,
      Math.max(this.lastSeen, other.lastSeen),
      this.errorCount + other.errorCount)
  }
}
