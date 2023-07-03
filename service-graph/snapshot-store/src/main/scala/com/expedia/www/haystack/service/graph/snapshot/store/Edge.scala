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
package com.expedia.www.haystack.service.graph.snapshot.store

import Constants._

import scala.collection.mutable

case class Edge(source: Node,
                destination: Node,
                statsCount: Long,
                statsLastSeen: Long,
                statsErrorCount: Long,
                effectiveFrom: Long,
                effectiveTo: Long) {
  private val Newline = "\n"
  private val CommaNewline = "," + Newline
  private val QuoteColonSpace = "\": "

  def toJson(prependComma: Boolean): String = {
    val stringBuilder = new StringBuilder
    if(prependComma) {
      stringBuilder.append(CommaNewline)
    }
    stringBuilder.append("    {")
    appendNode(stringBuilder, source, SourceKey)
    appendNode(stringBuilder, destination, DestinationKey)
    appendStats(stringBuilder)
    stringBuilder.append("      \"").append(EffectiveFromKey).append(QuoteColonSpace).append(effectiveFrom).append(CommaNewline)
    stringBuilder.append("      \"").append(EffectiveToKey).append(QuoteColonSpace).append(effectiveTo).append(Newline)
    stringBuilder.append("    }")
    stringBuilder.toString()
  }

  private def appendStats(stringBuilder: StringBuilder) = {
    stringBuilder.append("\n      \"").append(StatsKey).append("\": {\n")
    stringBuilder.append("        \"").append(CountKey).append(QuoteColonSpace).append(statsCount).append(CommaNewline)
    stringBuilder.append("        \"").append(LastSeenKey).append(QuoteColonSpace).append(statsLastSeen).append(CommaNewline)
    stringBuilder.append("        \"").append(ErrorCountKey).append(QuoteColonSpace).append(statsErrorCount).append(Newline)
    stringBuilder.append("      },\n")
  }

  private def appendNode(stringBuilder: StringBuilder, node: Node, key: String) = {
    stringBuilder.append("\n      \"").append(key).append("\": {\n")
    stringBuilder.append("        \"").append(NameKey).append("\": \"").append(node.name).append("\"")
    stringBuilder.append(CommaNewline).append("        \"").append(TagsKey).append("\": {")
    if (areAnyTagsDefined) {
      stringBuilder.append(Newline)
      if (node.tier.isDefined) {
        stringBuilder.append("          \"").append(TierKey).append("\": \"").append(node.tier.get).append("\"")
        if (node.infrastructureProvider.isDefined) {
          stringBuilder.append(CommaNewline)
        }
      }
      if (node.infrastructureProvider.isDefined) {
        stringBuilder.append("          \"").append(InfrastructureProviderKey).append("\": \"")
          .append(node.infrastructureProvider.get).append("\"")
      }
      stringBuilder.append("\n        ")
    }
    stringBuilder.append("}\n")
    stringBuilder.append("      },")

    def areAnyTagsDefined = {
      node.infrastructureProvider.isDefined || node.tier.isDefined
    }
  }
}

object Edge {
  def mapper(nodeIdVsNode: mutable.Map[Long, Node],
             edgeWithIds: EdgeWithIds): Edge = {
    Edge(nodeIdVsNode(edgeWithIds.sourceId),
      nodeIdVsNode(edgeWithIds.destinationId),
      edgeWithIds.statsCount,
      edgeWithIds.statsLastSeen,
      edgeWithIds.statsErrorCount,
      edgeWithIds.effectiveFrom,
      edgeWithIds.effectiveTo)
  }
}
