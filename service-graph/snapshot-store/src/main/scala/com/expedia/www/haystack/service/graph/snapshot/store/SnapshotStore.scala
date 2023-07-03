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

import java.time.Instant
import java.time.format.DateTimeFormatterBuilder

trait SnapshotStore {
  /**
    * Builds a SnapshotStore implementation given arguments to pass to the constructor
    *
    * @param constructorArguments arguments to pass to the constructor
    * @return the concrete SnapshotStore to use
    */
  def build(constructorArguments: Array[String]): SnapshotStore

  /**
    * Writes a string to the persistent store
    *
    * @param instant date/time of the write, used to create the name, which will later be used in read() and purge()
    * @param content String to write
    * @return implementation-dependent value; see implementation documentation for details
    */
  def write(instant: Instant,
            content: String): AnyRef

  /**
    * Reads content from the persistent store
    *
    * @param instant date/time of the read
    * @return the content of the youngest item whose ISO-8601-based name is earlier or equal to instant
    */
  def read(instant: Instant): Option[String]

  /**
    * Purges items from the persistent store (optional operation; the S3 implementation of SnapshotStore will use an S3
    * lifecycle rule to purge items, but the file implementation must purge old files)
    *
    * @param instant date/time of items to be purged; items whose ISO-8601-based name is earlier than or equal to
    *                instant will be purged
    * @return the number of items purged
    */
  def purge(instant: Instant): Integer = {
    0
    // Override if purge code is needed by the particular SnapshotStore implementation
  }

  private val formatter = new DateTimeFormatterBuilder().appendInstant(3).toFormatter

  def createIso8601FileName(instant: Instant): String = {
    formatter.format(instant)
  }

  // Not stateful, so only one object is needed
  private val jsonIntoDataFramesTransformer = new JsonIntoDataFramesTransformer

  def transformJsonToNodesAndEdges(json: String): NodesAndEdges = {
    jsonIntoDataFramesTransformer.parseJson(json)
  }

  def transformNodesAndEdgesToJson(nodesRawData: String,
                                   edgesRawData: String): String = {
    // Stateful because of instance variable DataFramesIntoJsonTransformer.prependComma, so each parse needs one
    val dataFramesIntoJsonTransformer = new DataFramesIntoJsonTransformer

    dataFramesIntoJsonTransformer.parseDataFrames(nodesRawData, edgesRawData)
  }
}
