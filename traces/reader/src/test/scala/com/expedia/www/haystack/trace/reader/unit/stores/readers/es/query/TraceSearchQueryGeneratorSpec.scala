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
package com.expedia.www.haystack.trace.reader.unit.stores.readers.es.query

import com.expedia.open.tracing.api.{Field, TracesSearchRequest}
import com.expedia.www.haystack.trace.commons.clients.es.document.TraceIndexDoc
import com.expedia.www.haystack.trace.commons.config.entities.WhitelistIndexFieldConfiguration
import com.expedia.www.haystack.trace.reader.config.entities.SpansIndexConfiguration
import com.expedia.www.haystack.trace.reader.stores.readers.es.ESUtils._
import com.expedia.www.haystack.trace.reader.stores.readers.es.query.TraceSearchQueryGenerator
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec
import com.expedia.www.haystack.trace.reader.unit.stores.readers.es.query.helper.ExpressionTreeBuilder._
import com.google.gson.Gson
import io.searchbox.core.Search
import org.scalatest.BeforeAndAfterEach

class TraceSearchQueryGeneratorSpec extends BaseUnitTestSpec with BeforeAndAfterEach {
  private val spansIndexConfiguration = SpansIndexConfiguration(
    indexNamePrefix = "haystack-traces",
    indexType = "spans",
    indexHourTtl = 72,
    indexHourBucket = 6,
    useRootDocumentStartTime = false)

  var timezone: String = _

  override def beforeEach() {
    timezone = System.getProperty("user.timezone")
    System.setProperty("user.timezone", "CST")
  }

  override def afterEach(): Unit = {
    System.setProperty("user.timezone", timezone)
  }

