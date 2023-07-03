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

package com.expedia.www.haystack.trace.commons.config.entities

import com.expedia.www.haystack.trace.commons.config.reload.Reloadable

/**
  * defines the configuration parameters for reloading the app configs from external store like ElasticSearch
 *
  * @param configStoreEndpoint: endpoint for external store where app configuration is stored
  * @param databaseName: name of the database
  * @param reloadIntervalInMillis: app config will be refreshed after this given interval in millis
  * @param observers: list of reloadable configuration objects that subscribe to the reloader
  * @param loadOnStartup: loads the app configuration from external store on startup, default is true
  */
case class ReloadConfiguration(configStoreEndpoint: String,
                               databaseName: String,
                               reloadIntervalInMillis: Int,
                               username: Option[String],
                               password: Option[String],
                               observers: Seq[Reloadable],
                               loadOnStartup: Boolean = true)
