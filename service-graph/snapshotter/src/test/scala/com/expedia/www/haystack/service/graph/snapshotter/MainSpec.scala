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

import java.io.File
import java.nio.file.{Files, Path}
import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit.HOURS

import com.amazonaws.services.s3.AmazonS3
import com.expedia.www.haystack.service.graph.snapshot.store.Constants.{DotCsv, SlashEdges, SlashNodes}
import com.expedia.www.haystack.service.graph.snapshot.store.S3SnapshotStore.createItemName
import com.expedia.www.haystack.service.graph.snapshot.store.{FileSnapshotStore, S3SnapshotStore, SnapshotStore}
import com.expedia.www.haystack.service.graph.snapshotter.Main.{ServiceGraphUrlRequiredMsg, StringStoreClassRequiredMsg, UrlBaseRequiredMsg}
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, verifyNoMoreInteractions, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import org.slf4j.Logger
import scalaj.http.{HttpRequest, HttpResponse}

import scala.io.{BufferedSource, Codec, Source}

class MainSpec extends FunSpec with Matchers with MockitoSugar with BeforeAndAfter with SnapshotStore {
  private var mockLogger: Logger = _
  private var realLogger: Logger = _

  private var mockFactory: Factory = _
  private var realFactory: Factory = _

  private var mockClock: Clock = _
  private var realClock: Clock = _

  private var mockAmazonS3: AmazonS3 = _
  private var realAmazonS3: AmazonS3 = _

  private val mockHttpRequest = mock[HttpRequest]

  private def readFile(fileName: String): String = {
    implicit val codec: Codec = Codec.UTF8
    lazy val bufferedSource: BufferedSource = Source.fromResource(fileName)
    val fileContents = bufferedSource.getLines.mkString("\n")
    bufferedSource.close()
    fileContents + "\n"
  }

  private val body = readFile("serviceGraph.json")
  private val edges = readFile("serviceGraph_edges.csv")
  private val nodes = readFile("serviceGraph_nodes.csv")
  private val httpResponse: HttpResponse[String] = new HttpResponse[String](body = body, code = 0, headers = Map())
  private val now = Instant.now()

  private var tempDirectory: Path = _

  before {
    saveReaObjectsThatWillBeReplacedWithMocks()
    createMocks()
    replaceRealObjectsWithMocks()

    tempDirectory = Files.createTempDirectory(this.getClass.getSimpleName)

    def saveReaObjectsThatWillBeReplacedWithMocks(): Unit = {
      realLogger = Main.logger
      realFactory = Main.factory
      realClock = Main.clock
      realAmazonS3 = S3SnapshotStore.amazonS3
    }

    def createMocks(): Unit = {
      mockLogger = mock[Logger]
      mockFactory = mock[Factory]
      mockClock = mock[Clock]
      mockAmazonS3 = mock[AmazonS3]
    }

    def replaceRealObjectsWithMocks(): Unit = {
      Main.logger = mockLogger
      Main.factory = mockFactory
      Main.clock = mockClock
      S3SnapshotStore.amazonS3 = mockAmazonS3
    }
  }

  after {
    restoreRealObjects()
    recursiveDelete(tempDirectory.toFile)
    verifyNoMoreInteractions(mockLogger)
    verifyNoMoreInteractions(mockFactory)
    verifyNoMoreInteractions(mockClock)
    verifyNoMoreInteractions(mockAmazonS3)

    def restoreRealObjects(): Unit = {
      Main.logger = realLogger
      Main.factory = realFactory
      Main.clock = realClock
    }

    def recursiveDelete(file: File) {
      if (file.isDirectory)
        Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(recursiveDelete)
      file.delete
    }
  }

  describe("Main.main() called with no arguments") {
    it("should log an error") {
      Main.main(Array())
      verify(mockLogger).error(ServiceGraphUrlRequiredMsg)
    }
  }

  describe("Main.main() called with one argument") {
    it("should log an error") {
      Main.main(Array(serviceGraphUrlBase))
      verify(mockLogger).error(StringStoreClassRequiredMsg)
    }
  }

  private val fullyQualifiedFileSnaphotStoreClassName = new FileSnapshotStore().getClass.getCanonicalName

  describe("Main.main() called with two arguments") {
    it("should log an error") {
      Main.main(Array(serviceGraphUrlBase, fullyQualifiedFileSnaphotStoreClassName))
      verify(mockLogger).error(UrlBaseRequiredMsg)
    }
  }

  val serviceGraphUrlBase: String = "http://apis/graph/servicegraph"
  val serviceGraphUrl: String = serviceGraphUrlBase + Main.ServiceGraphUrlSuffix

  describe("Main.main() called with FileSnapshotStore arguments") {
    it("should create a FileSnapshotStore, write to it, then call purge()") {
      def verifyDirectoryIsEmptyToProveThatPurgeWasCalled = {
        tempDirectory.toFile.listFiles().length shouldBe 0
      }

      when(mockFactory.createHttpRequest(any(), any())).thenReturn(mockHttpRequest)
      when(mockHttpRequest.asString).thenReturn(httpResponse)
      when(mockClock.instant()).thenReturn(now)

      Main.main(Array(serviceGraphUrlBase, fullyQualifiedFileSnaphotStoreClassName, tempDirectory.toString))

      verifyDirectoryIsEmptyToProveThatPurgeWasCalled
      verifiesForCallToServiceGraphUrl(1)
    }
  }

  describe("Main.main() called with all S3SnapshotStore arguments") {
    it("should create an S3SnapshotStore, write to it, then call purge()") {
      val bucketName = "haystack-snapshots"
      val folderName = "hourly-snapshots"
      val fileNameBase = createIso8601FileName(now)
      when(mockFactory.createHttpRequest(any(), any())).thenReturn(mockHttpRequest)
      when(mockHttpRequest.asString).thenReturn(httpResponse)
      when(mockClock.instant()).thenReturn(now)

      Main.main(Array(serviceGraphUrlBase, new S3SnapshotStore().getClass.getCanonicalName, bucketName, folderName, "1000"))

      verifiesForCallToServiceGraphUrl(2)
      verify(mockAmazonS3).doesBucketExistV2(bucketName)
      verify(mockAmazonS3).createBucket(bucketName)
      verify(mockAmazonS3).putObject(bucketName, createItemName(folderName, fileNameBase + SlashEdges + DotCsv), edges)
      verify(mockAmazonS3).putObject(bucketName, createItemName(folderName, fileNameBase + SlashNodes + DotCsv), nodes)
    }
  }

  private def verifiesForCallToServiceGraphUrl(wantedNumberOfInvocations: Int) = {
    verify(mockFactory).createHttpRequest(serviceGraphUrl, now.toEpochMilli - HOURS.toMillis(1))
    verify(mockHttpRequest, times(wantedNumberOfInvocations)).asString
    verify(mockClock).instant()
  }

  describe("Factory.createHttpRequest()") {
    it("should properly construct the URL") {
      val factory = new Factory
      val httpRequest = factory.createHttpRequest(serviceGraphUrl, now.toEpochMilli)
      val url = httpRequest.url
      url should startWith(serviceGraphUrlBase)
      url should endWith(Main.ServiceGraphUrlSuffix.format(now.toEpochMilli))
    }
  }

  //noinspection NotImplementedCode
  def build(constructorArguments: Array[String]): SnapshotStore = ???
  //noinspection NotImplementedCode
  def read(instant: java.time.Instant): Option[String] = ???
  //noinspection NotImplementedCode
  def write(instant: java.time.Instant,content: String): AnyRef = ???
}
