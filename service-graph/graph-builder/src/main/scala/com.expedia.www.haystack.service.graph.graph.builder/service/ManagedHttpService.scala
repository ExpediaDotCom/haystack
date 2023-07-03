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
package com.expedia.www.haystack.service.graph.graph.builder.service

import java.util.concurrent.atomic.AtomicBoolean

import com.expedia.www.haystack.commons.kstreams.app.ManagedService

class ManagedHttpService(service: HttpService) extends ManagedService {
  require(service != null)
  private val isRunning: AtomicBoolean = new AtomicBoolean(false)

  override def start(): Unit = {
    service.start()
    isRunning.set(true)
  }

  override def stop(): Unit = {
    if(isRunning.getAndSet(false)) {
      service.close()
    }
  }

  override def hasStarted: Boolean = isRunning.get()
}
