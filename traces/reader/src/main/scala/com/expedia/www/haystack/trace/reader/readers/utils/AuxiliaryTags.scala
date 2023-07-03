/*
 *  Copyright 2017 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */

package com.expedia.www.haystack.trace.reader.readers.utils

object AuxiliaryTags {
  val INFRASTRUCTURE_LOCATION = "X-HAYSTACK-AWS-REGION"
  val INFRASTRUCTURE_PROVIDER = "X-HAYSTACK-INFRASTRUCTURE-PROVIDER"
  
  val IS_MERGED_SPAN = "X-HAYSTACK-IS-MERGED-SPAN"
  val NETWORK_DELTA = "X-HAYSTACK-NETWORK-DELTA"

  val CLIENT_SERVICE_NAME = "X-HAYSTACK-CLIENT-SERVICE-NAME"
  val CLIENT_OPERATION_NAME = "X-HAYSTACK-CLIENT-OPERATION-NAME"
  val CLIENT_SPAN_ID = "X-HAYSTACK-CLIENT-SPAN-ID"
  val CLIENT_INFRASTRUCTURE_PROVIDER = "X-HAYSTACK-CLIENT-INFRASTRUCTURE-PROVIDER"
  val CLIENT_INFRASTRUCTURE_LOCATION = "X-HAYSTACK-CLIENT-INFRASTRUCTURE-LOCATION"
  val CLIENT_START_TIME = "X-HAYSTACK-CLIENT-START-TIME"
  val CLIENT_DURATION = "X-HAYSTACK-CLIENT-DURATION"
  
  val SERVER_SERVICE_NAME = "X-HAYSTACK-SERVER-SERVICE-NAME"
  val SERVER_OPERATION_NAME = "X-HAYSTACK-SERVER-OPERATION-NAME"
  val SERVER_INFRASTRUCTURE_PROVIDER = "X-HAYSTACK-SERVER-INFRASTRUCTURE-PROVIDER"
  val SERVER_INFRASTRUCTURE_LOCATION = "X-HAYSTACK-SERVER-INFRASTRUCTURE-LOCATION"
  val SERVER_START_TIME = "X-HAYSTACK-SERVER-START-TIME"
  val SERVER_DURATION = "X-HAYSTACK-SERVER-DURATION"

  val ERR_IS_MULTI_PARTIAL_SPAN = "X-HAYSTACK-ERR-IS-MULTI-PARTIAL-SPAN"
}
