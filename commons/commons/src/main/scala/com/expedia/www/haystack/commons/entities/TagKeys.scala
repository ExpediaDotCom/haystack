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
The Tag keys are according to metrics 2.0 specifications see http://metrics20.org/spec/#tag-keys
 */
object TagKeys {
  /**
    * OPERATION_NAME_KEY is a identifier for the operation name specified in haystack.
    */
  val OPERATION_NAME_KEY = "operationName"
  /**
    * SERVICE_NAME_KEY is a identifier for the service name specified in haystack.
    */
  val SERVICE_NAME_KEY = "serviceName"
  /**
    * RESULT_KEY is a identifier for the values: such as ok, fail
    */
  val RESULT_KEY = "result"
  /**
    * STATS_KEY is a identifier to clarify the statistical view
    */
  val STATS_KEY = "stat"
  /**
    * ERROR_KEY is a identifier to specify whether a span is a success or failure. Useful in trending for success or
    * failure count
    */
  val ERROR_KEY = "error"
  /**
    * INTERVAL_KEY is a identifier to specify whether the interval of a trend. For eg: OneMinute, FiveMinute etc
    */
  val INTERVAL_KEY = "interval"
  /**
    * ORG_ID_KEY is a identifier to specify the organization sending the span/trend.
    */
  val ORG_ID_KEY = "orgId"
  /**
    * PRODUCT_KEY is a identifier to specify the namespace of the trend.
    */
  val PRODUCT_KEY = "product"
}
