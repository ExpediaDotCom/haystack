/*
 *  Copyright 2019 Expedia, Inc.
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

package com.expedia.www.haystack.trace.indexer.unit

import com.expedia.www.haystack.trace.commons.config.entities.WhitelistIndexFieldConfiguration
import com.expedia.www.haystack.trace.indexer.writers.es.IndexTemplateHandler
import com.google.gson.{Gson, JsonParser}
import io.searchbox.client.{JestClient, JestResult}
import io.searchbox.indices.template.{GetTemplate, PutTemplate}
import org.easymock.EasyMock
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}

class IndexTemplateHandlerSpec extends FunSpec with Matchers with EasyMockSugar {

  private val templateJson =
    """
      |{
      |    "spans-index-template": {
      |        "order": 0,
      |        "index_patterns": ["haystack-traces*"],
      |        "settings": {
      |            "index": {
      |                "analysis": {
      |                    "normalizer": {
      |                        "lowercase_normalizer": {
      |                            "filter": ["lowercase"],
      |                            "type": "custom"
      |                        }
      |                    }
      |                },
      |                "number_of_shards": "8",
      |                "mapping": {
      |                    "ignore_malformed": "true"
      |                }
      |            }
      |        },
      |        "mappings": {
      |            "spans": {
      |                "_field_names": {
      |                    "enabled": false
      |                },
      |                "_all": {
      |                    "enabled": false
      |                },
      |                "_source": {
      |                    "includes": ["traceid"]
      |                },
      |                "properties": {
      |                    "traceid": {
      |                        "enabled": false
      |                    },
      |                    "starttime": {
      |                        "type": "long",
      |                        "doc_values": true
      |                    },
      |                    "spans": {
      |                        "type": "nested",
      |                        "properties": {
      |                            "servicename": {
      |                                "type": "keyword",
      |                                "normalizer": "lowercase_normalizer",
      |                                "doc_values": false,
      |                                "norms": false
      |                            },
      |                            "operationname": {
      |                                "type": "keyword",
      |                                "normalizer": "lowercase_normalizer",
      |                                "doc_values": false,
      |                                "norms": false
      |                            },
      |                            "starttime": {
      |                                "enabled": false
      |                            },
      |                            "duration": {
      |                                "type": "long",
      |                                "doc_values": true
      |                            },
      |                            "f1": {
      |                                "type": "long",
      |                                "doc_values": true
      |                            }
      |                        }
      |                    }
      |                },
      |                "dynamic_templates": [{
      |                    "strings_as_keywords_1": {
      |                        "match_mapping_type": "string",
      |                        "mapping": {
      |                            "type": "keyword",
      |                            "normalizer": "lowercase_normalizer",
      |                            "doc_values": false,
      |                            "norms": false
      |                        }
      |                    }
      |                }, {
      |                    "longs_disable_doc_norms": {
      |                        "match_mapping_type": "long",
      |                        "mapping": {
      |                            "type": "long",
      |                            "doc_values": false,
      |                            "norms": false
      |                        }
      |                    }
      |                }]
      |            }
      |        },
      |        "aliases": {
      |            "haystack-traces": {}
      |        }
      |    }
      |}
      |
          """.stripMargin
  describe("Index Template Handler") {
    it("should read the template and update it") {
      val client = mock[JestClient]
      val getTemplateResult = new JestResult(new Gson())
      val putTemplateResult = new JestResult(new Gson())
      val config = WhitelistIndexFieldConfiguration()

      val getTemplate = EasyMock.newCapture[GetTemplate]()
      val putTemplate = EasyMock.newCapture[PutTemplate]()

      expecting {
        getTemplateResult.setSucceeded(true)
        putTemplateResult.setSucceeded(true)
        getTemplateResult.setJsonObject(new JsonParser().parse(templateJson).getAsJsonObject)
        client.execute(EasyMock.capture(getTemplate)).andReturn(getTemplateResult)
        client.execute(EasyMock.capture(putTemplate)).andReturn(putTemplateResult)
      }

      whenExecuting(client) {
        new IndexTemplateHandler(client, None, "spans", config).run()
        config.onReload(
          """
          |{
          |"fields": [
          |   {
          |     "name": "status_code",
          |      "type": "int",
          |      "enableRangeQuery": true
          |   },
          |    {
          |     "name": "f1",
          |      "type": "long",
          |      "enableRangeQuery": false
          |   }
          |]}
        """.
            stripMargin)
      }

      putTemplate.getValue.getData(new Gson()) shouldEqual "{\"settings\":{\"index\":{\"analysis\":{\"normalizer\":{\"lowercase_normalizer\":{\"filter\":[\"lowercase\"],\"type\":\"custom\"}}},\"number_of_shards\":\"8\",\"mapping\":{\"ignore_malformed\":\"true\"}}},\"mappings\":{\"spans\":{\"_field_names\":{\"enabled\":false},\"_all\":{\"enabled\":false},\"_source\":{\"includes\":[\"traceid\"]},\"properties\":{\"traceid\":{\"enabled\":false},\"starttime\":{\"type\":\"long\",\"doc_values\":true},\"spans\":{\"type\":\"nested\",\"properties\":{\"servicename\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false},\"operationname\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false},\"starttime\":{\"enabled\":false},\"duration\":{\"type\":\"long\",\"doc_values\":true},\"f1\":{\"type\":\"long\",\"doc_values\":false}}}},\"dynamic_templates\":[{\"strings_as_keywords_1\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false}}},{\"longs_disable_doc_norms\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"doc_values\":false,\"norms\":false}}}]}},\"aliases\":{\"haystack-traces\":{}},\"index_patterns\":[\"haystack-traces*\"],\"order\":0}"
    }
  }
}

