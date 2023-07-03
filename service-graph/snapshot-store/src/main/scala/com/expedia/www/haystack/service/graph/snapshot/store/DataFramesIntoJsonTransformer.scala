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

import com.expedia.www.haystack.service.graph.snapshot.store.Constants.EdgesKey
import com.expedia.www.haystack.service.graph.snapshot.store.DataFramesIntoJsonTransformer.{AddToMapError, WriteError}
import kantan.csv._
import kantan.csv.ops._
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

object DataFramesIntoJsonTransformer {
  val WriteError: String = "Problem reading JSON in write()"
  val AddToMapError: String = "Problem reading JSON in addToMap()"
}

class DataFramesIntoJsonTransformer(logger: Logger) {
  def this() {
    this(LoggerFactory.getLogger("com.expedia.www.haystack.service.graph.snapshot.store.DataFramesIntoJsonTransformer"))
  }

  private def addToMap(map: mutable.Map[Long, Node],
                       either: Either[ReadError, NodeWithId]): Unit = {
    either match {
      case Left(readError) => logger.error(AddToMapError, readError)
      case Right(nodeWithId) => map.put(nodeWithId.id, NodeWithId.nodeMapper(nodeWithId))
    }
  }

  private var prependComma = false

  private def write(stringBuilder: StringBuilder,
                    nodeIdVsNode: mutable.Map[Long, Node],
                    either: Either[ReadError, EdgeWithIds]): Unit = {
    either match {
      case Left(readError) => logger.error(WriteError, readError)
      case Right(edgeWithId) =>
        val edge = Edge.mapper(nodeIdVsNode, edgeWithId)
        stringBuilder.append(edge.toJson(prependComma))
        prependComma = true
    }
  }

  private implicit val nodeDecoder: RowDecoder[NodeWithId] =
    RowDecoder.decoder(0, 1, 2, 3)(NodeWithId.apply)

  private implicit val edgeDecoder: RowDecoder[EdgeWithIds] =
    RowDecoder.decoder(0, 1, 2, 3, 4, 5, 6, 7)(EdgeWithIds.apply)

  def parseDataFrames(nodesRawData: String,
                      edgesRawData: String): String = {
    val nodeIdVsNode = saveNodesToMap(nodesRawData)
    val stringBuilder = new StringBuilder
    stringBuilder.append("{\n  \"").append(EdgesKey).append("\": [\n")
    edgesRawData.asCsvReader[EdgeWithIds](rfc.withHeader).foreach(write(stringBuilder, nodeIdVsNode, _))
    stringBuilder.append("\n  ]\n}\n")
    stringBuilder.toString()
  }

  private def saveNodesToMap(nodesRawData: String): mutable.Map[Long, Node] = {
    val nodeIdVsNode = mutable.Map[Long, Node]()
    nodesRawData.asCsvReader[NodeWithId](rfc.withHeader).foreach(addToMap(nodeIdVsNode, _))
    nodeIdVsNode
  }
}
