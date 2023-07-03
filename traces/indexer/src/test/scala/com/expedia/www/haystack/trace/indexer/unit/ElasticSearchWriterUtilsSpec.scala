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
package com.expedia.www.haystack.trace.indexer.unit

import com.expedia.www.haystack.trace.indexer.writers.es.ElasticSearchWriterUtils
import org.scalatest.{BeforeAndAfterEach, FunSpec, GivenWhenThen, Matchers}

class ElasticSearchWriterUtilsSpec extends FunSpec with Matchers with GivenWhenThen with BeforeAndAfterEach {
  var timezone: String = _

  override def beforeEach() {
    timezone = System.getProperty("user.timezone")
    System.setProperty("user.timezone", "CST")
  }

  override def afterEach(): Unit = {
    System.setProperty("user.timezone", timezone)
  }

  describe("elastic search writer") {
    it("should use UTC when generating ES indexes") {
      Given("the system timezone is not UTC")
      System.setProperty("user.timezone", "CST")
      val eventTimeInMicros = System.currentTimeMillis() * 1000

      When("the writer generates the ES indexes")
      val cstName = ElasticSearchWriterUtils.indexName("haystack-traces", 6, eventTimeInMicros)
      System.setProperty("user.timezone", "UTC")
      val utcName = ElasticSearchWriterUtils.indexName("haystack-traces", 6, eventTimeInMicros)

      Then("it should use UTC to get those indexes")
      cstName shouldBe utcName
    }
  }
}
