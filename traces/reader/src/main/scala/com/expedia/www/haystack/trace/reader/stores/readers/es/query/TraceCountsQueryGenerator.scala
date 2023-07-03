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

import com.expedia.open.tracing.api.TraceCountsRequest
import com.expedia.www.haystack.trace.commons.clients.es.document.TraceIndexDoc
import com.expedia.www.haystack.trace.commons.config.entities.WhitelistIndexFieldConfiguration
import com.expedia.www.haystack.trace.reader.config.entities.SpansIndexConfiguration
import io.searchbox.core.Search
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

import scala.collection.JavaConverters._


object TraceCountsQueryGenerator {
  val COUNT_HISTOGRAM_NAME = "countagg"
}

class TraceCountsQueryGenerator(config: SpansIndexConfiguration,
                                nestedDocName: String,
                                whitelistIndexFields: WhitelistIndexFieldConfiguration)
  extends SpansIndexQueryGenerator(nestedDocName, whitelistIndexFields) {

  import TraceCountsQueryGenerator._

  def generate(request: TraceCountsRequest, useSpecificIndices: Boolean): Search = {
    require(request.getStartTime > 0)
    require(request.getEndTime > 0)
    require(request.getInterval > 0)

    if (useSpecificIndices) {
      generate(request)
    } else {
      new Search.Builder(buildQueryString(request))
        .addIndex(config.indexNamePrefix)
        .addType(config.indexType)
        .build()
    }
  }

  def generate(request: TraceCountsRequest): Search = {
    require(request.getStartTime > 0)
    require(request.getEndTime > 0)
    require(request.getInterval > 0)

    // create ES count query
    val targetIndicesToSearch = getESIndexes(
      request.getStartTime,
      request.getEndTime,
      config.indexNamePrefix,
      config.indexHourBucket,
      config.indexHourTtl).asJava

    new Search.Builder(buildQueryString(request))
      .addIndex(targetIndicesToSearch)
      .addType(config.indexType)
      .build()
  }

  private def buildQueryString(request: TraceCountsRequest): String = {
    val query: BoolQueryBuilder =
      if(request.hasFilterExpression) {
        createExpressionTreeBasedQuery(request.getFilterExpression)
      }
      else {
        // this is deprecated
        createFilterFieldBasedQuery(request.getFieldsList)
      }

    query.must(QueryBuilders.rangeQuery(TraceIndexDoc.START_TIME_KEY_NAME).gte(request.getStartTime).lte(request.getEndTime))

    val aggregation = AggregationBuilders
      .histogram(COUNT_HISTOGRAM_NAME)
      .field(TraceIndexDoc.START_TIME_KEY_NAME)
      .interval(request.getInterval)
      .extendedBounds(request.getStartTime, request.getEndTime)

    new SearchSourceBuilder()
      .query(query)
      .aggregation(aggregation)
      .size(0)
      .toString
  }
}