  describe("TraceSearchQueryGenerator") {
    it("should generate valid search queries") {
      Given("a trace search request")
      val serviceName = "svcName"
      val operationName = "opName"
      val request = TracesSearchRequest
        .newBuilder()
        .addFields(Field.newBuilder().setName(TraceIndexDoc.SERVICE_KEY_NAME).setValue(serviceName).build())
        .addFields(Field.newBuilder().setName("operation").setValue(operationName).build())
        .setStartTime(1)
        .setEndTime(System.currentTimeMillis() * 1000)
        .setLimit(10)
        .build()
      val queryGenerator = new TraceSearchQueryGenerator(spansIndexConfiguration, "spans", WhitelistIndexFieldConfiguration())

      When("generating query")
      val query = queryGenerator.generate(request)

      Then("generate a valid query")
      query.getType should be("spans")
    }

    it("should generate caption independent search queries") {
      Given("a trace search request")
      val fieldKey = "svcName"
      val fieldValue = "opName"
      val request = TracesSearchRequest
        .newBuilder()
        .addFields(Field.newBuilder().setName(fieldKey).setValue(fieldValue).build())
        .setStartTime(1)
        .setEndTime(System.currentTimeMillis() * 1000)
        .setLimit(10)
        .build()
      val queryGenerator = new TraceSearchQueryGenerator(spansIndexConfiguration, "spans", new WhitelistIndexFieldConfiguration)

      When("generating query")
      val query: Search = queryGenerator.generate(request)

      Then("generate a valid query with fields in lowercase")
      query.toJson.contains(fieldKey.toLowerCase()) should be(true)
    }

    it("should generate valid search queries for expression tree based searches") {
      Given("a trace search request")

      val request = TracesSearchRequest
        .newBuilder()
        .setFilterExpression(operandLevelExpressionTree)
        .setStartTime(1)
        .setEndTime(System.currentTimeMillis() * 1000)
        .setLimit(10)
        .build()

      val queryGenerator = new TraceSearchQueryGenerator(spansIndexConfiguration, "spans", WhitelistIndexFieldConfiguration())

      When("generating query")
      val query: Search = queryGenerator.generate(request)

      Then("generate a valid query with fields in lowercase")
      query.toJson.contains(fieldKey.toLowerCase()) should be(true)
    }

    it("should generate valid search queries for expression tree based searches with span level searches") {
      Given("a trace search request")

      val startTime = 1531454400L * 1000 * 1000 // July 13, 2018 04:00:00 AM (in microSec)
      val endTime = 1531476000L * 1000 * 1000 // July 13, 2018 10:00:00 AM (in microSec)

      val request = TracesSearchRequest
        .newBuilder()
        .setFilterExpression(spanLevelExpressionTree)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setLimit(10)
        .build()

      val queryGenerator = new TraceSearchQueryGenerator(spansIndexConfiguration, "spans", WhitelistIndexFieldConfiguration())

      When("generating query")
      val query: Search = queryGenerator.generate(request)

      Then("generate a valid query with fields in lowercase")
      query.toJson.contains(fieldKey.toLowerCase()) should be(true)
      query.getIndex shouldBe "haystack-traces-2018-07-13-0,haystack-traces-2018-07-13-1"
    }

    it("should use UTC when determining which indexes to read") {
      Given("the system timezone is NOT UTC")
      System.setProperty("user.timezone", "CST")

      When("getting the indexes")
      val esIndexes = new TraceSearchQueryGenerator(spansIndexConfiguration, "spans", WhitelistIndexFieldConfiguration()).getESIndexes(1530806291394000L, 1530820646394000L, "haystack-traces", 4, 24)

      Then("they are correct based off of UTC")
      esIndexes shouldBe Vector("haystack-traces-2018-07-05-3", "haystack-traces-2018-07-05-4")
    }

    it("should query the mentioned index rather that calculated one") {
      Given("a trace search request")

      val request = TracesSearchRequest
        .newBuilder()
        .setFilterExpression(spanLevelExpressionTree)
        .setStartTime(1)
        .setEndTime(System.currentTimeMillis() * 1000)
        .setLimit(10)
        .build()

      val queryGenerator = new TraceSearchQueryGenerator(spansIndexConfiguration, "spans", WhitelistIndexFieldConfiguration())

      When("generating query")
      val query: Search = queryGenerator.generate(request, useSpecificIndices = false)

      Then("generate a valid query with given index name")
      query.toJson.contains(fieldKey.toLowerCase()) should be(true)
      query.getIndex shouldBe "haystack-traces"
    }

    it("should generate valid count query for expression tree with duration field types") {
      Given("a trace count request")
      val queryGenerator = new TraceSearchQueryGenerator(spansIndexConfiguration, "spans", WhitelistIndexFieldConfiguration())
      val requests = Seq(expressionTreeWithDurationFields) map {
        expression => {
          TracesSearchRequest
            .newBuilder()
            .setFilterExpression(expression)
            .setStartTime(1)
            .setEndTime(1100 * 1000 * 1000)
            .setLimit(10)
            .build()
        }
      }
      When("generating query")
      val queries: Seq[Search] = requests.map(req => queryGenerator.generate(req, useSpecificIndices = false))

      Then("generate a valid query")
      queries.map(query => query.getData(new Gson()).replaceAll("\n", "").replaceAll(" ", "")) shouldEqual Seq(
        "{\"size\":10,\"query\":{\"bool\":{\"must\":[{\"nested\":{\"query\":{\"range\":{\"spans.starttime\":{\"from\":1,\"to\":1100000000,\"include_lower\":true,\"include_upper\":true,\"boost\":1.0}}},\"path\":\"spans\",\"ignore_unmapped\":false,\"score_mode\":\"none\",\"boost\":1.0}}],\"filter\":[{\"nested\":{\"query\":{\"bool\":{\"filter\":[{\"term\":{\"spans.1\":{\"value\":\"1\",\"boost\":1.0}}},{\"term\":{\"spans.2\":{\"value\":\"2\",\"boost\":1.0}}},{\"term\":{\"spans.3\":{\"value\":\"3\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"path\":\"spans\",\"ignore_unmapped\":false,\"score_mode\":\"none\",\"boost\":1.0}},{\"nested\":{\"query\":{\"bool\":{\"filter\":[{\"term\":{\"spans.4\":{\"value\":\"4\",\"boost\":1.0}}},{\"term\":{\"spans.5\":{\"value\":\"5\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"path\":\"spans\",\"ignore_unmapped\":false,\"score_mode\":\"none\",\"boost\":1.0}},{\"nested\":{\"query\":{\"bool\":{\"filter\":[{\"term\":{\"spans.svcname\":{\"value\":\"svcValue\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"path\":\"spans\",\"ignore_unmapped\":false,\"score_mode\":\"none\",\"boost\":1.0}},{\"nested\":{\"query\":{\"bool\":{\"filter\":[{\"range\":{\"spans.duration\":{\"from\":500000,\"to\":null,\"include_lower\":false,\"include_upper\":true,\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"path\":\"spans\",\"ignore_unmapped\":false,\"score_mode\":\"none\",\"boost\":1.0}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"sort\":[{\"spans.starttime\":{\"order\":\"desc\",\"nested_path\":\"spans\"}}]}")
    }
  }
}
