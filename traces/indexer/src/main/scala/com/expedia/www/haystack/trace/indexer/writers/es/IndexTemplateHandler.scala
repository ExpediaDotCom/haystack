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

package com.expedia.www.haystack.trace.indexer.writers.es

import java.util

import com.expedia.www.haystack.trace.commons.config.entities.IndexFieldType.IndexFieldType
import com.expedia.www.haystack.trace.commons.config.entities.{IndexFieldType, WhitelistIndexFieldConfiguration}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.searchbox.client.JestClient
import io.searchbox.indices.template.{GetTemplate, PutTemplate}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class IndexTemplateHandler(client: JestClient,
                           applyTemplate: Option[String],
                           indexType: String,
                           whitelistFieldConfig: WhitelistIndexFieldConfiguration) {

  private val LOGGER = LoggerFactory.getLogger(classOf[IndexTemplateHandler])

  private val ES_TEMPLATE_NAME = "spans-index-template"
  private val mapper = new ObjectMapper()

  def run() {
    applyTemplate match {
      case Some(template) => updateESTemplate(template)
      case _ => /* may be the template is set from outside the app */
    }

    whitelistFieldConfig.addOnChangeListener(() => {
      LOGGER.info("applying the new elastic template as whitelist fields have changed from query perspective like enableRangeQuery")
      readTemplate() match {
        case Some(template) => updateESTemplate(template)
        case _ =>
      }
    })
  }

  private def esDataType(`type`: IndexFieldType): String = {
    `type` match {
      case IndexFieldType.int => "integer"
      case IndexFieldType.string => "keyword"
      case _ => `type`.toString
    }
  }

  private def updateESTemplate(templateJson: String): Unit = {
    val esTemplate: util.HashMap[String, Object] = mapper.readValue(templateJson, new TypeReference[util.HashMap[String, Object]]() {})
    val mappings = esTemplate.get("mappings").asInstanceOf[util.HashMap[String, Object]]
    val propertyMap =
      mappings.get(indexType).asInstanceOf[util.HashMap[String, Object]]
        .get("properties").asInstanceOf[util.HashMap[String, Object]]
        .get(indexType).asInstanceOf[util.HashMap[String, Object]]
        .get("properties").asInstanceOf[util.HashMap[String, Object]]

    whitelistFieldConfig.whitelistIndexFields.foreach(wf => {
      val prop = propertyMap.get(wf.name)
      if (prop != null) {
        if (wf.enabled && wf.enableRangeQuery) {
          propertyMap.put(wf.name, Map("type" -> esDataType(wf.`type`), "doc_values" -> true, "norms" -> false).asJava)
        } else {
          prop.asInstanceOf[util.HashMap[String, Object]].put("doc_values", Boolean.box(wf.enableRangeQuery))
        }
      }
    })

    val newTemplateJson = mapper.writeValueAsString(esTemplate)

    LOGGER.info(s"setting the template with name $ES_TEMPLATE_NAME - $newTemplateJson")

    val putTemplateRequest = new PutTemplate.Builder(ES_TEMPLATE_NAME, newTemplateJson).build()
    val result = client.execute(putTemplateRequest)
    if (!result.isSucceeded) {
      throw new RuntimeException(s"Fail to apply the following template to elastic search with reason=${result.getErrorMessage}")
    }
  }

  private def readTemplate(): Option[String] = {
    val request = new GetTemplate.Builder(ES_TEMPLATE_NAME).build()
    val result = client.execute(request)
    if (result.isSucceeded) {
      Some(result.getJsonObject.get(ES_TEMPLATE_NAME).toString)
    } else {
      LOGGER.error(s"Fail to read the template with name $ES_TEMPLATE_NAME for reason ${result.getErrorMessage}")
      None
    }
  }
}
