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

import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.trace.reader.readers.utils.AuxiliaryTags

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * add the infrastructure tag in all the spans coming for a service, if any of its span(client or server) contains it.
  * Many services send the infrastructure tag only in the server span for ease and saving transfer cost.
  * This transformer refill the infrastructure tags if required.
  */
class InfrastructureTagTransformer extends TraceTransformer {

  override def transform(spans: Seq[Span]): Seq[Span] = {
    val serviceWithInfraTags = mutable.HashMap[String, mutable.ListBuffer[Tag]]()
    val spansWithoutInfraTags = mutable.HashSet[Span]()

    spans.foreach { span =>
      var infraTagsPresent = false
      span.getTagsList.asScala.foreach { tag =>
        if (tag.getKey == AuxiliaryTags.INFRASTRUCTURE_PROVIDER || tag.getKey == AuxiliaryTags.INFRASTRUCTURE_LOCATION) {
          val tags = serviceWithInfraTags.getOrElseUpdate(span.getServiceName, mutable.ListBuffer[Tag]())
          tags.append(tag)
          infraTagsPresent = true
        }
      }

      if (!infraTagsPresent) {
        spansWithoutInfraTags += span
      }
    }

    spans.map { span =>
      if (serviceWithInfraTags.contains(span.getServiceName) && spansWithoutInfraTags.contains(span)) {
        span.toBuilder.addAllTags(serviceWithInfraTags(span.getServiceName).asJava).build()
      } else {
        span
      }
    }
  }
}
