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

package com.expedia.www.haystack.trace.reader.stores.readers.es.query

import com.expedia.www.haystack.trace.reader.config.entities.ServiceMetadataIndexConfiguration
import io.searchbox.core.Search
import org.elasticsearch.index.query.QueryBuilders.termQuery
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

class ServiceMetadataQueryGenerator(config: ServiceMetadataIndexConfiguration) {
  private val SERVICE_NAME_KEY = "servicename"
  private val OPERATION_NAME_KEY = "operationname"
  private val LIMIT = 10000

  def generateSearchServiceQuery(): Search = {
    val serviceAggregationQuery = buildServiceAggregationQuery()
    generateSearchQuery(serviceAggregationQuery)
  }

  def generateSearchOperationQuery(serviceName: String): Search = {
    val serviceAggregationQuery = buildOperationAggregationQuery(serviceName)
    generateSearchQuery(serviceAggregationQuery)
  }

  private def generateSearchQuery(queryString: String): Search = {
    new Search.Builder(queryString)
      .addIndex(config.indexName)
      .addType(config.indexType)
      .build()
  }

  private def buildServiceAggregationQuery(): String = {
    val aggr = AggregationBuilders.terms("distinct_services").field(SERVICE_NAME_KEY).size(LIMIT)
    new SearchSourceBuilder().aggregation(aggr).size(0).toString
  }

  private def buildOperationAggregationQuery(serviceName: String): String = {
    new SearchSourceBuilder()
      .query(termQuery(SERVICE_NAME_KEY, serviceName))
      .fetchSource(OPERATION_NAME_KEY, SERVICE_NAME_KEY)
      .size(LIMIT)
      .toString
  }
}
