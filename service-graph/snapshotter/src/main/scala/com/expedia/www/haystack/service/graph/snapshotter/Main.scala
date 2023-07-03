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
package com.expedia.www.haystack.service.graph.snapshotter

import java.time.{Clock, Instant}

import com.expedia.www.haystack.service.graph.snapshot.store.SnapshotStore
import org.slf4j.{Logger, LoggerFactory}
import scalaj.http.{Http, HttpRequest}

object Main {
  val ServiceGraphUrlRequiredMsg =
    "The first argument must specify the service graph URL"
  val StringStoreClassRequiredMsg =
    "The second argument must specify the fully qualified class name of a class that implements SnapshotStore"
  val UrlBaseRequiredMsg =
    "The third argument must specify the base of the service graph URL"
  val ServiceGraphUrlSuffix: String = "?from=%d"
  val appConfiguration = new AppConfiguration()

  var logger: Logger = LoggerFactory.getLogger(Main.getClass)
  var clock: Clock = Clock.systemUTC()
  var factory: Factory = new Factory

  /** Main method
    * @param args specifies the class to run and its parameters.
    * ==args(0)==
    * The first parameter is the fully qualified class name of the implementation of
    * [[com.expedia.www.haystack.service.graph.snapshot.store.SnapshotStore]] to run.
    * There are currently two implementations:
    *   - [[com.expedia.www.haystack.service.graph.snapshot.store.FileSnapshotStore]]
    *   - [[com.expedia.www.haystack.service.graph.snapshot.store.S3SnapshotStore]]
    * ==args(1+)==
    * The rest of the arguments are passed to the constructor of the class specified by args(0).
    * See the documentation in the build() method of the desired implementation for argument details.
    * ===Examples===
    * ====FileSnapshotStore====
    * To run FileSnapshotStore and use /var/snapshots for snapshot storage, the arguments would be:
    *   - com.expedia.www.haystack.service.graph.snapshot.store.FileSnapshotStore
    *   - /var/snapshots
    * ====S3SnapshotStore====
    * To run S3SnapshotStore and use the "Haystack" bucket with subfolder "snapshots" for snapshot storage, and a batch
    * size of 10,000 when calling the S3 "listObjectsV2" API, the arguments would be:
    *   - com.expedia.www.haystack.service.graph.snapshot.store.S3SnapshotStore
    *   - Haystack
    *   - snapshots
    *   - 10000
    */
  def main(args: Array[String]): Unit = {
    if (args.length == 0) {
      logger.error(ServiceGraphUrlRequiredMsg)
    } else if (args.length == 1) {
      logger.error(StringStoreClassRequiredMsg)
    } else if (args.length == 2) {
      logger.error(UrlBaseRequiredMsg)
    } else {
      val snapshotStore = instantiateSnapshotStore(args)
      val now = clock.instant()
      val json = getCurrentServiceGraph(args(0) + ServiceGraphUrlSuffix, now)
      storeServiceGraphInTheStringStore(snapshotStore, now, json)
      purgeOldSnapshots(snapshotStore, now)
    }
  }

  private def instantiateSnapshotStore(args: Array[String]): SnapshotStore = {
    def createStringStoreInstanceWithDefaultConstructor: SnapshotStore = {
      val fullyQualifiedClassName = args(1)
      val klass = Class.forName(fullyQualifiedClassName)
      val instanceBuiltByDefaultConstructor = klass.newInstance().asInstanceOf[SnapshotStore]
      instanceBuiltByDefaultConstructor
    }

    val snapshotStore = createStringStoreInstanceWithDefaultConstructor.build(args.drop(1))
    snapshotStore
  }

  private def getCurrentServiceGraph(url: String, instant: Instant) = {
    val request = factory.createHttpRequest(url, instant.toEpochMilli - appConfiguration.windowSizeMs)
    val httpResponse = request.asString
    httpResponse.body
  }

  private def storeServiceGraphInTheStringStore(snapshotStore: SnapshotStore,
                                                instant: Instant,
                                                json: String): AnyRef = {
    snapshotStore.write(instant, json)
  }

  private def purgeOldSnapshots(snapshotStore: SnapshotStore,
                                instant: Instant): Integer = {
    snapshotStore.purge(instant.minusMillis(appConfiguration.purgeAgeMs))
  }
}

class Factory {
  def createHttpRequest(url: String, windowSizeMs: Long): HttpRequest = {
    val urlWithParameter = url.format(windowSizeMs)
    Http(urlWithParameter)
  }
}
