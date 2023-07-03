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

package com.expedia.www.haystack.trace.reader.readers.transformers

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.trace.reader.readers.utils._

/**
  * Merges partial spans and generates a single Span combining a client and corresponding server span
  * gracefully fallback to collapse to a single span if there are multiple or missing client/server spans
  */
class PartialSpanTransformer extends SpanTreeTransformer {
  override def transform(spanForest: MutableSpanForest): MutableSpanForest = {
    var hasAnySpanMerged = false

    val mergedSpans: Seq[Span] = spanForest.getUnderlyingSpans.groupBy(_.getSpanId).map((pair) => pair._2 match {
      case Seq(span: Span) => span
      case spans: Seq[Span] =>
        hasAnySpanMerged = true
        SpanMerger.mergeSpans(spans)
    }).toSeq

    spanForest.updateUnderlyingSpans(mergedSpans, hasAnySpanMerged)
  }
}
