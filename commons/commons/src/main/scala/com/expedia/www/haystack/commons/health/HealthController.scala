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

package com.expedia.www.haystack.commons.health

import java.util.concurrent.atomic.AtomicReference

import com.expedia.www.haystack.commons.health.HealthStatus.HealthStatus
import org.slf4j.LoggerFactory

import scala.collection.mutable

object HealthStatus extends Enumeration {
  type HealthStatus = Value
  val HEALTHY, UNHEALTHY, NOT_SET = Value
}

/**
  * provides the health check of app
  */
class HealthStatusController {
  private val LOGGER = LoggerFactory.getLogger(classOf[HealthStatusController])
  private val status = new AtomicReference[HealthStatus](HealthStatus.NOT_SET)
  private var listeners = mutable.ListBuffer[HealthStatusChangeListener]()

  def setHealthy(): Unit = {
    LOGGER.info("Setting the app status as 'HEALTHY'")
    if(status.getAndSet(HealthStatus.HEALTHY) != HealthStatus.HEALTHY) notifyChange(HealthStatus.HEALTHY)
  }

  def setUnhealthy(): Unit = {
    LOGGER.error("Setting the app status as 'UNHEALTHY'")
    if(status.getAndSet(HealthStatus.UNHEALTHY) != HealthStatus.UNHEALTHY) notifyChange(HealthStatus.UNHEALTHY)
  }

  def isHealthy: Boolean = status.get() == HealthStatus.HEALTHY

  def addListener(l: HealthStatusChangeListener): Unit = listeners += l

  private def notifyChange(status: HealthStatus): Unit = {
    listeners foreach {
      l =>
        l.onChange(status)
    }
  }
}

object HealthController {
  private val healthController = new HealthStatusController

  /**
    * set the app status as health
    */
  def setHealthy(): Unit = {
    healthController.setHealthy()
  }

  /**
    * set the app status as unhealthy
    */
  def setUnhealthy(): Unit = {
    healthController.setUnhealthy()
  }

  /**
    * @return true if app is healthy else false
    */
  def isHealthy: Boolean = healthController.isHealthy

  /**
    * add health change listener that will be called on any change in the health status
    * @param l listener
    */
  def addListener(l: HealthStatusChangeListener): Unit = healthController.addListener(l)  
}
