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

package com.expedia.www.haystack.service.graph.graph.builder.model

import com.expedia.www.haystack.service.graph.graph.builder.TestSpec

class ServiceGraphEdgeSpec extends TestSpec {

  describe("ServiceGraphEdge") {
    it("should merge Service graph objects accurately") {
      Given("valid ServiceGraphEdge objects")
      val serviceGraph1 = ServiceGraphEdge(
        ServiceGraphVertex("src", Map("X-HAYSTACK-INFRASTRUCTURE-PROVIDER" -> "aws")),
        ServiceGraphVertex("dest", Map("X-HAYSTACK-INFRASTRUCTURE-PROVIDER" -> "dc")),
        ServiceEdgeStats(10, 15000, 3), 0, 10000)

      val serviceGraph2 = ServiceGraphEdge(
        ServiceGraphVertex("src", Map("X-HAYSTACK-INFRASTRUCTURE-PROVIDER" -> "dc")),
        ServiceGraphVertex("dest", Map("X-HAYSTACK-INFRASTRUCTURE-PROVIDER" -> "dc")),
        ServiceEdgeStats(15, 16000, 5), 0, 10000)

      val serviceGraph3 = ServiceGraphEdge(
        ServiceGraphVertex("src", Map("X-HAYSTACK-INFRASTRUCTURE-PROVIDER" -> "aws")),
        ServiceGraphVertex("dest", Map("X-HAYSTACK-INFRASTRUCTURE-PROVIDER" -> "dc")),
        ServiceEdgeStats(20, 17000, 8), 0, 10000)

      When("Merging service graph objects")
      val serviceGraph4 = serviceGraph1 + serviceGraph2
      val serviceGraph5 = serviceGraph3 + serviceGraph4

      serviceGraph5.source.tags.get("X-HAYSTACK-INFRASTRUCTURE-PROVIDER").get should be ("aws,dc")
      serviceGraph5.destination.tags.get("X-HAYSTACK-INFRASTRUCTURE-PROVIDER").get should be ("dc")
      serviceGraph5.stats.count should be (45)
      serviceGraph5.stats.errorCount should be (16)
      serviceGraph5.stats.lastSeen should be (17000)
    }

  }
}
