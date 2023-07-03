/*
 *  Copyright 2019, Expedia Group.
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

/**
  * defines the configuration parameters for AWS request signing
 *
  * @param enabled: signing will be performed if this flag is enabled
  * @param region: aws region
  * @param awsServiceName: aws service name for which signing needs to be done
  * @param accessKey: aws access key. If not present DefaultAWSCredentialsProviderChain is used
  * @param secretKey: aws secret key
  */
case class AWSRequestSigningConfiguration (enabled: Boolean,
                                           region: String,
                                           awsServiceName: String,
                                           accessKey: Option[String],
                                           secretKey: Option[String])
