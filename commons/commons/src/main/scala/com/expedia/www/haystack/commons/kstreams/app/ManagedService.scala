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

/**
  * A simple trait for managing a service
  */
trait ManagedService {
  /**
    * This method is called when the service needs to be started
    * <p>Any exception thrown by this method is propagated up the calling chain
    * <p>After a successful start, `hasStarted` should return true
    */
  def start()

  /**
    * This method is called when the service needs to be stopped.
    * <p>If the service has not been started, this method should do nothing or
    * should have no side effect
    * <p>After successfully stopping, `hasStarted` should return false
    */
  def stop()

  /**
    * Indicates the state of the service
    * @return
    */
  def hasStarted : Boolean
}
