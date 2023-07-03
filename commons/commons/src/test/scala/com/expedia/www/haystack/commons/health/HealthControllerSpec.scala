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

package com.expedia.www.haystack.commons.health

import com.expedia.www.haystack.commons.unit.UnitTestSpec

class HealthControllerSpec extends UnitTestSpec {
  val statusFile = "/tmp/app-health.status"


  "file based health checker" should {

    "set the value with the correct boolean value for the app's health status" in {
      Given("a file path")

      When("checked with default state")
      val healthChecker = HealthController
      healthChecker.addListener(new UpdateHealthStatusFile(statusFile))
      val status = healthChecker.isHealthy

      Then("default state should be unhealthy")
      status shouldBe false

      When("explicitly set as healthy")
      healthChecker.setHealthy()

      Then("The state should be updated to healthy")
      healthChecker.isHealthy shouldBe true
      readStatusLine shouldEqual "true"

      When("explicitly set as unhealthy")
      healthChecker.setUnhealthy()

      Then("The state should be updated to unhealthy")
      healthChecker.isHealthy shouldBe false
      readStatusLine shouldBe "false"
    }
  }

  private def readStatusLine = scala.io.Source.fromFile(statusFile).getLines().toList.head
}
