/*
 *  Copyright 2019, Expedia Group.
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

package com.expedia.www.haystack.trace.indexer.integration.clients

import java.text.SimpleDateFormat
import java.util.Date

import com.expedia.www.haystack.commons.retries.RetryOperation
import com.expedia.www.haystack.trace.commons.config.entities._
import com.expedia.www.haystack.trace.indexer.config.entities.{ElasticSearchConfiguration, ServiceMetadataWriteConfiguration}
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.core.Search
import io.searchbox.indices.DeleteIndex
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats}

case class EsSourceDocument(traceid: String)

class ElasticSearchTestClient {
  protected implicit val formats: Formats = DefaultFormats + new EnumNameSerializer(IndexFieldType)

  private val ELASTIC_SEARCH_ENDPOINT = "http://elasticsearch:9200"
  private val SPANS_INDEX_NAME_PREFIX = "haystack-traces"
  private val SPANS_INDEX_TYPE = "spans"
  private val SPANS_INDEX_HOUR_BUCKET = 6

  private val HAYSTACK_TRACES_INDEX = {
    val formatter = new SimpleDateFormat("yyyy-MM-dd")
    s"$SPANS_INDEX_NAME_PREFIX-${formatter.format(new Date())}"
  }

  private val esClient: JestClient = {
    val factory = new JestClientFactory()
    factory.setHttpClientConfig(new HttpClientConfig.Builder(ELASTIC_SEARCH_ENDPOINT).build())
    factory.getObject
  }

  def prepare(): Unit = {
    // drop the haystack-traces-<today's date> index
    0 until (24 / SPANS_INDEX_HOUR_BUCKET) foreach {
      idx => {
        esClient.execute(new DeleteIndex.Builder(s"$HAYSTACK_TRACES_INDEX-$idx").build())
      }
    }
  }

  def buildConfig = ElasticSearchConfiguration(
    ELASTIC_SEARCH_ENDPOINT,
    None,
    None,
    Some(INDEX_TEMPLATE),
    "one",
    SPANS_INDEX_NAME_PREFIX,
    SPANS_INDEX_HOUR_BUCKET,
    SPANS_INDEX_TYPE,
    3000,
    3000,
    5,
    10,
    10,
    10,
    RetryOperation.Config(3, 2000, 2),
    getAWSRequestSigningConfiguration)

  def getAWSRequestSigningConfiguration: AWSRequestSigningConfiguration = {
    AWSRequestSigningConfiguration(enabled = false, "", "", None, None)
  }

  def buildServiceMetadataConfig: ServiceMetadataWriteConfiguration = {
    ServiceMetadataWriteConfiguration(enabled = true,
      esEndpoint = ELASTIC_SEARCH_ENDPOINT,
      username = None,
      password = None,
      consistencyLevel = "one",
      indexTemplateJson = Some(SERVICE_METADATA_INDEX_TEMPLATE),
      indexName = "service-metadata",
      indexType = "metadata",
      connectionTimeoutMillis = 3000,
      readTimeoutMillis = 3000,
      maxInFlightBulkRequests = 10,
      maxDocsInBulk = 5,
      maxBulkDocSizeInBytes = 50,
      flushIntervalInSec = 10,
      flushOnMaxOperationCount = 10,
      retryConfig = RetryOperation.Config(10, 250, 2))
  }


  def indexingConfig: WhitelistIndexFieldConfiguration = {
    val cfg = WhitelistIndexFieldConfiguration()
    val cfgJsonData = Serialization.write(WhiteListIndexFields(
      List(WhitelistIndexField(name = "role", `type` = IndexFieldType.string, aliases = Set("_role")), WhitelistIndexField(name = "errorcode", `type` = IndexFieldType.long))))
    cfg.onReload(cfgJsonData)
    cfg
  }

  def querySpansIndex(query: String): List[EsSourceDocument] = {
    import scala.collection.JavaConverters._
    val searchQuery = new Search.Builder(query)
      .addIndex(SPANS_INDEX_NAME_PREFIX)
      .addType(SPANS_INDEX_TYPE)
      .build()
    val result = esClient.execute(searchQuery)
    if (result.getSourceAsStringList != null && result.getSourceAsStringList.size() > 0) {
      result.getSourceAsStringList.asScala.map(Serialization.read[EsSourceDocument]).toList
    }
    else {
      Nil
    }
  }

  def queryServiceMetadataIndex(query: String): List[String] = {
    import scala.collection.JavaConverters._
    val SERVICE_METADATA_INDEX_NAME = "service-metadata"
    val SERVICE_METADATA_INDEX_TYPE = "metadata"
    val searchQuery = new Search.Builder(query)
      .addIndex(SERVICE_METADATA_INDEX_NAME)
      .addType(SERVICE_METADATA_INDEX_TYPE)
      .build()
    val result = esClient.execute(searchQuery)
    if (result.getSourceAsStringList != null && result.getSourceAsStringList.size() > 0) {
      result.getSourceAsStringList.asScala.toList
    }
    else {
      Nil
    }
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
      |            "_field_names": {
      |                "enabled": false
      |            },
      |            "_all": {
      |                "enabled": false
      |            },
      |            "_source": {
      |                "includes": ["traceid"]
      |            },
      |            "properties": {
      |                "traceid": {
      |                   "enabled": false
      |                },
      |                "starttime": {
      |                   "type": "long",
      |                   "doc_values": true
      |                },
      |                "spans": {
      |                    "type": "nested",
      |                    "properties": {
      |                        "starttime": {
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

  private val SERVICE_METADATA_INDEX_TEMPLATE =
    """{
      |  "template": "service-metadata*",
      |  "aliases": {
      |    "service-metadata": {}
      |  },
      |  "settings": {
      |    "number_of_shards": 4,
      |    "index.mapping.ignore_malformed": true,
      |    "analysis": {
      |      "normalizer": {
      |        "lowercase_normalizer": {
      |          "type": "custom",
      |          "filter": [
      |            "lowercase"
      |          ]
      |        }
      |      }
      |    }
      |  },
      |  "mappings": {
      |    "metadata": {
      |      "_field_names": {
      |        "enabled": false
      |      },
      |      "_all": {
      |        "enabled": false
      |      },
      |      "properties": {
      |        "servicename": {
      |          "type": "keyword",
      |          "norms": false
      |        },
      |        "operationname": {
      |          "type": "keyword",
      |          "doc_values": false,
      |          "norms": false
      |        }
      |      }
      |    }
      |  }
      |}
      |""".stripMargin
}
