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

import com.expedia.open.tracing.api.FieldValuesRequest
import com.expedia.www.haystack.trace.commons.config.entities.WhitelistIndexFieldConfiguration
import com.expedia.www.haystack.trace.reader.config.entities.SpansIndexConfiguration
import io.searchbox.core.Search
import org.elasticsearch.search.builder.SearchSourceBuilder

class FieldValuesQueryGenerator(config: SpansIndexConfiguration,
                                nestedDocName: String,
                                indexConfiguration: WhitelistIndexFieldConfiguration)
  extends SpansIndexQueryGenerator(nestedDocName, indexConfiguration) {

  def generate(request: FieldValuesRequest): Search = {
    new Search.Builder(buildQueryString(request))
      .addIndex(s"${config.indexNamePrefix}*")
      .addType(config.indexType)
      .build()
  }

  private def buildQueryString(request: FieldValuesRequest): String = {
    val query = createFilterFieldBasedQuery(request.getFiltersList)
    if (query.filter().size() > 0) {
      new SearchSourceBuilder()
        .aggregation(createNestedAggregationQueryWithNestedFilters(request.getFieldName.toLowerCase, request.getFiltersList))
        .size(0)
        .toString
    } else {
      new SearchSourceBuilder()
        .aggregation(createNestedAggregationQuery(request.getFieldName.toLowerCase))
        .size(0)
        .toString
    }
  }
}