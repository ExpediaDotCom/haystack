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

package com.expedia.www.haystack.trace.indexer.config.entities

import java.util.Properties

/** @param numStreamThreads num of stream threads
  * @param pollTimeoutMs kafka consumer poll timeout
  * @param consumerProps consumer config object
  * @param producerProps producer config object
  * @param produceTopic producer topic
  * @param consumeTopic consumer topic
  * @param consumerCloseTimeoutInMillis kafka consumer close timeout
  * @param commitOffsetRetries retries of commit offset failed
  * @param commitBackoffInMillis if commit operation fails, retry with backoff
  * @param maxWakeups maximum wakeups allowed
  * @param wakeupTimeoutInMillis wait timeout for consumer.poll() to return zero or more records
  */
case class KafkaConfiguration(numStreamThreads: Int,
                              pollTimeoutMs: Long,
                              consumerProps: Properties,
                              producerProps: Properties,
                              produceTopic: String,
                              consumeTopic: String,
                              consumerCloseTimeoutInMillis: Int,
                              commitOffsetRetries: Int,
                              commitBackoffInMillis: Long,
                              maxWakeups: Int,
                              wakeupTimeoutInMillis: Int)
