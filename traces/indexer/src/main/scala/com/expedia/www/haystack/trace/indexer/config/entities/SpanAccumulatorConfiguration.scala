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

package com.expedia.www.haystack.trace.indexer.config.entities

import com.expedia.www.haystack.trace.commons.packer.PackerType.PackerType

/**
  * @param minTracesPerCache minimum number of traces that will reside in each store.
  * @param maxEntriesAllStores maximum number of records across all state stores, one record is one span buffer object
  * @param pollIntervalMillis poll interval to gather the buffered-spans that are ready to emit out to sink
  * @param bufferingWindowMillis time window for which unique traceId will be hold to gather its child spans
  * @param packerType apply the compression on the spanbuffer before storing to trace-backend
  */
case class SpanAccumulatorConfiguration(minTracesPerCache: Int,
                                        maxEntriesAllStores: Int,
                                        pollIntervalMillis: Long,
                                        bufferingWindowMillis: Long,
                                        packerType: PackerType)
