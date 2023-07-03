/*
 *  Copyright 2019, Expedia Group.
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

package com.expedia.www.haystack.trace.reader.stores.readers.es

import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trace.commons.clients.es.AWSSigningJestClientFactory
import com.expedia.www.haystack.trace.commons.config.entities.AWSRequestSigningConfiguration
import com.expedia.www.haystack.trace.reader.config.entities.ElasticSearchClientConfiguration
import com.expedia.www.haystack.trace.reader.metrics.AppMetricNames
import com.expedia.www.haystack.trace.reader.stores.readers.es.ESUtils._
import com.google.gson.Gson
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.core.{Search, SearchResult}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.Try

class ElasticSearchReader(config: ElasticSearchClientConfiguration, awsRequestSigningConfig: AWSRequestSigningConfiguration)(implicit val dispatcher: ExecutionContextExecutor) extends MetricsSupport with AutoCloseable {
  private val LOGGER = LoggerFactory.getLogger(classOf[ElasticSearchReader])
  private val readTimer = metricRegistry.timer(AppMetricNames.ELASTIC_SEARCH_READ_TIME)
  private val readFailures = metricRegistry.meter(AppMetricNames.ELASTIC_SEARCH_READ_FAILURES)

  // initialize the elastic search client
  private val esClient: JestClient = {
    LOGGER.info("Initializing the http elastic search client with endpoint={}", config.endpoint)

    val factory = {
      if (awsRequestSigningConfig.enabled) {
        LOGGER.info("using AWSSigningJestClientFactory for es client")
        new AWSSigningJestClientFactory(awsRequestSigningConfig)
      } else {
        LOGGER.info("using JestClientFactory for es client")
        new JestClientFactory()
      }
    }

    val builder = new HttpClientConfig.Builder(config.endpoint)
      .multiThreaded(true)
      .connTimeout(config.connectionTimeoutMillis)
      .readTimeout(config.readTimeoutMillis)

    if (config.username.isDefined && config.password.isDefined) {
      builder.defaultCredentials(config.username.get, config.password.get)
    }

    factory.setHttpClientConfig(builder.build())
    factory.getObject
  }

  def search(request: Search): Future[SearchResult] = {
    val promise = Promise[SearchResult]()
    val time = readTimer.time()
    try {
      LOGGER.debug(s"elastic search query requested: ${request.toString}', query: '${request.toJson}'")
      esClient.executeAsync(request, new ElasticSearchReadResultListener(request, promise, time, readFailures))
      promise.future
    } catch {
      case ex: Exception =>
        readFailures.mark()
        time.stop()
        LOGGER.error(s"Failed to read from elasticsearch for request=${request.toJson} with exception", ex)
        Future.failed(ex)
    }
  }

  def count(request: Search): Future[SearchResult] = {
    val promise = Promise[SearchResult]()
    val time = readTimer.time()
    try {
      LOGGER.debug(s"elastic count query requested: ${request.toString}', query: '${request.toJson}'")
      esClient.executeAsync(request, new ElasticSearchCountResultListener(request, promise, time, readFailures))
      promise.future
    } catch {
      case ex: Exception =>
        readFailures.mark()
        time.stop()
        LOGGER.error(s"Failed to read from elasticsearch for request=${request.getData(new Gson())} with exception", ex)
        Future.failed(ex)
    }
  }

  override def close(): Unit = Try(esClient.shutdownClient())
}
