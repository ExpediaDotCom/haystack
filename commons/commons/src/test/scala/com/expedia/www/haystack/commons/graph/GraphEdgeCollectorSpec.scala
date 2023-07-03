/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.www.haystack.commons.graph

import com.expedia.open.tracing.Tag.TagType
import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.commons.entities.TagKeys
import com.expedia.www.haystack.commons.unit.UnitTestSpec


class GraphEdgeCollectorSpec extends UnitTestSpec {


  "graph edge collector" should {

    "should collect predefined collection tags" in {
      Given("a graph edge collector with a list of tags to be collected")
      val tags = Set("tag1", "tag2")
      And("a span containing tags")
      val span = Span.newBuilder().addTags(Tag.newBuilder().setKey("tag1").setVStr("val1")).build()

      When("collecting the tags for a given span")
      val edgeTagCollector = new GraphEdgeTagCollector(tags)
      val collectedTags = edgeTagCollector.collectTags(span)

      Then("only the predefined tags that are also part of the span should be collected")
      collectedTags.get("tag1") should be (Some("val1"))
      collectedTags should not contain ("tag2")
    }

    "should always collect default tags" in {
      Given("a graph edge collector and an empty tag list")
      val tags = Set[String]()
      And("a span containing only the default tag")
      val span = Span.newBuilder().addTags(Tag.newBuilder().setKey(TagKeys.ERROR_KEY).setVBool(true)
                  .setType(TagType.BOOL)).build()

      When("collecting the tags for a given span")
      val edgeTagCollector = new GraphEdgeTagCollector(tags)
      val collectedTags = edgeTagCollector.collectTags(span)

      Then("only the predefined tags that are also part of the span should be collected")
      collectedTags.get(TagKeys.ERROR_KEY) should be (Some("true"))
    }

    "should throw an exception if tag type cannot be converted to string" in {
      Given("a graph edge collector and an empty tag list")
      val tags = Set("test")
      And("a span containing a tag whose type is not supported")
      val span = Span.newBuilder().addTags(Tag.newBuilder().setKey("test").setType(TagType.BINARY)).build()

      When("collecting the tags for a given span")
      Then("only the predefined tags that are also part of the span should be collected")
      val edgeTagCollector = new GraphEdgeTagCollector(tags)
      intercept[IllegalArgumentException] {
         edgeTagCollector.collectTags(span)
      }
    }

  }

}
