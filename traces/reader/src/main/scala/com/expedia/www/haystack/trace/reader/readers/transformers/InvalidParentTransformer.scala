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
  * If there are spans with invalid parentId in the trace, mark root to be their parentId
  *
  * **Apply this transformer only if you are not confident about clients sending in parentIds properly**
  */
class InvalidParentTransformer extends SpanTreeTransformer {
  override def transform(spanForest: MutableSpanForest): MutableSpanForest = {
    val rootTrees = spanForest.getAllTrees.filter(_.span.getParentSpanId.isEmpty)

    require(rootTrees.size == 1)

    spanForest.mergeTreesUnder(rootTrees.head)
  }
}
