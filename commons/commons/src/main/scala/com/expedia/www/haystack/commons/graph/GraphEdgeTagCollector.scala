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
package com.expedia.www.haystack.commons.graph

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.Tag.TagType
import com.expedia.www.haystack.commons.entities.TagKeys

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Define tag names that should be collected when building a GraphEdge.
  * @param tags: Set of tag names to be collected for building the graph edge
  */
class GraphEdgeTagCollector(tags: Set[String] = Set()) {

  /**
    * Default tags that will always be collected.
    */
  private val defaultTags: Set[String] = Set(TagKeys.ERROR_KEY)

  private val filteredTags = defaultTags ++ tags

  /**
    * @param span: Span containing all the tags
    * @return Filtered list of tag keys and values in the span that match the pre defined tag names.
    */
  def collectTags(span: Span): Map[String, String] = {
    val edgeTags =  mutable.Map[String, String]()
    span.getTagsList.asScala.filter(t => filteredTags.contains(t.getKey)).foreach { tag =>
      tag.getType match {
        case TagType.STRING => edgeTags += (tag.getKey -> tag.getVStr)
        case TagType.BOOL => edgeTags += (tag.getKey -> tag.getVBool.toString)
        case TagType.DOUBLE =>   edgeTags += (tag.getKey -> tag.getVDouble.toString)
        case TagType.LONG =>   edgeTags += (tag.getKey -> tag.getVLong.toString)
        case _ =>   throw new IllegalArgumentException("Invalid tag type detected.")
      }
    }
    edgeTags.toMap
  }
}


