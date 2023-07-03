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
import java.time.format.DateTimeFormatter.ISO_INSTANT
import java.util

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder.standard
import com.amazonaws.services.s3.model.{ListObjectsV2Result, S3ObjectSummary}
import com.expedia.www.haystack.service.graph.snapshot.store.Constants.{DotCsv, SlashEdges, SlashNodes}
import com.expedia.www.haystack.service.graph.snapshot.store.S3SnapshotStoreSpec.itemNamesWrittenToS3
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoMoreInteractions, when}
import org.scalatest.{BeforeAndAfterAll, _}
import org.scalatest.mockito.MockitoSugar

import scala.collection.JavaConverters._
import scala.collection.{immutable, mutable}

object S3SnapshotStoreSpec {
  private val itemNamesWrittenToS3 = mutable.SortedSet[(String, String)]()
}

class S3SnapshotStoreSpec extends SnapshotStoreSpecBase with BeforeAndAfterAll with MockitoSugar with Matchers {
  // Set to true to run these test in an integration-type way, talking to a real S3.
  // You must have valid keys on your machine to do so, typically in ~/.aws/credentials.
  private val useRealS3 = false

  private val bucketName = "haystack-service-graph-snapshots"
  private val folderName = "unit-test-snapshots"
  private val nextContinuationToken = "nextContinuationToken"
  private val listObjectsV2Result = mock[ListObjectsV2Result]
  private val s3Client = if (useRealS3) standard.withRegion(Regions.US_WEST_2).build else mock[AmazonS3]
  private val serviceGraphJson = readFile(Constants.JsonFileNameWithExtension)
  private val nodesCsv = readFile(Constants.NodesCsvFileNameWithExtension)
  private val edgesCsv = readFile(Constants.EdgesCsvFileNameWithExtension)

  override def afterAll() {
    if (useRealS3) {
      itemNamesWrittenToS3.foreach(itemName => s3Client.deleteObject(bucketName, itemName._1))
      itemNamesWrittenToS3.foreach(itemName => s3Client.deleteObject(bucketName, itemName._2))
      s3Client.deleteBucket(bucketName)
    }
    else {
      verifyNoMoreInteractionsForAllMocksThenReset()
    }
  }

  describe("S3SnapshotStore.build()") {
    val store = new S3SnapshotStore()
    var s3Store = store.build(Array(store.getClass.getCanonicalName, bucketName, folderName, "42"))
      .asInstanceOf[S3SnapshotStore]
    it("should use the arguments in the default constructor and the array") {
      val s3Client: AmazonS3 = s3Store.s3Client
      s3Client.getRegion.toString shouldEqual Regions.US_WEST_2.getName
      s3Store.bucketName shouldEqual bucketName
      s3Store.folderName shouldEqual folderName
      s3Store.listObjectsBatchSize shouldEqual 42
    }
    it("should use 0 for listObjectsBatchSize if no listObjectsBatchSize is specified in the args array") {
      s3Store = store.build(Array(store.getClass.getCanonicalName, bucketName, folderName))
        .asInstanceOf[S3SnapshotStore]
      s3Store.listObjectsBatchSize shouldEqual 0
    }
  }

