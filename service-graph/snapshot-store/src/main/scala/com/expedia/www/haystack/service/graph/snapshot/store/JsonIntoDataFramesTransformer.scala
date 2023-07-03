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

import com.expedia.www.haystack.service.graph.snapshot.store.Constants._
import com.expedia.www.haystack.service.graph.snapshot.store.JsonIntoDataFramesTransformer._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable
import scala.util.Try

object JsonIntoDataFramesTransformer {
  private val SourceId = SourceKey + IdKey.capitalize
  private val DestinationId = DestinationKey + IdKey.capitalize
  private val StatsCount = StatsKey + CountKey.capitalize
  private val StatsLastSeen = StatsKey + LastSeenKey.capitalize
  private val StatsErrorCount = StatsKey + ErrorCountKey.capitalize
  private val NodesFormatString = "%s,%s,%s,%s\n"
  private val EdgesFormatString = "%s,%s,%s,%s,%s,%s,%s,%s\n"

  val NodesHeader: String = NodesFormatString.format(
    IdKey, NameKey, InfrastructureProviderKey, TierKey)
  val EdgesHeader: String = EdgesFormatString.format(
    IdKey, SourceId, DestinationId, StatsCount, StatsLastSeen, StatsErrorCount, EffectiveFromKey, EffectiveToKey)
}

class JsonIntoDataFramesTransformer {

  def parseJson(jsonInput: String): NodesAndEdges = {
    val jValue = parse(jsonInput, useBigDecimalForDouble = false, useBigIntForLong = true)
    implicit val formats: DefaultFormats.type = DefaultFormats
    val map = jValue.extract[Map[String, Any]]
    val edgesList = map.getOrElse(EdgesKey, List[Any]()).asInstanceOf[List[Any]]
    val nodeToIdMap = getNodeToIdMap(edgesList)
    val nodes = createNodesCsvList(nodeToIdMap)
    val edges = createEdgesCsvList(edgesList, nodeToIdMap)
    NodesAndEdges(nodes, edges)
  }

  private def createEdgesCsvList(edgesList: List[Any], nodeToIdMap: mutable.Map[Node, Long]): String = {
    val stringBuilder = new mutable.StringBuilder(EdgesHeader)
    var id = 1
    for {
      edge <- edgesList
    } yield {
      val edgeAsMap = edge.asInstanceOf[Map[String, Any]]
      val statsAsMap = edgeAsMap.getOrElse(StatsKey, Map[String, Any]()).asInstanceOf[Map[String, Any]]
      val maybeSourceNode = findNode(SourceKey, edgeAsMap)
      val maybeDestinationNode = findNode(DestinationKey, edgeAsMap)
      val str = EdgesFormatString.format(id,
        if (maybeSourceNode.isDefined) nodeToIdMap(maybeSourceNode.get).toString else "",
        if (maybeDestinationNode.isDefined) nodeToIdMap(maybeDestinationNode.get).toString else "",
        Try(statsAsMap(CountKey).asInstanceOf[BigInt].toString()).getOrElse(""),
        Try(statsAsMap(LastSeenKey).asInstanceOf[BigInt].toString()).getOrElse(""),
        Try(statsAsMap(ErrorCountKey).asInstanceOf[BigInt].toString()).getOrElse(""),
        Try(edgeAsMap(EffectiveFromKey).asInstanceOf[BigInt].toString()).getOrElse(""),
        Try(edgeAsMap(EffectiveToKey).asInstanceOf[BigInt].toString()).getOrElse(""))
      stringBuilder.append(str)
      id = id + 1
    }
    stringBuilder.toString
  }

  private def createNodesCsvList(nodeToIdMap: mutable.Map[Node, Long]): String = {
    val stringBuilder = new mutable.StringBuilder(NodesHeader)
    nodeToIdMap.foreach {
      case (node, id) =>
        val name = surroundWithQuotesIfNecessary(node.name)
        val infrastructureProvider = surroundWithQuotesIfNecessary(node.infrastructureProvider.getOrElse(""))
        val tier = surroundWithQuotesIfNecessary(node.tier.getOrElse(""))
        val str = NodesFormatString.format(id, name, infrastructureProvider, tier)
        stringBuilder.append(str)
    }
    stringBuilder.toString
  }

  // See http://www.creativyst.com/Doc/Articles/CSV/CSV01.htm#FileFormat
  private def surroundWithQuotesIfNecessary(string: String): String = {
    val stringWithEscapedQuotes = string.replaceAll("\"", "\"\"")
    var stringToReturn = stringWithEscapedQuotes
    if (string.startsWith(" ") || string.endsWith(" ") || string.contains(",")) {
      stringToReturn = "\"" + string + "\""
    }
    stringToReturn
  }

  private def getNodeToIdMap(edgesList: List[Any]): mutable.Map[Node, Long] = {
    val sourceNodes = findNodesOfType(edgesList, SourceKey)
    val destinationNodes = findNodesOfType(edgesList, DestinationKey)
    val nodes = (sourceNodes ::: destinationNodes).distinct
    val nodeToIdMap = mutable.Map[Node, Long]()
    var nodeId: Long = 1L
    for (node <- nodes) {
      nodeToIdMap(node) = nodeId
      nodeId = nodeId + 1
    }
    nodeToIdMap
  }

  private def findNodesOfType(edgesList: List[Any], nodeType: String) = {
    val nodes = for {
      edge <- edgesList
    } yield {
      val edgeMap = edge.asInstanceOf[Map[String, Any]]
      val sourceNode: Option[Node] = findNode(nodeType, edgeMap)
      sourceNode
    }
    nodes.flatten
  }

  private def findNode(nodeType: String, edgeMap: Map[String, Any]): Option[Node] = {
    var optionNode: Option[Node] = None
    val nodeOptionAny = edgeMap.get(nodeType)
    if (nodeOptionAny.isDefined) {
      val map = nodeOptionAny.get.asInstanceOf[Map[String, Any]]
      val nameOptionAny = map.get(NameKey)
      if (nameOptionAny.isDefined) {
        val name = nameOptionAny.get.asInstanceOf[String]
        val tags = map.getOrElse(TagsKey, Map.empty[String, String]).asInstanceOf[Map[String, String]]
        val xHaystackInfrastructureProvider = Option(tags.getOrElse(InfrastructureProviderKey, null))
        val tier = Option(tags.getOrElse(TierKey, null))
        optionNode = Some(Node(name, xHaystackInfrastructureProvider, tier))
      }
    }
    optionNode
  }
}
