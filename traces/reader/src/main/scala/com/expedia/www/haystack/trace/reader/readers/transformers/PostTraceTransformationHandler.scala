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

import com.expedia.open.tracing.api.Trace
import com.expedia.www.haystack.trace.reader.readers.utils.MutableSpanForest

import scala.collection.JavaConverters._

/**
  * takes a sequence of [[SpanTreeTransformer]] and apply transform functions on the chain
  *
  * transformer functions takes [[MutableSpanForest]]  and generates a transformed [[MutableSpanForest]]
  * [[PostTraceTransformationHandler]] takes a [[Seq]] of [[SpanTreeTransformer]] and applies chaining on them,
  * providing response [[List]] of a transformer to the next one
  *
  * @param transformerSeq
  */
class PostTraceTransformationHandler(transformerSeq: Seq[SpanTreeTransformer]) {
  private val transformerChain =
    Function.chain(transformerSeq.foldLeft(Seq[MutableSpanForest => MutableSpanForest]())((seq, transformer) => seq :+ transformer.transform _))

  def transform(trace: Trace): Trace = {
    // build a span forest from the given spans in a trace
    val spanForest = MutableSpanForest(trace.getChildSpansList.asScala)

    // transform the forest and yeild only one tree
    val transformedSpanForest = transformerChain(spanForest)

    Trace
      .newBuilder()
      .setTraceId(trace.getTraceId)
      .addAllChildSpans(transformedSpanForest.getUnderlyingSpans.asJavaCollection)
      .build()
  }
}
