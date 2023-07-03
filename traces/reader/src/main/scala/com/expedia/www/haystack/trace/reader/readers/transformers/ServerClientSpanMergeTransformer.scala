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

package com.expedia.www.haystack.trace.reader.readers.transformers

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.trace.reader.readers.utils.{MutableSpanForest, SpanMerger}

class ServerClientSpanMergeTransformer extends SpanTreeTransformer {

  private def shouldMerge(parentSpan: Span, childSpan: Span) = {
    childSpan.getServiceName != parentSpan.getServiceName &&
      !SpanMerger.isAlreadyMergedSpan(parentSpan) &&
      !SpanMerger.isAlreadyMergedSpan(childSpan) &&
      SpanMerger.shouldMergeSpanKinds(parentSpan, childSpan)
  }

  override def transform(spanForest: MutableSpanForest): MutableSpanForest = {
    spanForest.collapse((tree) =>
      tree.children match {
        case Seq(singleChild) if shouldMerge(tree.span, singleChild.span) =>
          Some(SpanMerger.mergeParentChildSpans(tree.span, singleChild.span))
        case _ => None
      })

    spanForest
  }
}
