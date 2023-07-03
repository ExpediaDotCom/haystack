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
package com.expedia.www.haystack.service.graph.graph.builder.service

import org.mockito.Mockito.{verify, verifyNoMoreInteractions}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}

class ManagedHttpServiceSpec extends FunSpec with Matchers with MockitoSugar {
  describe("ManagedHttpService constructor") {
    it ("should require the service argument to be non-null") {
      an [IllegalArgumentException] should be thrownBy new ManagedHttpService(null)
    }
  }

  describe("ManagedHttpService.start()") {
    val httpService = mock[HttpService]
    val managedHttpService = new ManagedHttpService(httpService)
    it("should call the service's start() method and set isRunning to true") {
      managedHttpService.start()
      verify(httpService).start()
      assert(managedHttpService.hasStarted)
    }
    verifyNoMoreInteractions(httpService)
  }

  describe("ManagedHttpService.stop()") {
    val httpService = mock[HttpService]
    val managedHttpService = new ManagedHttpService(httpService)
    it("should not call the service's stop() method if the service is not running") {
      managedHttpService.stop()
      verifyNoMoreInteractions(httpService)
    }
    it("should call the service's close() method and set isRunning to false if the service is running") {
      managedHttpService.start()
      managedHttpService.stop()
      verify(httpService).start()
      verify(httpService).close()
      assert(!managedHttpService.hasStarted)
      verifyNoMoreInteractions(httpService)
    }
  }
}
