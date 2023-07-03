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
package com.expedia.www.haystack.service.graph.graph.builder.config.entities

/**
  * Threads configuration for servlet container
  * @param min minimum number of threads to use for running servlets
  * @param max maximum number of threads to use for running servlets
  * @param idleTimeout timeout for a thread
  */
case class ServiceThreadsConfiguration(min: Int, max: Int, idleTimeout: Int) {
  require(min > 0)
  require(max > min)
  require(idleTimeout > 0)
}
