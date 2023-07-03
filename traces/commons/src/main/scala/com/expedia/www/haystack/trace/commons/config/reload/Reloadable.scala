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

/**
  * An entity(for e.g. a configuration object) that extends reloadable trait allows it to reload the config object
  * dynamically. The config reloader reads the new configuration periodically from an external store and
  * calls 'onReload()' method.
  * The 'name' provides the tableName where the configuration is stored for this entity.
  */
trait Reloadable {
  def name: String

  def onReload(newConfig: String): Unit
}
