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
package com.expedia.www.haystack.service.graph.graph.builder.service.resources

import javax.servlet.http.HttpServletRequest

import com.expedia.www.haystack.service.graph.graph.builder.config.entities.ServiceConfiguration
import com.expedia.www.haystack.service.graph.graph.builder.model.{ServiceGraph, ServiceGraphEdge}
import com.expedia.www.haystack.service.graph.graph.builder.service.fetchers.{LocalServiceEdgesFetcher, RemoteServiceEdgesFetcher}
import com.expedia.www.haystack.service.graph.graph.builder.service.utils.EdgesMerger._
import com.expedia.www.haystack.service.graph.graph.builder.service.utils.QueryTimestampReader
import org.apache.kafka.streams.KafkaStreams
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class GlobalServiceGraphResource(streams: KafkaStreams,
                                 storeName: String,
                                 serviceConfig: ServiceConfiguration,
                                 localEdgesFetcher: LocalServiceEdgesFetcher,
                                 remoteEdgesFetcher: RemoteServiceEdgesFetcher) (implicit val timestampReader: QueryTimestampReader)
  extends Resource("servicegraph") {
  private val LOGGER = LoggerFactory.getLogger(classOf[LocalServiceGraphResource])
  private val globalEdgeCount = metricRegistry.histogram("servicegraph.global.edges")

  protected override def get(request: HttpServletRequest): ServiceGraph = {
    val from = timestampReader.fromTimestamp(request)
    val to = timestampReader.toTimestamp(request)

    // get list of all hosts containing service-graph store
    // fetch local service graphs from all hosts
    // and merge local graphs to create global graph
    val edgesListFuture: Iterable[Future[Seq[ServiceGraphEdge]]] = streams
      .allMetadataForStore(storeName)
      .asScala
      .map(host => {
        if (host.host() == serviceConfig.host) {
          LOGGER.info(s"service graph from local invoked")
          Future(localEdgesFetcher.fetchEdges(from, to))
        }
        else {
          LOGGER.info(s"service graph from ${host.host()} for edges is invoked")
          remoteEdgesFetcher.fetchEdges(host.host(), host.port(), from, to)
        }
      })

    val singleResultFuture = Future.sequence(edgesListFuture)
    val edges = Await
      .result(singleResultFuture, serviceConfig.client.socketTimeout.millis)
      .foldLeft(mutable.ListBuffer[ServiceGraphEdge]())((buffer, coll) => buffer ++= coll)

    val mergedEdgeList = getMergedServiceEdges(edges)
    globalEdgeCount.update(mergedEdgeList.length)
    ServiceGraph(mergedEdgeList)
  }
}
