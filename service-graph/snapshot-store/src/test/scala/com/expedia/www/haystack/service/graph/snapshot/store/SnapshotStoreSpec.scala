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
package com.expedia.www.haystack.service.graph.snapshot.store
import java.time.Instant

class SnapshotStoreSpec extends SnapshotStoreSpecBase {
  private val snapshotStore = new SnapshotStore {
    override def write(instant: Instant, content: String): AnyRef = {
      None
    }

    override def read(instant: Instant): Option[String] = {
      None
    }

    override def purge(instant: Instant): Integer = {
      0
    }

    override def build(constructorArguments: Array[String]): SnapshotStore = {
      this
    }
  }

  describe("SnapshotStore") {
    it("should create the correct ISO 8601 file name") {
      snapshotStore.createIso8601FileName(Instant.EPOCH) shouldEqual "1970-01-01T00:00:00.000Z"
      snapshotStore.createIso8601FileName(Instant.EPOCH.plusMillis(1)) shouldEqual "1970-01-01T00:00:00.001Z"
      snapshotStore.createIso8601FileName(Instant.EPOCH.plusMillis(-1)) shouldEqual "1969-12-31T23:59:59.999Z"
    }
  }

}
