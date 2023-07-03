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

import com.expedia.www.haystack.commons.entities.{GraphEdge, TagKeys}

import scala.collection.mutable

/**
  * Object to hold stats for graph edges
 *
  * @param count edge count seen so far
  * @param lastSeen timestamp the edge was last seen, in ms
  * @param errorCount error rate for this specific operation
  */
case class EdgeStats(count: Long,
                     lastSeen: Long,
                     errorCount: Long,
                     sourceTags: mutable.Map[String, String] = mutable.HashMap[String, String](),
                     destinationTags:  mutable.Map[String, String] = mutable.HashMap[String, String]()) {
  def update(e: GraphEdge): EdgeStats = {
    this.sourceTags ++= e.source.tags
    this.sourceTags.remove(TagKeys.ERROR_KEY)
    this.destinationTags ++= e.destination.tags
    this.destinationTags.remove(TagKeys.ERROR_KEY)

    val incrErrorCountBy = if (e.source.tags.getOrElse(TagKeys.ERROR_KEY, "false") == "true") 1 else 0
    EdgeStats(
      count + 1,
      lastSeen(e),
      errorCount + incrErrorCountBy,
      sourceTags,
      destinationTags)
  }

  private def lastSeen(e: GraphEdge): Long = {
    if (e.sourceTimestamp == 0) System.currentTimeMillis() else Math.max(e.sourceTimestamp, this.lastSeen)
  }
}
