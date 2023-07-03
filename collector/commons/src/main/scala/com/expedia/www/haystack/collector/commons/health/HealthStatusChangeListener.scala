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

import com.expedia.www.haystack.collector.commons.health.HealthStatus.HealthStatus

/**
  * health status listener
  */
trait HealthStatusChangeListener {

  /**
    * called whenever there there is a state change in health
    * @param status current health status
    */
  def onChange(status: HealthStatus): Unit
}
