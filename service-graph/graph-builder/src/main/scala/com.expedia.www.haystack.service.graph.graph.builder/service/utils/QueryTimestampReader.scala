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
package com.expedia.www.haystack.service.graph.graph.builder.service.utils

import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.servlet.http.HttpServletRequest

import org.apache.commons.lang3.StringUtils

class QueryTimestampReader(aggregateWindowSec: Long) {

  def toTimestamp(request: HttpServletRequest): Long = {
    if (StringUtils.isEmpty(request.getParameter("to"))) {
      Instant.now().toEpochMilli
    } else {
      extractTime(request, "to")
    }
  }

  def fromTimestamp(request: HttpServletRequest): Long = {
    val timestamp = if (StringUtils.isEmpty(request.getParameter("from"))) {
      Instant.now().minus(24, ChronoUnit.HOURS).toEpochMilli
    } else {
      extractTime(request, "from")
    }
    adjustTimeWithAggregateWindow(timestamp)

  }

  private def extractTime(request: HttpServletRequest, key: String): Long = {
    request.getParameter(key).toLong
  }

  private def adjustTimeWithAggregateWindow(epochMillis: Long): Long = {
    val result = Math.floor(epochMillis / (aggregateWindowSec * 1000)).toLong
    result * aggregateWindowSec * 1000
  }
}
