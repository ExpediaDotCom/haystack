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

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.time.Instant
import java.util.{Comparator, Optional}

class FileSnapshotStore(val directoryName: String) extends SnapshotStore {
  private val directory = Paths.get(directoryName)

  def this() = {
    this("/")
  }

  /**
    * Returns a FileSnapshotStore using the directory name specified
    *
    * @param constructorArguments constructorArguments[0] must specify the directory to which snapshots will be stored
    * @return the concrete FileSnapshotStore to use
    */
  override def build(constructorArguments: Array[String]): SnapshotStore = {
    new FileSnapshotStore(constructorArguments(0))
  }

  /**
    * Writes a string to the persistent store
    *
    * @param instant date/time of the write, used to create the name, which will later be used in read() and purge()
    * @param content String to write
    * @return a tuple of the paths of the two CSV files (one for nodes, one for edges, in that order) to which the nodes
    *         and edges were written; @see java.nio.file.Path
    */
  override def write(instant: Instant,
                     content: String): (Path, Path) = {
    if (!Files.exists(directory)) {
      Files.createDirectories(directory)
    }
    val nodesAndEdges = transformJsonToNodesAndEdges(content)
    val nodePath = write(instant, Constants._Nodes, nodesAndEdges.nodes)
    val edgesPath = write(instant, Constants._Edges, nodesAndEdges.edges)
    (nodePath, edgesPath)
  }

  private def write(instant: Instant, suffix: String, content: String) = {
    val path = directory.resolve(createIso8601FileName(instant) + suffix)
    Files.write(path, content.getBytes(StandardCharsets.UTF_8))
  }

  private val pathNameComparator: Comparator[Path] = (o1: Path, o2: Path) => o1.toString.compareTo(o2.toString)
  /**
    * Reads content from the persistent store
    *
    * @param instant date/time of the read
    * @return the content, transformed to JSON, of the youngest _nodes and _edges files whose ISO-8601-based name is
    *         earlier or equal to instant
    */
  override def read(instant: Instant): Option[String] = {
    var optionString: Option[String] = None
    val fileNameForInstant = createIso8601FileName(instant)
    val fileToUse: Optional[Path] = Files
      .walk(directory, 1)
      .filter(_.toFile.getName.endsWith(Constants._Nodes))
      .filter(_.toFile.getName.substring(0, fileNameForInstant.length) <= fileNameForInstant)
      .max(pathNameComparator)
    if (fileToUse.isPresent) {
      val nodesRawData = Files.readAllLines(fileToUse.get).toArray.mkString("\n")
      val edgesPath = Paths.get(fileToUse.get().toAbsolutePath.toString.replace(Constants._Nodes, Constants._Edges))
      val edgesRawData = Files.readAllLines(edgesPath).toArray.mkString("\n")
      optionString = Some(transformNodesAndEdgesToJson(nodesRawData, edgesRawData))
    }
    optionString
  }

  /**
    * Purges items from the persistent store
    *
    * @param instant date/time of items to be purged; items whose ISO-8601-based name is earlier than or equal to
    *                instant will be purged
    * @return the number of items purged
    */
  override def purge(instant: Instant): Integer = {
    val fileNameForInstant = createIso8601FileName(instant)
    val pathsToPurge: Array[AnyRef] = Files
      .walk(directory, 1)
      .filter(isNodesOrEdgesFile(_))
      .filter(_.toFile.getName.substring(0, fileNameForInstant.length) <= fileNameForInstant)
      .toArray
    for (anyRef <- pathsToPurge) {
      Files.delete(anyRef.asInstanceOf[Path])
    }
    pathsToPurge.length
  }

  private def isNodesOrEdgesFile(path: Path): Boolean = {
    val name = path.toFile.getName
    name.endsWith(Constants._Nodes) || name.endsWith(Constants._Edges)
  }

}
