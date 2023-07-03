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

package com.expedia.www.haystack.trace.reader.integration

import java.text.SimpleDateFormat
import java.util.concurrent.{Executors, TimeUnit}
import java.util.{Date, UUID}

import com.expedia.open.tracing.Span
import com.expedia.open.tracing.api.TraceReaderGrpc
import com.expedia.open.tracing.api.TraceReaderGrpc.TraceReaderBlockingStub
import com.expedia.open.tracing.backend.StorageBackendGrpc.StorageBackendBlockingStub
import com.expedia.open.tracing.backend.{StorageBackendGrpc, TraceRecord, WriteSpansRequest, WriteSpansResponse}
import com.expedia.open.tracing.buffer.SpanBuffer
import com.expedia.www.haystack.trace.commons.clients.es.document.TraceIndexDoc
import com.expedia.www.haystack.trace.commons.config.entities.{IndexFieldType, WhiteListIndexFields, WhitelistIndexField}
import com.expedia.www.haystack.trace.commons.packer.{PackerFactory, PackerType}
import com.expedia.www.haystack.trace.reader.Service
import com.expedia.www.haystack.trace.reader.unit.readers.builders.ValidTraceBuilder
import com.expedia.www.haystack.trace.storage.backends.memory.{Service => BackendService}
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.health.v1.HealthGrpc
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.core.Index
import io.searchbox.indices.CreateIndex
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats}
import org.scalatest._

import scala.collection.JavaConverters._
import scala.collection.mutable

trait BaseIntegrationTestSpec extends FunSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with ValidTraceBuilder {
  protected implicit val formats: Formats = DefaultFormats + new EnumNameSerializer(IndexFieldType)
  protected var client: TraceReaderBlockingStub = _

  protected var healthCheckClient: HealthGrpc.HealthBlockingStub = _

  private val ELASTIC_SEARCH_ENDPOINT = "http://elasticsearch:9200"
  private val ELASTIC_SEARCH_WHITELIST_INDEX = "reload-configs"
  private val ELASTIC_SEARCH_WHITELIST_TYPE = "whitelist-index-fields"
  private val SPANS_INDEX_TYPE = "spans"

  private val executors = Executors.newFixedThreadPool(2)

  private val DEFAULT_DURATION = TimeUnit.MILLISECONDS.toMicros(500)

  private val HAYSTACK_TRACES_INDEX = {
    val date = new Date()

    val dateBucket = new SimpleDateFormat("yyyy-MM-dd").format(date)
    val hourBucket = new SimpleDateFormat("HH").format(date).toInt / 6

    s"haystack-traces-$dateBucket-$hourBucket"
  }
  private val INDEX_TEMPLATE =
    """{
      |    "template": "haystack-traces*",
      |    "settings": {
      |        "number_of_shards": 1,
      |        "index.mapping.ignore_malformed": true,
      |        "analysis": {
      |            "normalizer": {
      |                "lowercase_normalizer": {
      |                    "type": "custom",
      |                    "filter": ["lowercase"]
      |                }
      |            }
      |        }
      |    },
      |    "aliases": {
      |        "haystack-traces": {}
      |    },
      |    "mappings": {
      |        "spans": {
      |            "_all": {
      |                "enabled": false
      |            },
      |            "_source": {
      |                "includes": ["traceid"]
      |            },
      |            "properties": {
      |                "traceid": {
      |                    "enabled": false
      |                },
      |                "starttime": {
      |                   "type": "long",
      |                   "doc_values": true
      |                },
      |                "spans": {
      |                    "type": "nested",
      |                    "properties": {
      |                        "servicename": {
      |                            "type": "keyword",
      |                            "normalizer": "lowercase_normalizer",
      |                            "doc_values": true,
      |                            "norms": false
      |                        },
      |                        "operationname": {
      |                            "type": "keyword",
      |                            "normalizer": "lowercase_normalizer",
      |                            "doc_values": true,
      |                            "norms": false
      |                        },
      |                        "starttime": {
      |                            "type": "long",
      |                            "doc_values": true
      |                        },
      |                        "duration": {
      |                            "type": "long",
      |                            "doc_values": true
      |                        }
      |                    }
      |                }
      |            },
      |            "dynamic_templates": [{
      |                "strings_as_keywords_1": {
      |                    "match_mapping_type": "string",
      |                    "mapping": {
      |                        "type": "keyword",
      |                        "normalizer": "lowercase_normalizer",
      |                        "doc_values": false,
      |                        "norms": false
      |                    }
      |                }
      |            }, {
      |                "longs_disable_doc_norms": {
      |                    "match_mapping_type": "long",
      |                    "mapping": {
      |                        "type": "long",
      |                        "doc_values": false,
      |                        "norms": false
      |                    }
      |                }
      |            }]
      |        }
      |    }
      |}
      |""".stripMargin


