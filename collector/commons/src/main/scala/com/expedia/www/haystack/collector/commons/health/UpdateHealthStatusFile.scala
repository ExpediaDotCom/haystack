/*
 *
 *     Copyright 2017 Expedia, Inc.
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

package com.expedia.www.haystack.collector.commons.health

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import com.expedia.www.haystack.collector.commons.health.HealthStatus.HealthStatus

/**
  * writes the current health status to a status file. This can be used to provide the health to external system
  * like container orchestration frameworks
  * @param statusFilePath: file path where health status will be recorded.
  */
class UpdateHealthStatusFile(statusFilePath: String) extends HealthStatusChangeListener {

  /**
    * call on the any change in health status of app
    * @param status: current health status
    */
  override def onChange(status: HealthStatus): Unit = {
    val isHealthy = if (status == HealthStatus.HEALTHY) "true" else "false"
    Files.write(Paths.get(statusFilePath), isHealthy.getBytes(StandardCharsets.UTF_8))
  }
}
