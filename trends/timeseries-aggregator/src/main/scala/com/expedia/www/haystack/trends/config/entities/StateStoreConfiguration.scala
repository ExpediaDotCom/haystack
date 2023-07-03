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
package com.expedia.www.haystack.trends.config.entities


/**
  * This class contains configurations specific to the kafka streams state store for keeping the trend metrics being computed
  *
  * @param stateStoreCacheSize         - max number of trends which can be computed by single state store (number * number of stream tasks * size of a trendMetric in memory should be < heap of the process)
  * @param enableChangeLogging         - enable/disable chanelogging - This helps in recreating the state in the app crashes or partition reassignment
  * @param changeLogDelayInSecs        - Interval at which the state should be check pointed at the changelog topic in kafka
  * @param changeLogTopicConfiguration - Configuration specific to kafka changelog topic - refer to
  */

case class StateStoreConfiguration(stateStoreCacheSize: Int, enableChangeLogging: Boolean, changeLogDelayInSecs: Int, changeLogTopicConfiguration: Map[String, String])
