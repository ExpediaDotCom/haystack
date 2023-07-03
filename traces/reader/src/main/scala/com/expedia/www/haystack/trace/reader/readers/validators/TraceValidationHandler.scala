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

import scala.util.{Success, Try}

/**
  * takes a sequence of [[TraceValidator]] and apply validations on the trace
  * will either return Success or Failure with the first failed validation as exception
  *
  * @param validatorSeq sequence of validations to apply on the trace
  */
class TraceValidationHandler(validatorSeq: Seq[TraceValidator]) {
  def validate(trace: Trace): Try[Trace] = {
    validatorSeq
      .map(_.validate(trace))
      .find(_.isFailure)
      .getOrElse(Success(trace))
  }
}
