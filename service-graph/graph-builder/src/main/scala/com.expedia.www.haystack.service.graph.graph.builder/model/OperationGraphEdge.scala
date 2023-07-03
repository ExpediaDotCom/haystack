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
package com.expedia.www.haystack.service.graph.graph.builder.model

import org.apache.commons.lang3.StringUtils

/**
  * A graph edge representing relationship between two services over an operation
  * @param source source service
  * @param destination destination service
  * @param stats stats around the edge
  * @param effectiveFrom start timestamp from which stats are collected
  * @param effectiveTo end timestamp till which stats are collected
  */
case class OperationGraphEdge(source: String,
                              destination: String,
                              operation: String,
                              stats: EdgeStats,
                              effectiveFrom: Long,
                              effectiveTo: Long)  {
  require(StringUtils.isNotEmpty(source))
  require(StringUtils.isNotEmpty(destination))
  require(StringUtils.isNotEmpty(operation))
  require(stats != null)
}
