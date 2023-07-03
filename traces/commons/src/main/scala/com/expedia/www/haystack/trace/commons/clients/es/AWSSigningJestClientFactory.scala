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

package com.expedia.www.haystack.trace.commons.clients.es

import java.time.{LocalDateTime, ZoneId}

import com.expedia.www.haystack.trace.commons.config.entities.AWSRequestSigningConfiguration
import com.google.common.base.Supplier
import io.searchbox.client.JestClientFactory
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.slf4j.LoggerFactory
import vc.inreach.aws.request.{AWSSigner, AWSSigningRequestInterceptor}
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.internal.StaticCredentialsProvider


/**
  * wrapper for JestClientFactory. Provides support for AWS ES request signing by adding an interceptor to the client.
  *
  * @param awsRequestSigningConfig config required for request signing like creds, region
  */
class AWSSigningJestClientFactory(awsRequestSigningConfig: AWSRequestSigningConfiguration) extends JestClientFactory {
  private val LOGGER = LoggerFactory.getLogger(classOf[AWSSigningJestClientFactory])

  val awsSigner = new AWSSigner(getCredentialProvider, awsRequestSigningConfig.region, awsRequestSigningConfig.awsServiceName, new ClockSupplier)
  val requestInterceptor = new AWSSigningRequestInterceptor(awsSigner)

  override def configureHttpClient(builder: HttpClientBuilder): HttpClientBuilder = {
    builder.addInterceptorLast(requestInterceptor)
  }

  override def configureHttpClient(builder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
    builder.addInterceptorLast(requestInterceptor)
  }

  def getCredentialProvider: AWSCredentialsProvider = {
    if (awsRequestSigningConfig.accessKey.isDefined) {
      LOGGER.info("using static aws credential provider with access and secret key for ES")
      new StaticCredentialsProvider(
        new BasicAWSCredentials(awsRequestSigningConfig.accessKey.get, awsRequestSigningConfig.secretKey.get))
    } else {
      LOGGER.info("using default credential provider chain for ES")
      new DefaultAWSCredentialsProviderChain
    }
  }
}

class ClockSupplier extends Supplier[LocalDateTime] {
  override def get(): LocalDateTime = {
    LocalDateTime.now(ZoneId.of("UTC"))
  }
}
