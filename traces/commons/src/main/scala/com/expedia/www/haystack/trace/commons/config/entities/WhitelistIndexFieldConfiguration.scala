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

package com.expedia.www.haystack.trace.commons.config.entities

import java.util.concurrent.ConcurrentHashMap

import com.expedia.www.haystack.trace.commons.config.entities.IndexFieldType.IndexFieldType
import com.expedia.www.haystack.trace.commons.config.reload.Reloadable
import org.apache.commons.lang3.StringUtils
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.Serialization
import org.json4s.{DefaultFormats, Formats}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

object IndexFieldType extends Enumeration {
  type IndexFieldType = Value
  val string, long, int, double, bool = Value
}

case class WhitelistIndexField(name: String,
                               `type`: IndexFieldType,
                               aliases: Set[String] = Set(),
                               enableRangeQuery: Boolean = false,
                               searchContext: String = "span",
                               enabled: Boolean = true)

case class WhiteListIndexFields(fields: List[WhitelistIndexField])

case class WhitelistIndexFieldConfiguration() extends Reloadable {
  private val LOGGER = LoggerFactory.getLogger(classOf[WhitelistIndexFieldConfiguration])

  protected implicit val formats: Formats = DefaultFormats + new EnumNameSerializer(IndexFieldType)

  @volatile
  private var currentVersion: Int = 0

  val indexFieldMap = new ConcurrentHashMap[String, WhitelistIndexField]()

  var reloadConfigTableName: Option[String] = None

  private val onChangeListeners = mutable.ListBuffer[() => Unit]()

  // fail fast
  override def name: String = reloadConfigTableName
    .getOrElse(throw new RuntimeException("fail to find the reload config table name!"))

  /**
    * this is called whenever the configuration reloader system reads the configuration object from external store
    * we check if the config data has changed using the string's hashCode
    * @param configData config object that is loaded at regular intervals from external store
    */
  override def onReload(configData: String): Unit = {
    if(StringUtils.isNotEmpty(configData) && hasConfigChanged(configData)) {
      LOGGER.info("new indexing fields have been detected: " + configData)
      val fieldsToIndex = Serialization.read[WhiteListIndexFields](configData)

      val lowercaseFieldNames = fieldsToIndex
        .fields
        .map(field => field.copy(name = field.name.toLowerCase, aliases = field.aliases.map(_.toLowerCase)))

      updateIndexFieldMap(WhiteListIndexFields(lowercaseFieldNames))
      // set the current version to newer one
      currentVersion = configData.hashCode

      this.synchronized {
        onChangeListeners.foreach(l => l())
      }
    }
  }

  def addOnChangeListener(listener: () => Unit): Unit = {
    this.synchronized {
      onChangeListeners.append(listener)
    }
  }

  private def updateIndexFieldMap(fList: WhiteListIndexFields): Unit = {
    // remove the fields from the map if they are not present in the newly provided whitelist set
    val indexableFieldNames = fList.fields.flatMap(field => field.aliases + field.name)

    indexFieldMap.values().removeIf((f: WhitelistIndexField) => !indexableFieldNames.contains(f.name))

    // add the fields in the map
    for(field <- fList.fields) {
      indexFieldMap.put(field.name, field)
      field.aliases.foreach(alias => indexFieldMap.put(alias, field))
    }
  }

  /**
    * detect if configuration has changed using the hashCode as version
    * @param newConfigData new configuration data
    * @return
    */
  private def hasConfigChanged(newConfigData: String): Boolean = newConfigData.hashCode != currentVersion

  /**
    * @return the whitelist index fields
    */
  def whitelistIndexFields: List[WhitelistIndexField] = indexFieldMap.values().asScala.toList

  def globalTraceContextIndexFieldNames: Set[String] = whitelistIndexFields.filter(_.searchContext == "trace").map(_.name).toSet
}
