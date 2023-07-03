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

package com.expedia.www.haystack.trace.indexer.integration

import java.util.UUID
import java.util.concurrent.{Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}

import com.expedia.open.tracing.Tag.TagType
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.open.tracing.{Log, Span, Tag}
import com.expedia.www.haystack.trace.commons.packer.{PackerType, Unpacker}
import com.expedia.www.haystack.trace.indexer.config.entities.SpanAccumulatorConfiguration
import com.expedia.www.haystack.trace.indexer.integration.clients.{ElasticSearchTestClient, GrpcTestClient, KafkaTestClient}
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils
import org.scalatest._

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

case class TraceDescription(traceId: String, spanIdPrefix: String)

abstract class BaseIntegrationTestSpec extends WordSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  protected val MAX_WAIT_FOR_OUTPUT_MS = 12000

  protected val spanAccumulatorConfig = SpanAccumulatorConfiguration(
    minTracesPerCache = 100,
    maxEntriesAllStores = 500,
    pollIntervalMillis = 2000L,
    bufferingWindowMillis = 6000L,
    PackerType.SNAPPY)

  protected var scheduler: ScheduledExecutorService = _

  val kafka = new KafkaTestClient
  val traceBackendClient = new GrpcTestClient
  val elastic = new ElasticSearchTestClient

  override def beforeAll() {
    scheduler = Executors.newSingleThreadScheduledExecutor()
    kafka.prepare(getClass.getSimpleName)
    traceBackendClient.prepare()
    elastic.prepare()
  }

  override def afterAll(): Unit = if (scheduler != null) scheduler.shutdownNow()

  protected def validateChildSpans(spanBuffer: SpanBuffer,
                                   traceId: String,
                                   spanIdPrefix: String,
                                   childSpanCount: Int): Unit = {
    spanBuffer.getTraceId shouldBe traceId

  withClue(s"the trace-id $traceId has lesser spans than expected"){
    spanBuffer.getChildSpansCount shouldBe childSpanCount
  }

    (0 until spanBuffer.getChildSpansCount).toList foreach { idx =>
      spanBuffer.getChildSpans(idx).getSpanId shouldBe s"$spanIdPrefix-$idx"
      spanBuffer.getChildSpans(idx).getTraceId shouldBe spanBuffer.getTraceId
      spanBuffer.getChildSpans(idx).getServiceName shouldBe s"service$idx"
      spanBuffer.getChildSpans(idx).getParentSpanId should not be null
      spanBuffer.getChildSpans(idx).getOperationName shouldBe s"op$idx"
    }
  }

  private def randomSpan(traceId: String, spanId: String, serviceName: String, operationName: String): Span = {
    Span.newBuilder()
      .setTraceId(traceId)
      .setParentSpanId(UUID.randomUUID().toString)
      .setSpanId(spanId)
      .setServiceName(serviceName)
      .setOperationName(operationName)
      .setStartTime(System.currentTimeMillis() * 1000)
      .addTags(Tag.newBuilder().setKey("errorCode").setType(TagType.LONG).setVLong(404))
      .addTags(Tag.newBuilder().setKey("_role").setType(TagType.STRING).setVStr("haystack"))
      .addLogs(Log.newBuilder().addFields(Tag.newBuilder().setKey("exceptiontype").setType(TagType.STRING).setVStr("external").build()).build())
      .build()
  }

  protected def produceSpansAsync(maxSpansPerTrace: Int,
                                  produceInterval: FiniteDuration,
                                  traceDescription: List[TraceDescription],
                                  startRecordTimestamp: Long,
                                  maxRecordTimestamp: Long,
                                  startSpanIdxFrom: Int = 0): ScheduledFuture[_] = {
    var timestamp = startRecordTimestamp
    var cnt = 0
    scheduler.scheduleWithFixedDelay(() => {
      if (cnt < maxSpansPerTrace) {
        val spans = traceDescription.map(sd => {
          new KeyValue[String, Span](sd.traceId, randomSpan(sd.traceId, s"${sd.spanIdPrefix}-${startSpanIdxFrom + cnt}", s"service${startSpanIdxFrom + cnt}", s"op${startSpanIdxFrom + cnt}"))
        }).asJava
        IntegrationTestUtils.produceKeyValuesSynchronouslyWithTimestamp(
          kafka.INPUT_TOPIC,
          spans,
          kafka.TEST_PRODUCER_CONFIG,
          timestamp)
        timestamp = timestamp + (maxRecordTimestamp / (maxSpansPerTrace - 1))
      }
      cnt = cnt + 1
    }, 0, produceInterval.toMillis, TimeUnit.MILLISECONDS)
  }

  def verifyBackendWrites(traceDescriptions: Seq[TraceDescription], minSpansPerTrace: Int, maxSpansPerTrace: Int): Unit = {
    val traceRecords = traceBackendClient.queryTraces(traceDescriptions)

    traceRecords should have size traceDescriptions.size

    traceRecords.foreach(record => {
      val spanBuffer = Unpacker.readSpanBuffer(record.getSpans.toByteArray)
      val descr = traceDescriptions.find(_.traceId == record.getTraceId).get
      record.getSpans should not be null
      spanBuffer.getChildSpansCount should be >= minSpansPerTrace
      spanBuffer.getChildSpansCount should be <= maxSpansPerTrace

      spanBuffer.getChildSpansList.asScala.zipWithIndex foreach {
        case (sp, idx) =>
          sp.getSpanId shouldBe s"${descr.spanIdPrefix}-$idx"
          sp.getServiceName shouldBe s"service$idx"
          sp.getOperationName shouldBe s"op$idx"
      }
    })
  }

  def verifyOperationNames(): Unit = {
    val operationNamesQuery =
      """{
        | "query" : {
        |    "term" : {
        |      "servicename" : {
        |        "value" : "service0",
        |        "boost" : 1.0
        |      }
        |    }
        |  },
        |  "_source" : {
        |    "includes" : [
        |      "operationname"
        |    ],
        |    "excludes" : [
        |      "servicename"
        |    ]
        |  }
        |}""".stripMargin
    val docs = elastic.queryServiceMetadataIndex(operationNamesQuery)
    docs.size shouldBe 1

  }

  def verifyElasticSearchWrites(traceIds: Seq[String]): Unit = {
    val matchAllQuery =
      """{
        |    "query": {
        |        "match_all": {}
        |    }
        |}""".stripMargin

    var docs = elastic.querySpansIndex(matchAllQuery)
    docs.size shouldBe traceIds.size
    docs.indices.foreach { idx =>
      val traceId = docs.apply(idx).traceid
      traceIds should contain(traceId)
    }

    val spanSpecificQuery =
      """
        |{
        |  "query": {
        |    "bool": {
        |      "must": [
        |        {
        |          "nested": {
        |            "path": "spans",
        |            "query": {
        |              "bool": {
        |                "must": [
        |                  {
        |                    "match": {
        |                      "spans.servicename": "service0"
        |                    }
        |                  },
        |                  {
        |                    "match": {
        |                      "spans.operationname": "op0"
        |                    }
        |                  }
        |                ]
        |              }
        |            }
        |          }
        |        }
        |      ]
        |}}}
      """.stripMargin
    docs = elastic.querySpansIndex(spanSpecificQuery)
    docs.size shouldBe traceIds.size

    val emptyResponseQuery =
      """
        |{
        |  "query": {
        |    "bool": {
        |      "must": [
        |        {
        |          "nested": {
        |            "path": "spans",
        |            "query": {
        |              "bool": {
        |                "must": [
        |                  {
        |                    "match": {
        |                      "spans.servicename": "service0"
        |                    }
        |                  },
        |                  {
        |                    "match": {
        |                      "spans.operationname": "op1"
        |                    }
        |                  }
        |                ]
        |              }
        |            }
        |          }
        |        }
        |      ]
        |}}}
      """.stripMargin
    docs = elastic.querySpansIndex(emptyResponseQuery)
    docs.size shouldBe 0

    val tagQuery =
      """
        |{
        |  "query": {
        |    "bool": {
        |      "must": [
        |        {
        |          "nested": {
        |            "path": "spans",
        |            "query": {
        |              "bool": {
        |                "must": [
        |                  {
        |                    "match": {
        |                      "spans.servicename": "service2"
        |                    }
        |                  },
        |                  {
        |                    "match": {
        |                      "spans.operationname": "op2"
        |                    }
        |                  },
        |                  {
        |                    "match": {
        |                      "spans.errorcode": "404"
        |                    }
        |                  }
        |                ]
        |              }
        |            }
        |          }
        |        }
        |      ]
        |}}}
      """.stripMargin
    docs = elastic.querySpansIndex(tagQuery)
    docs.size shouldBe traceIds.size
    docs.map(_.traceid) should contain theSameElementsAs traceIds


    val roleTagQuery =
      """
        |{
        |  "query": {
        |    "bool": {
        |      "must": [
        |        {
        |          "nested": {
        |            "path": "spans",
        |            "query": {
        |              "bool": {
        |                "must": [
        |                  {
        |                    "match": {
        |                      "spans.servicename": "service2"
        |                    }
        |                  },
        |                  {
        |                    "match": {
        |                      "spans.operationname": "op2"
        |                    }
        |                  },
        |                  {
        |                    "match": {
        |                      "spans.role": "haystack"
        |                    }
        |                  }
        |                ]
        |              }
        |            }
        |          }
        |        }
        |      ]
        |}}}
      """.stripMargin
    docs = elastic.querySpansIndex(roleTagQuery)
    docs.size shouldBe traceIds.size
    docs.map(_.traceid) should contain theSameElementsAs traceIds

  }
}
