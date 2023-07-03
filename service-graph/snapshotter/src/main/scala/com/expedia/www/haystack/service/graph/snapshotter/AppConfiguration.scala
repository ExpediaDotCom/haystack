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
package com.expedia.www.haystack.service.graph.snapshotter

import com.expedia.www.haystack.commons.config.ConfigurationLoader
import org.apache.commons.lang3.StringUtils

/**
  * This class reads the configuration from the given resource name
  *
  * @param resourceName name of the resource file to load
  */
class AppConfiguration(resourceName: String) {

  require(StringUtils.isNotBlank(resourceName))

  private val config = ConfigurationLoader.loadConfigFileWithEnvOverrides(resourceName = this.resourceName)

  /**
    * Default constructor that loads configuration from the resource named "app.conf"
    */
  def this() = this("app.conf")

  val purgeAgeMs: Long = config.getLong("snapshotter.purge.age.ms")

  val windowSizeMs: Long = config.getLong("snapshotter.window.size.ms")
}