  private var esClient: JestClient = _
  private var traceBackendClient: StorageBackendBlockingStub = _

  def setupTraceBackend(): StorageBackendBlockingStub = {
    val port = 8090
    executors.submit(new Runnable {
      override def run(): Unit = BackendService.main(Array {
        port.toString
      })
    })
    traceBackendClient = StorageBackendGrpc.newBlockingStub(
      ManagedChannelBuilder.forAddress("localhost", port)
        .usePlaintext(true)
        .build())
    traceBackendClient
  }

  override def beforeAll() {
    // setup traceBackend
    traceBackendClient = setupTraceBackend()

    // setup elasticsearch
    val factory = new JestClientFactory()
    factory.setHttpClientConfig(
      new HttpClientConfig.Builder(ELASTIC_SEARCH_ENDPOINT)
        .multiThreaded(true)
        .build())
    esClient = factory.getObject
    esClient.execute(new CreateIndex.Builder(HAYSTACK_TRACES_INDEX)
      .settings(INDEX_TEMPLATE)
      .build)

    executors.submit(new Runnable {
      override def run(): Unit = Service.main(null)
    })

    Thread.sleep(5000)

    client = TraceReaderGrpc.newBlockingStub(ManagedChannelBuilder.forAddress("localhost", 8088)
      .usePlaintext(true)
      .build())

    healthCheckClient = HealthGrpc.newBlockingStub(ManagedChannelBuilder.forAddress("localhost", 8088)
      .usePlaintext(true)
      .build())
  }


  protected def putTraceInEsAndTraceBackend(traceId: String = UUID.randomUUID().toString,
                                            spanId: String = UUID.randomUUID().toString,
                                            serviceName: String = "",
                                            operationName: String = "",
                                            tags: Map[String, String] = Map.empty,
                                            startTime: Long = System.currentTimeMillis() * 1000,
                                            sleep: Boolean = true,
                                            duration: Long = DEFAULT_DURATION): Unit = {
    insertTraceInBackend(traceId, spanId, serviceName, operationName, tags, startTime, duration)
    insertTraceInEs(traceId, spanId, serviceName, operationName, tags, startTime, duration)

    // wait for few sec to let ES refresh its index
    if (sleep) Thread.sleep(5000)
  }

  private def insertTraceInEs(traceId: String,
                              spanId: String,
                              serviceName: String,
                              operationName: String,
                              tags: Map[String, String],
                              startTime: Long,
                              duration: Long) = {
    import TraceIndexDoc._
    // create map using service, operation and tags
    val fieldMap: mutable.Map[String, Any] = mutable.Map(
      SERVICE_KEY_NAME -> serviceName,
      OPERATION_KEY_NAME -> operationName,
      START_TIME_KEY_NAME -> mutable.Set[Any](startTime),
      DURATION_KEY_NAME -> mutable.Set[Any](duration)
    )
    tags.foreach(pair => fieldMap.put(pair._1.toLowerCase(), pair._2))

    // index the document
    val result = esClient.execute(new Index.Builder(TraceIndexDoc(traceId, 0, startTime, Seq(fieldMap)).json)
      .index(HAYSTACK_TRACES_INDEX)
      .`type`(SPANS_INDEX_TYPE)
      .build)

    if (result.getErrorMessage != null) {
      fail("Fail to execute the indexing request " + result.getErrorMessage)
    }
  }

