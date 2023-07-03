/*
 *  Copyright 2018 Expedia, Inc.
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

package com.expedia.www.haystack.trace.indexer.config.entities

import com.expedia.www.haystack.commons.retries.RetryOperation

/**
  * Configurations for writing service metadata to elastic search
  * @param enabled: enable writing service metadata, if its set to false, list of service_names and operation names would be fetched from elastic search traces index, which is an expensive aggregation
  * @param esEndpoint: http endpoint to connect
  * @param indexTemplateJson: template as json that will be applied when the app runs, this is optional  * @param username
  * @param password: password for the es
  * @param consistencyLevel: consistency level of writes, for e.g. one, quoram
  * @param indexName: name of the elastic search index where the data is written
  * @param indexType: elastic search index type
  * @param connectionTimeoutMillis : connection timeout in millis
  * @param readTimeoutMillis: read timeout in millis
  * @param maxInFlightBulkRequests: max bulk writes that can be run in parallel
  * @param maxDocsInBulk: maximum number of index documents in a single bulk
  * @param maxBulkDocSizeInBytes maximum size (in bytes) of a single bulk request
  * @param flushIntervalInSec: interval for collecting service name operation names in memory before flushing to es
  * @param flushOnMaxOperationCount: maximum number of unique operations to force flushing to es
  * @param retryConfig: retry max retries limit, initial backoff and exponential factor values
  */

case class ServiceMetadataWriteConfiguration(enabled: Boolean,
                                             esEndpoint: String,
                                             username: Option[String],
                                             password: Option[String],
                                             consistencyLevel: String,
                                             indexTemplateJson: Option[String],
                                             indexName: String,
                                             indexType: String,
                                             connectionTimeoutMillis: Int,
                                             readTimeoutMillis: Int,
                                             maxInFlightBulkRequests: Int,
                                             maxDocsInBulk: Int,
                                             maxBulkDocSizeInBytes: Int,
                                             flushIntervalInSec: Int,
                                             flushOnMaxOperationCount: Int,
                                             retryConfig: RetryOperation.Config
                                            ) {
  require(maxInFlightBulkRequests > 0)
}
