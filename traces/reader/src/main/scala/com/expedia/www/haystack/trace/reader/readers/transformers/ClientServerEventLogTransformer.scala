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
import com.expedia.www.haystack.trace.commons.utils.{SpanMarkers, SpanUtils}

import scala.collection.JavaConverters._

/**
  * add log events(if not present) using span.kind tag value
  */
class ClientServerEventLogTransformer extends TraceTransformer {

  override def transform(spans: Seq[Span]): Seq[Span] = {
    spans.map(span => {
      span.getTagsList.asScala.find(_.getKey == SpanMarkers.SPAN_KIND_TAG_KEY).map(_.getVStr) match {
        case Some(SpanMarkers.SERVER_SPAN_KIND) if !SpanUtils.containsServerLogTag(span) => SpanUtils.addServerLogTag(span)
        case Some(SpanMarkers.CLIENT_SPAN_KIND) if !SpanUtils.containsClientLogTag(span) => SpanUtils.addClientLogTag(span)
        case _ => span
      }
    })
  }
}