  case class FieldWithMetadata(name: String, isRangeQuery: Boolean)

  protected def putWhitelistIndexFieldsInEs(fields: List[FieldWithMetadata]): Unit = {
    val whitelistFields = for (field <- fields) yield WhitelistIndexField(field.name, IndexFieldType.string, aliases = Set(s"_${field.name}"), field.isRangeQuery)
    esClient.execute(new Index.Builder(Serialization.write(WhiteListIndexFields(whitelistFields)))
      .index(ELASTIC_SEARCH_WHITELIST_INDEX)
      .`type`(ELASTIC_SEARCH_WHITELIST_TYPE)
      .build)

    // wait for few sec to let ES refresh its index and app to reload its config
    Thread.sleep(10000)
  }

  private def insertTraceInBackend(traceId: String,
                                   spanId: String,
                                   serviceName: String,
                                   operationName: String,
                                   tags: Map[String, String],
                                   startTime: Long,
                                   duration: Long): WriteSpansResponse = {
    val spanBuffer = createSpanBufferWithSingleSpan(traceId, spanId, serviceName, operationName, tags, startTime, duration)
    writeToBackend(spanBuffer, traceId)
  }

  protected def putTraceInBackend(traceId: String,
                                  spanId: String = UUID.randomUUID().toString,
                                  serviceName: String = "",
                                  operationName: String = "",
                                  tags: Map[String, String] = Map.empty,
                                  startTime: Long = System.currentTimeMillis() * 1000,
                                  duration: Long = DEFAULT_DURATION): Unit = {
    insertTraceInBackend(traceId, spanId, serviceName, operationName, tags, startTime, duration)
    // wait for few sec to let ES refresh its index
    Thread.sleep(1000)
  }

  protected def putTraceInBackendWithPartialSpans(traceId: String): WriteSpansResponse = {
    val trace = buildMultiServiceTrace()
    val spanBuffer = SpanBuffer
      .newBuilder()
      .setTraceId(traceId)
      .addAllChildSpans(trace.getChildSpansList)
      .build()

    writeToBackend(spanBuffer, traceId)
  }

  private def writeToBackend(spanBuffer: SpanBuffer, traceId: String): WriteSpansResponse = {
    val packer = PackerFactory.spanBufferPacker(PackerType.NONE)

    val traceRecord = TraceRecord
      .newBuilder()
      .setTraceId(traceId)
      .setSpans(ByteString.copyFrom(packer.apply(spanBuffer).packedDataBytes))
      .build()


    val writeSpanRequest = WriteSpansRequest.newBuilder()
      .addRecords(traceRecord)
      .build()

    traceBackendClient.writeSpans(writeSpanRequest)
  }

  private def createSpanBufferWithSingleSpan(traceId: String,
                                             spanId: String,
                                             serviceName: String,
                                             operationName: String,
                                             tags: Map[String, String],
                                             startTime: Long,
                                             duration: Long) = {
    val spanTags = tags.map(tag => com.expedia.open.tracing.Tag.newBuilder().setKey(tag._1).setVStr(tag._2).build())

    SpanBuffer
      .newBuilder()
      .setTraceId(traceId)
      .addChildSpans(Span
        .newBuilder()
        .setTraceId(traceId)
        .setSpanId(spanId)
        .setOperationName(operationName)
        .setServiceName(serviceName)
        .setStartTime(startTime)
        .setDuration(duration)
        .addAllTags(spanTags.asJava)
        .build())
      .build()
  }
}
