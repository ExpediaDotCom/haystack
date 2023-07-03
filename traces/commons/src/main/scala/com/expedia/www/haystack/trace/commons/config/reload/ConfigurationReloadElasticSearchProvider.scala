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

package com.expedia.www.haystack.trace.commons.config.reload

import com.expedia.www.haystack.commons.retries.RetryOperation
import com.expedia.www.haystack.trace.commons.clients.es.AWSSigningJestClientFactory
import com.expedia.www.haystack.trace.commons.config.entities.{AWSRequestSigningConfiguration, ReloadConfiguration}
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.core.Search

import scala.util.{Failure, Success}

class ConfigurationReloadElasticSearchProvider(reloadConfig: ReloadConfiguration, awsRequestSigningConfig: AWSRequestSigningConfiguration)
  extends ConfigurationReloadProvider(reloadConfig) {

  private val matchAllQuery = "{\"query\":{\"match_all\":{\"boost\":1.0}}}"

  private val esClient: JestClient = {
    val factory = {
      if (awsRequestSigningConfig.enabled) {
        LOGGER.info("using AWSSigningJestClientFactory for es client")
        new AWSSigningJestClientFactory(awsRequestSigningConfig)
      } else {
        LOGGER.info("using JestClientFactory for es client")
        new JestClientFactory()
      }
    }

    val builder = new HttpClientConfig.Builder(reloadConfig.configStoreEndpoint).multiThreaded(false)

    if (reloadConfig.username.isDefined && reloadConfig.password.isDefined) {
      builder.defaultCredentials(reloadConfig.username.get, reloadConfig.password.get)
    }

    factory.setHttpClientConfig(builder.build())
    factory.getObject
  }

  /**
    * loads the configuration from external store
    */
  override def load(): Unit = {
    reloadConfig.observers.foreach(observer => {

      val searchQuery = new Search.Builder(matchAllQuery)
        .addIndex(reloadConfig.databaseName)
        .addType(observer.name)
        .build()

      RetryOperation.executeWithRetryBackoff(() => esClient.execute(searchQuery), RetryOperation.Config(3, 1000, 2)) match {
        case Success(result) =>
          if (result.isSucceeded) {
            LOGGER.info(s"Reloading(or loading) is successfully done for the configuration name =${observer.name}")
            observer.onReload(result.getSourceAsString)
          } else {
            LOGGER.error(s"Fail to reload the configuration from elastic search with error: ${result.getErrorMessage} " +
              s"for observer name=${observer.name}")
          }

        case Failure(reason) =>
          LOGGER.error(s"Fail to reload the configuration from elastic search for observer name=${observer.name}. " +
            s"Will try at next scheduled time", reason)
      }
    })
  }

  override def close(): Unit = {
    this.esClient.shutdownClient()
    super.close()
  }
}
