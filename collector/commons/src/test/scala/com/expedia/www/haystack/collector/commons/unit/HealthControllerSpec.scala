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

package com.expedia.www.haystack.collector.commons.unit

import com.expedia.www.haystack.collector.commons.health.{HealthController, UpdateHealthStatusFile}
import org.scalatest.{FunSpec, Matchers}

class HealthControllerSpec extends FunSpec with Matchers {

  private val statusFile = "/tmp/app-health.status"

  describe("file based health checker") {
    it("should set the state as healthy if previous state is not set or unhealthy") {
      val healthChecker = HealthController
      healthChecker.addListener(new UpdateHealthStatusFile(statusFile))
      healthChecker.isHealthy shouldBe false
      healthChecker.setHealthy()
      healthChecker.isHealthy shouldBe true
      readStatusLine shouldEqual "true"
    }

    it("should set the state as unhealthy if previous state is healthy") {
      val healthChecker = HealthController
      healthChecker.addListener(new UpdateHealthStatusFile(statusFile))

      healthChecker.setHealthy()
      healthChecker.isHealthy shouldBe true
      readStatusLine shouldEqual "true"

      healthChecker.setUnhealthy()
      healthChecker.isHealthy shouldBe false
      readStatusLine shouldEqual "false"
    }
  }

  private def readStatusLine = scala.io.Source.fromFile(statusFile).getLines().toList.head
}