  describe("S3SnapshotStore") {
    var s3Store = new S3SnapshotStore(s3Client, bucketName, folderName, 3)
    it("should create the bucket when the bucket does not exist") {
      if (!useRealS3) {
        whensForWrite(false)
      }
      itemNamesWrittenToS3 += s3Store.write(oneMillisecondBeforeNow, serviceGraphJson)
      if (!useRealS3) {
        verify(s3Client).doesBucketExistV2(bucketName)
        verify(s3Client).createBucket(bucketName)
        verify(s3Client).putObject(bucketName, createItemName(oneMillisecondBeforeNow) + SlashNodes + DotCsv, nodesCsv)
        verify(s3Client).putObject(bucketName, createItemName(oneMillisecondBeforeNow) + SlashEdges + DotCsv, edgesCsv)
        verifyNoMoreInteractionsForAllMocksThenReset()
      }
    }
    it("should not create the bucket when the bucket already exists") {
      if (!useRealS3) {
        whensForWrite(true)
      }
      itemNamesWrittenToS3 += s3Store.write(oneMillisecondAfterNow, serviceGraphJson)
      itemNamesWrittenToS3 += s3Store.write(twoMillisecondsAfterNow, serviceGraphJson)
      if (!useRealS3) {
        verify(s3Client, times(2)).doesBucketExistV2(bucketName)
        verify(s3Client).putObject(bucketName, createItemName(oneMillisecondAfterNow) + SlashNodes + DotCsv, nodesCsv)
        verify(s3Client).putObject(bucketName, createItemName(oneMillisecondAfterNow) + SlashEdges + DotCsv, edgesCsv)
        verify(s3Client).putObject(bucketName, createItemName(twoMillisecondsAfterNow) + SlashNodes + DotCsv, nodesCsv)
        verify(s3Client).putObject(bucketName, createItemName(twoMillisecondsAfterNow) + SlashEdges + DotCsv, edgesCsv)
        verifyNoMoreInteractionsForAllMocksThenReset()
      }
    }
    it("should return None when read() is called with a time that is too early") {
      if (!useRealS3) {
        whensForRead
        when(listObjectsV2Result.isTruncated).thenReturn(false)
        when(listObjectsV2Result.getObjectSummaries).thenReturn(convertStringToS3ObjectSummary)
      }
      assert(s3Store.read(twoMillisecondsBeforeNow).isEmpty)
      if (!useRealS3) {
        verifiesForRead(1)
        verifyNoMoreInteractionsForAllMocksThenReset()
      }
    }
    it("should return the correct object when read() is called with a time that is not an exact match but is not too early") {
      if (!useRealS3) {
        whensForRead
        when(s3Client.getObjectAsString(anyString(), anyString())).thenReturn(nodesCsv, edgesCsv)
        when(listObjectsV2Result.isTruncated).thenReturn(false)
        when(listObjectsV2Result.getObjectSummaries).thenReturn(convertStringToS3ObjectSummary)
      }
      assert(s3Store.read(now).get == serviceGraphJson)
      if (!useRealS3) {
        verifiesForRead(1)
        verify(s3Client).getObjectAsString(anyString(),
          org.mockito.Matchers.eq(createItemName(oneMillisecondBeforeNow) + SlashNodes))
        verify(s3Client).getObjectAsString(anyString(),
          org.mockito.Matchers.eq(createItemName(oneMillisecondBeforeNow) + SlashEdges))
        verifyNoMoreInteractionsForAllMocksThenReset()
      }
    }
    it("should return the correct object when read() is called with a time that is an exact match") {
      if (!useRealS3) {
        whensForRead
        when(s3Client.getObjectAsString(anyString(), anyString())).thenReturn(nodesCsv, edgesCsv)
        when(listObjectsV2Result.isTruncated).thenReturn(false)
        when(listObjectsV2Result.getObjectSummaries).thenReturn(convertStringToS3ObjectSummary)
      }
      val actual = s3Store.read(twoMillisecondsAfterNow).get
      val expected = serviceGraphJson
      assert(actual == expected)
      if (!useRealS3) {
        verifiesForRead(1)
        verify(s3Client).getObjectAsString(anyString(),
          org.mockito.Matchers.eq(createItemName(twoMillisecondsAfterNow) + SlashNodes))
        verify(s3Client).getObjectAsString(anyString(),
          org.mockito.Matchers.eq(createItemName(twoMillisecondsAfterNow) + SlashEdges))
        verifyNoMoreInteractionsForAllMocksThenReset()
      }
    }
    it("should return the correct object for small batches") {
      s3Store = new S3SnapshotStore(s3Client, bucketName, folderName, 1)
      if (!useRealS3) {
        whensForRead
        when(s3Client.getObjectAsString(anyString(), anyString())).thenReturn(nodesCsv, edgesCsv)
        when(listObjectsV2Result.isTruncated).thenReturn(true, true, false)
        val it = itemNamesWrittenToS3.iterator
        when(listObjectsV2Result.getObjectSummaries)
          .thenReturn(
            convertTupleToObjectSummary(it.next()).asJava,
            convertTupleToObjectSummary(it.next()).asJava,
            convertTupleToObjectSummary(it.next()).asJava)
      }
      assert(s3Store.read(twoMillisecondsAfterNow).get == serviceGraphJson)
      if (!useRealS3) {
        verifiesForRead(3)
        verify(s3Client).getObjectAsString(anyString(),
          org.mockito.Matchers.eq(createItemName(twoMillisecondsAfterNow) + SlashNodes))
        verify(s3Client).getObjectAsString(anyString(),
          org.mockito.Matchers.eq(createItemName(twoMillisecondsAfterNow) + SlashEdges))
        verifyNoMoreInteractionsForAllMocksThenReset()
      }
    }
    it("should never delete any items when purge() is called") {
      s3Store.purge(twoMillisecondsAfterNow) shouldEqual 0
      if (!useRealS3) {
        verifyNoMoreInteractionsForAllMocksThenReset()
      }
    }
    it("should throw an IllegalArgumentException when read() is called with a 0 value of listObjectsBatchSize") {
      s3Store = new S3SnapshotStore(s3Client, bucketName, folderName, 0)
      an [IllegalArgumentException] should be thrownBy s3Store.read(twoMillisecondsBeforeNow)
      if (!useRealS3) {
        verifyNoMoreInteractionsForAllMocksThenReset()
      }
    }
  }

