/*
 *
 *     Copyright 2018 Expedia, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */

package com.expedia.www.haystack.commons.kstreams

import com.expedia.open.tracing.Span
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.processor.TimestampExtractor

class SpanTimestampExtractor extends TimestampExtractor with IteratorAgeMetricSupport {

  override def extract(record: ConsumerRecord[AnyRef, AnyRef], previousTimestamp: Long): Long = {

    //The startTime for span in computed in microseconds and hence dividing by 1000 to create the epochTimeInMs
    val spanStartTimeMs = record.value().asInstanceOf[Span].getStartTime / 1000
    updateIteratorAge(spanStartTimeMs)
    spanStartTimeMs
  }
}
