/*
 *  Copyright 2018 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.www.haystack.collector.commons.config

import com.expedia.www.haystack.collector.commons.config.Format.Format


object Format extends Enumeration {
  type Format = Value
  val JSON = Value("json")
  val PROTO = Value("proto")
}

case class SpanValidation(spanMaxSize: SpanMaxSize)

case class SpanMaxSize(enable: Boolean,
                       logOnly: Boolean,
                       maxSizeLimit: Int,
                       infoTagKey: String,
                       infoTagValue: String,
                       skipTags: Seq[String],
                       skipServices: Seq[String])

case class ExtractorConfiguration(outputFormat: Format,
                                  spanValidation: SpanValidation)

