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

import com.expedia.www.haystack.service.graph.graph.builder.model.ServiceGraph
import com.expedia.www.haystack.service.graph.graph.builder.service.fetchers.LocalServiceEdgesFetcher
import com.expedia.www.haystack.service.graph.graph.builder.service.utils.QueryTimestampReader

class LocalServiceGraphResource(localEdgesFetcher: LocalServiceEdgesFetcher)(implicit val timestampReader: QueryTimestampReader)
  extends Resource("servicegraph.local") {

  private val edgeCount = metricRegistry.histogram("servicegraph.local.edges")

  protected override def get(request: HttpServletRequest): ServiceGraph = {
    val from = timestampReader.fromTimestamp(request)
    val to = timestampReader.toTimestamp(request)

    val localGraph = ServiceGraph(localEdgesFetcher.fetchEdges(from, to))
    edgeCount.update(localGraph.edges.length)
    localGraph
  }
}
