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
package com.expedia.www.haystack.commons.kstreams.app

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import org.apache.kafka.streams.KafkaStreams

/**
  * Simple service wrapper over `KafkaStreams` to manage the life cycle of the
  * instance.
  *
  * @param kafkaStreams underlying KafkaStreams instance that needs to be
  *                     managed
  * @param closeWaitInSeconds time to wait in seconds while stopping KafkaStreams
  */
class ManagedKafkaStreams(kafkaStreams: KafkaStreams, closeWaitInSeconds: Int) extends ManagedService {
  require(kafkaStreams != null)
  private val isRunning: AtomicBoolean = new AtomicBoolean(false)

  /**
    * This creates a managed KafkaStreams that waits for ever at
    * stop. To provide a specific timeout use the other constructor
    *
    * @param kafkaStreams underlying KafkaStreams instance that needs to be
    *                     managed
    */
  def this(kafkaStreams: KafkaStreams) = this(kafkaStreams, 0)

  /**
    * @see ManagedService.start
    */
  override def start(): Unit = {
    kafkaStreams.start()
    isRunning.set(true)
  }

  /**
    * @see ManagedService.stop
    */
  override def stop(): Unit = {
    if (isRunning.getAndSet(false)) {
      kafkaStreams.close(closeWaitInSeconds, TimeUnit.SECONDS)
    }
  }

  /**
    * @see ManagedService.hasStarted
    * @return
    */
  override def hasStarted: Boolean = isRunning.get()
}
