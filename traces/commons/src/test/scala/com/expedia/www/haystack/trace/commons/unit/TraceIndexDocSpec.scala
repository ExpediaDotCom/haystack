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

import com.expedia.www.haystack.trace.commons.clients.es.document.TraceIndexDoc
import org.scalatest.{FunSpec, Matchers}

import scala.collection.mutable

class TraceIndexDocSpec extends FunSpec with Matchers {
  describe("TraceIndex Document") {
    it("should produce the valid json document for indexing") {
      val startTime = 1528715319040L
      val spanDoc = mutable.Map("spanid" -> "SPAN-1", "operatioName" -> "op1", "serviceName" -> "svc", "duration" -> 100, "starttime" -> startTime)
      val indexDoc = TraceIndexDoc("trace-id", 100L, startTime, Seq(spanDoc))
      indexDoc.json shouldEqual "{\"traceid\":\"trace-id\",\"rootduration\":100,\"starttime\":1528715319040,\"spans\":[{\"spanid\":\"SPAN-1\",\"serviceName\":\"svc\",\"starttime\":1528715319040,\"operatioName\":\"op1\",\"duration\":100}]}"
    }
  }
}