  private def convertTupleToObjectSummary(tuple: (String, String)): immutable.Seq[S3ObjectSummary] = {
    val s3ObjectSummary1 = new S3ObjectSummary
    s3ObjectSummary1.setBucketName(bucketName)
    s3ObjectSummary1.setKey(tuple._1)
    val s3ObjectSummary2 = new S3ObjectSummary
    s3ObjectSummary2.setBucketName(bucketName)
    s3ObjectSummary2.setKey(tuple._2)
    List(s3ObjectSummary1, s3ObjectSummary2)
  }

  private def convertStringToS3ObjectSummary: util.List[S3ObjectSummary] = {
    val listBuilder = List.newBuilder[S3ObjectSummary]
    for (tuple <- itemNamesWrittenToS3) {
      val list: immutable.Seq[S3ObjectSummary] = convertTupleToObjectSummary(tuple)
      listBuilder += list.head
      listBuilder += list(1)
    }
    listBuilder.result().asJava
  }

  private def verifiesForRead(loopTimes: Int) = {
    verify(s3Client, times(loopTimes)).listObjectsV2(bucketName)
    verify(listObjectsV2Result, times(loopTimes)).getObjectSummaries
    verify(listObjectsV2Result, times(loopTimes)).getNextContinuationToken
    verify(listObjectsV2Result, times(loopTimes)).isTruncated
  }

  private def whensForRead = {
    when(listObjectsV2Result.getNextContinuationToken).thenReturn(nextContinuationToken)
    when(s3Client.listObjectsV2(anyString())).thenReturn(listObjectsV2Result)
  }

  private def whensForWrite(doesBucketExist: Boolean) = {
    when(s3Client.doesBucketExistV2(anyString())).thenReturn(doesBucketExist)
    when(listObjectsV2Result.getNextContinuationToken).thenReturn(nextContinuationToken)
  }

  private def verifyNoMoreInteractionsForAllMocksThenReset(): Unit = {
    verifyNoMoreInteractions(s3Client, listObjectsV2Result)
    Mockito.reset(s3Client, listObjectsV2Result)
  }

  private def createItemName(thisInstant: Instant) = {
    folderName + "/" + ISO_INSTANT.format(thisInstant)
  }
}