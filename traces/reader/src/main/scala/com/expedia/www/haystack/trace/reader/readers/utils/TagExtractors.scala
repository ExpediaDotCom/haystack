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

package com.expedia.www.haystack.trace.reader.readers.utils

import com.expedia.open.tracing.Span
import scala.collection.JavaConverters._

object TagExtractors {
  def containsTag(span: Span, tagKey: String): Boolean = {
    span.getTagsList.asScala.exists(_.getKey == tagKey)
  }

  def extractTagStringValue(span: Span, tagKey: String): String = {
    span.getTagsList.asScala.find(_.getKey == tagKey) match {
      case Some(t) => t.getVStr
      case _ => ""
    }
  }

  def extractTagLongValue(span: Span, tagKey: String): Long = {
    span.getTagsList.asScala.find(_.getKey == tagKey) match {
      case Some(t) => t.getVLong
      case _ => -1
    }
  }
}
