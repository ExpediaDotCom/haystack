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

import com.expedia.www.haystack.trace.reader.readers.utils.MutableSpanForest

/**
  *  Orders spans in natural ordering - root followed by other spans ordered by start time
  *
  *  Assumes there is only one root in give spans List
  *  corresponding validations are done in [[com.expedia.www.haystack.trace.reader.readers.validators.RootValidator]]
  *  corresponding transformation are done in [[InvalidRootTransformer]]
  */
class SortSpanTransformer extends SpanTreeTransformer {
  override def transform(spanForest: MutableSpanForest): MutableSpanForest = {
    require(spanForest.getAllTrees.size <= 1)

    val (left, right) = spanForest.getUnderlyingSpans.partition(_.getParentSpanId.isEmpty)
    val sortedSpans = left.toList.head :: right.toList.sortBy(_.getStartTime)
    spanForest.updateUnderlyingSpans(sortedSpans, triggerForestUpdate = false)
  }
}
