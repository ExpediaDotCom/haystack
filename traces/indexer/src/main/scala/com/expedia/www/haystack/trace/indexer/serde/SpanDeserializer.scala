/*
 *  Copyright 2017 Expedia, Inc.
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

package com.expedia.www.haystack.trace.indexer.serde

import java.util

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trace.indexer.metrics.AppMetricNames
import org.apache.kafka.common.serialization.Deserializer

class SpanDeserializer extends Deserializer[Span] with MetricsSupport {

  private val spanDeserMeter = metricRegistry.meter(AppMetricNames.SPAN_PROTO_DESER_FAILURE)

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()

  /**
    * converts the binary protobuf bytes into Span object
    * @param data serialized bytes of Span
    * @return
    */
  override def deserialize(topic: String, data: Array[Byte]): Span = {
    try {
      if(data == null || data.length == 0) null else Span.parseFrom(data)
    } catch {
      case _: Exception =>
        /* may be log and add metric */
        spanDeserMeter.mark()
        null
    }
  }
}
