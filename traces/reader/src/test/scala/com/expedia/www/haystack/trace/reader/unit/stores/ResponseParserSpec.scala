/*
 *  Copyright 2017 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */

package com.expedia.www.haystack.trace.reader.unit.stores

import com.expedia.www.haystack.trace.reader.stores.ResponseParser
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec
import com.google.gson.{Gson, JsonParser}
import io.searchbox.core.SearchResult
import scala.concurrent.ExecutionContext.Implicits.global

class ResponseParserSpec extends BaseUnitTestSpec with ResponseParser {
  describe("ResponseParserSpec") {
    it("should be able to parse the search result to trace counts") {

      Given("a trace search response")
      val result = new SearchResult(new Gson())
      result.setSucceeded(true)
      result.setJsonString(getJson())
      result.setJsonObject(new JsonParser().parse(getJson()).getAsJsonObject)

      When("map search result to trace counts")
      val traceCounts = mapSearchResultToTraceCounts(result)

      Then("generate a valid query")
      traceCounts should not be None
      traceCounts.map(traceCounts => traceCounts.getTraceCountCount shouldEqual 11)
    }
  }

  def getJson(): String = {
    """
      |{
      |  "took": 41810,
      |  "timed_out": false,
      |  "_shards": {
      |    "total": 240,
      |    "successful": 240,
      |    "skipped": 0,
      |    "failed": 0
      |  },
      |  "hits": {
      |    "total": 10052727254,
      |    "max_score": 0.0,
      |    "hits": []
      |  },
      |  "aggregations": {
      |    "spans": {
      |      "doc_count": 23138047525,
      |      "spans": {
      |        "doc_count": 2604513,
      |        "__count_per_interval": {
      |          "buckets": [
      |            {
      |              "key": 1.52690406E15,
      |              "doc_count": 150949
      |            },
      |            {
      |              "key": 1.52690412E15,
      |              "doc_count": 262163
      |            },
      |            {
      |              "key": 1.52690418E15,
      |              "doc_count": 259394
      |            },
      |            {
      |              "key": 1.52690424E15,
      |              "doc_count": 253247
      |            },
      |            {
      |              "key": 1.5269043E15,
      |              "doc_count": 253589
      |            },
      |            {
      |              "key": 1.52690436E15,
      |              "doc_count": 261232
      |            },
      |            {
      |              "key": 1.52690442E15,
      |              "doc_count": 258264
      |            },
      |            {
      |              "key": 1.52690448E15,
      |              "doc_count": 270179
      |            },
      |            {
      |              "key": 1.52690454E15,
      |              "doc_count": 266545
      |            },
      |            {
      |              "key": 1.5269046E15,
      |              "doc_count": 264921
      |            },
      |            {
      |              "key": 1.52690466E15,
      |              "doc_count": 104084
      |            }
      |          ]
      |        }
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  }
}
