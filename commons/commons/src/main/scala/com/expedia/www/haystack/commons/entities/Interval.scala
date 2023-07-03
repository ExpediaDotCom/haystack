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
package com.expedia.www.haystack.commons.entities

/**
  * This enum contains the metric intervals supported by the app
  */
object Interval extends Enumeration {
  type Interval = IntervalVal
  val ONE_MINUTE = IntervalVal("OneMinute", 60)
  val FIVE_MINUTE = IntervalVal("FiveMinute", 300)
  val FIFTEEN_MINUTE = IntervalVal("FifteenMinute", 900)
  val ONE_HOUR = IntervalVal("OneHour", 3600)

  def all: List[Interval] = {
    List(ONE_MINUTE, FIVE_MINUTE, FIFTEEN_MINUTE, ONE_HOUR)
  }

  def fromName(name: String): IntervalVal = {
    name match {
      case "OneMinute" => ONE_MINUTE
      case "FiveMinute" => FIVE_MINUTE
      case "FifteenMinute" => FIFTEEN_MINUTE
      case "OneHour" => ONE_HOUR
      case _ => ONE_MINUTE
    }
  }

  def fromVal(value: Long): IntervalVal = {
    value match {
      case 60 => ONE_MINUTE
      case 300 => FIVE_MINUTE
      case 900 => FIFTEEN_MINUTE
      case 3600 => ONE_HOUR
      case _ => ONE_MINUTE
    }
  }

  sealed case class IntervalVal(name: String, timeInSeconds: Int) extends Val(name) {
  }

}

