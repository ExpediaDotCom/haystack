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

package com.expedia.www.haystack.trace.reader.readers

import com.expedia.open.tracing.api.Trace
import com.expedia.www.haystack.trace.reader.readers.transformers.{PostTraceTransformationHandler, SpanTreeTransformer, TraceTransformationHandler, TraceTransformer}
import com.expedia.www.haystack.trace.reader.readers.validators.{TraceValidationHandler, TraceValidator}

import scala.util.Try

class TraceProcessor(validators: Seq[TraceValidator],
                     preValidationTransformers: Seq[TraceTransformer],
                     postValidationTransformers: Seq[SpanTreeTransformer]) {

  private val validationHandler: TraceValidationHandler = new TraceValidationHandler(validators)
  private val postTransformers: PostTraceTransformationHandler = new PostTraceTransformationHandler(postValidationTransformers)
  private val preTransformers: TraceTransformationHandler = new TraceTransformationHandler(preValidationTransformers)

  def process(trace: Trace): Try[Trace] = {
    for (trace <- Try(preTransformers.transform(trace));
         validated <- validationHandler.validate(trace)) yield postTransformers.transform(validated)
  }
}
