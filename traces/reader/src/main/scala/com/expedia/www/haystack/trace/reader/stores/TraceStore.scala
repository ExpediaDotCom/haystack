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

package com.expedia.www.haystack.trace.reader.stores

import com.expedia.open.tracing.api._

import scala.concurrent.Future

trait TraceStore extends AutoCloseable {
  def getTrace(traceId: String): Future[Trace]
  def searchTraces(request: TracesSearchRequest): Future[Seq[Trace]]
  def getFieldNames: Future[FieldNames]
  def getFieldValues(request: FieldValuesRequest): Future[Seq[String]]
  def getTraceCounts(request: TraceCountsRequest): Future[TraceCounts]
  def getRawTraces(request: RawTracesRequest): Future[Seq[Trace]]
}
