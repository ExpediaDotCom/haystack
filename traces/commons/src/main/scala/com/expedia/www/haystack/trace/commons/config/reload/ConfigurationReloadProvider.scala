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

package com.expedia.www.haystack.trace.commons.config.reload

import java.util.concurrent.{Executors, TimeUnit}

import com.expedia.www.haystack.trace.commons.config.entities.ReloadConfiguration
import org.slf4j.{Logger, LoggerFactory}

abstract class ConfigurationReloadProvider(config: ReloadConfiguration) extends AutoCloseable {
  protected val LOGGER: Logger = LoggerFactory.getLogger(classOf[ConfigurationReloadProvider])

  private val executor = Executors.newSingleThreadScheduledExecutor()

  // schedule the reload process from an external store
  if(config.reloadIntervalInMillis > -1) {
    LOGGER.info("configuration reload scheduler has been started with a delay of {}ms", config.reloadIntervalInMillis)
    executor.scheduleWithFixedDelay(() => {
      load()
    }, config.reloadIntervalInMillis, config.reloadIntervalInMillis, TimeUnit.MILLISECONDS)
  }

  def load(): Unit

  def close(): Unit = executor.shutdownNow()
}
