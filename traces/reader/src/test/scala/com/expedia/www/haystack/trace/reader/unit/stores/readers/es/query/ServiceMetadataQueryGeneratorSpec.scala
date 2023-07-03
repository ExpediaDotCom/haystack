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

import com.expedia.www.haystack.trace.reader.config.entities.ServiceMetadataIndexConfiguration
import com.expedia.www.haystack.trace.reader.stores.readers.es.query.ServiceMetadataQueryGenerator
import com.expedia.www.haystack.trace.reader.unit.BaseUnitTestSpec
import com.google.gson.Gson

class ServiceMetadataQueryGeneratorSpec extends BaseUnitTestSpec {
  private val indexType = "metadata"
  private val serviceMetadataIndexConfiguration = ServiceMetadataIndexConfiguration(
    enabled = true,
    indexName = "service_metadata",
    indexType = indexType)

  describe("ServiceMetadataQueryGenerator") {
    it("should generate valid aggregation queries for service names") {
      Given("a query generator")
      val queryGenerator = new ServiceMetadataQueryGenerator(serviceMetadataIndexConfiguration)

      When("asked for aggregated service name")
      val query = queryGenerator.generateSearchServiceQuery()

      Then("generate a valid query")
      query.getType should be(indexType)
      query.getData(new Gson()) shouldEqual "{\n  \"size\" : 0,\n  \"aggregations\" : {\n    \"distinct_services\" : {\n      \"terms\" : {\n        \"field\" : \"servicename\",\n        \"size\" : 10000,\n        \"min_doc_count\" : 1,\n        \"shard_min_doc_count\" : 0,\n        \"show_term_doc_count_error\" : false,\n        \"order\" : [\n          {\n            \"_count\" : \"desc\"\n          },\n          {\n            \"_key\" : \"asc\"\n          }\n        ]\n      }\n    }\n  }\n}"
      query.toString shouldEqual "Search{uri=service_metadata/metadata/_search, method=POST}"
    }

    it("should generate valid aggregation queries for operation names") {
      Given("a query generator and a service name")
      val queryGenerator = new ServiceMetadataQueryGenerator(serviceMetadataIndexConfiguration)
      val serviceName = "test_service"
      When("asked for aggregated operation names")
      val query = queryGenerator.generateSearchOperationQuery(serviceName)

      Then("generate a valid query")
      query.getType should be(indexType)
    }
  }
}
