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
package com.expedia.www.haystack.service.graph.graph.builder.service.fetchers

import java.util.concurrent.Executors

import com.expedia.www.haystack.service.graph.graph.builder.config.entities.ServiceClientConfiguration
import com.expedia.www.haystack.service.graph.graph.builder.model.{OperationGraph, OperationGraphEdge}
import org.apache.http.client.fluent.Request
import org.apache.http.client.utils.URIBuilder
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RemoteOperationEdgesFetcher(clientConfig: ServiceClientConfiguration) extends AutoCloseable {

  private val dispatcher = ExecutionContext.fromExecutorService(
    Executors.newFixedThreadPool(Math.min(Runtime.getRuntime.availableProcessors(), 2)))

  implicit val formats = DefaultFormats

  def fetchEdges(host: String, port: Int, from: Long, to: Long): Future[Seq[OperationGraphEdge]] = {
    val request = new URIBuilder()
      .setScheme("http")
      .setPath("/operationgraph/local")
      .setParameter("from", from.toString)
      .setParameter("to", to.toString)
      .setHost(host)
      .setPort(port)
      .build()

    Future {
      val response = Request.Get(request)
        .connectTimeout(clientConfig.connectionTimeout)
        .socketTimeout(clientConfig.socketTimeout)
        .execute()
        .returnContent()
        .asString()

      Serialization.read[OperationGraph](response).edges
    }(dispatcher)
  }

  override def close(): Unit = {
    Try(this.dispatcher.shutdown())
  }
}
