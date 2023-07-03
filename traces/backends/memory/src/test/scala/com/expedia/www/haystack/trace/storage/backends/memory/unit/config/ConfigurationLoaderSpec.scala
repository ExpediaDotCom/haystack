/*
 *  Copyright 2017 Expedia, Inc.
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
package com.expedia.www.haystack.trace.storage.backends.memory.unit.config

import com.expedia.www.haystack.trace.storage.backends.memory.config.ProjectConfiguration
import com.expedia.www.haystack.trace.storage.backends.memory.config.entities.ServiceConfiguration
import com.expedia.www.haystack.trace.storage.backends.memory.unit.BaseUnitTestSpec

class ConfigurationLoaderSpec extends BaseUnitTestSpec {
  describe("ConfigurationLoader") {
    val project = new ProjectConfiguration()
    it("should load the service config from base.conf") {
      val serviceConfig: ServiceConfiguration = project.serviceConfig
      serviceConfig.port shouldBe 8090
      serviceConfig.ssl.enabled shouldBe false
      serviceConfig.ssl.certChainFilePath shouldBe "/ssl/cert"
      serviceConfig.ssl.privateKeyPath shouldBe "/ssl/private-key"
    }
  }
}
