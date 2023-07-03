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

package com.expedia.www.haystack.trace.reader.readers.validators

import com.expedia.open.tracing.api.Trace
import com.expedia.www.haystack.trace.reader.exceptions.InvalidTraceException

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * validates if spans in the trace has a valid parentIds
  * assumes that traceId is a non-empty string and there is a single root, apply [[TraceIdValidator]] and [[RootValidator]] to make sure
  */
class ParentIdValidator extends TraceValidator {
  override def validate(trace: Trace): Try[Trace] = {
    val spans = trace.getChildSpansList.asScala
    val spanIdSet = spans.map(_.getSpanId).toSet

    if (!spans.forall(sp => spanIdSet.contains(sp.getParentSpanId) || sp.getParentSpanId.isEmpty)) {
      Failure(new InvalidTraceException(s"spans without valid parent found for traceId=${spans.head.getTraceId}"))
    }  else if (!spans.forall(sp => sp.getSpanId != sp.getParentSpanId)) {
      Failure(new InvalidTraceException(s"same parent and span id found for one ore more span for traceId=${spans.head.getTraceId}"))
    }
    else {
      Success(trace)
    }
  }
}
