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

package com.expedia.www.haystack.trace.commons.unit

import com.expedia.www.haystack.trace.commons.config.entities.{IndexFieldType, WhiteListIndexFields, WhitelistIndexField, WhitelistIndexFieldConfiguration}
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.{Entry, FunSpec, Matchers}

import scala.collection.JavaConverters._

class WhitelistIndexFieldConfigurationSpec extends FunSpec with Matchers {

  protected implicit val formats: Formats = DefaultFormats + new EnumNameSerializer(IndexFieldType)

  describe("whitelist field configuration") {
    it("an empty configuration should return whitelist fields as empty") {
      val config = WhitelistIndexFieldConfiguration()
      config.indexFieldMap shouldBe 'empty
      config.whitelistIndexFields shouldBe 'empty
    }

    it("a loaded configuration should return the non empty whitelist fields") {
      val whitelistField_1 = WhitelistIndexField(name = "role", `type` = IndexFieldType.string, enableRangeQuery = true)
      val whitelistField_2 = WhitelistIndexField(name = "Errorcode", `type` = IndexFieldType.long)

      val config = WhitelistIndexFieldConfiguration()
      val cfgJsonData = Serialization.write(WhiteListIndexFields(List(whitelistField_1, whitelistField_2)))

      // reload
      config.onReload(cfgJsonData)

      config.whitelistIndexFields.map(_.name) should contain allOf("role", "errorcode")
      config.whitelistIndexFields.filter(r => r.name == "role").head.enableRangeQuery shouldBe true
      config.indexFieldMap.size() shouldBe 2
      config.indexFieldMap.keys().asScala.toList should contain allOf("role", "errorcode")
      config.globalTraceContextIndexFieldNames.size shouldBe 0

      val whitelistField_3 = WhitelistIndexField(name = "status", `type` = IndexFieldType.string, aliases = Set("_status", "HTTP-STATUS"))
      val whitelistField_4 = WhitelistIndexField(name = "something", `type` = IndexFieldType.long, searchContext = "trace")

      val newCfgJsonData = Serialization.write(WhiteListIndexFields(List(whitelistField_1, whitelistField_3, whitelistField_4)))
      config.onReload(newCfgJsonData)

      config.whitelistIndexFields.size shouldBe 5
      config.whitelistIndexFields.map(_.name).toSet should contain allOf("status", "something", "role")
      config.indexFieldMap.size shouldBe 5
      config.indexFieldMap.keys().asScala.toList should contain allOf("status", "something", "role", "http-status", "_status")

      config.onReload(newCfgJsonData)
      config.whitelistIndexFields.size shouldBe 5
      config.whitelistIndexFields.map(_.name).toSet should contain allOf("status", "something", "role")
      config.indexFieldMap.size() shouldBe 5
      config.indexFieldMap.keys().asScala.toList should contain allOf("status", "something", "role", "http-status", "_status")

      config.indexFieldMap.get("http-status").name shouldEqual "status"
      config.indexFieldMap.get("_status").name shouldEqual "status"

      config.globalTraceContextIndexFieldNames.size shouldBe 1
      config.globalTraceContextIndexFieldNames.head shouldEqual "something"
    }
  }
}
