/*
 *  Copyright 2018 Expedia, Inc.
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

package com.expedia.www.haystack.http.span.collector

import com.expedia.www.haystack.collector.commons.config.{ConfigurationLoader, ExternalKafkaConfiguration, ExtractorConfiguration, KafkaProduceConfiguration}
import com.expedia.www.haystack.http.span.collector.authenticator.{Authenticator, NoopAuthenticator}
import com.expedia.www.haystack.span.decorators.plugin.config.Plugin
import com.typesafe.config.Config

import scala.reflect.ClassTag

case class HttpConfiguration(host: String = "127.0.0.1", port: Int = 8080, authenticator: Authenticator = NoopAuthenticator)

object ProjectConfiguration {
  val config: Config = ConfigurationLoader.loadConfigFileWithEnvOverrides()

  def kafkaProducerConfig(): KafkaProduceConfiguration = ConfigurationLoader.kafkaProducerConfig(config)
  def extractorConfig(): ExtractorConfiguration = ConfigurationLoader.extractorConfiguration(config)
  def externalKafkaConfig(): List[ExternalKafkaConfiguration] = ConfigurationLoader.externalKafkaConfiguration(config)
  def additionalTagConfig(): Map[String, String] = ConfigurationLoader.additionalTagsConfiguration(config)
  def pluginConfiguration(): Plugin = ConfigurationLoader.pluginConfigurations(config)

  lazy val httpConfig: HttpConfiguration = {
    val authenticator = if(config.hasPath("http.authenticator")) {
      toInstance[Authenticator](config.getString("http.authenticator"))
    } else {
      NoopAuthenticator
    }

    // initialize the
    authenticator.init(config)

    HttpConfiguration(config.getString("http.host"), config.getInt("http.port"), authenticator)
  }

  private def toInstance[T](className: String)(implicit ct: ClassTag[T]): T = {
    val c = Class.forName(className)
    if (c == null) {
      throw new RuntimeException(s"No class found with name $className")
    } else {
      val o = c.newInstance()
      val baseClass = ct.runtimeClass

      if (!baseClass.isInstance(o)) {
        throw new RuntimeException(s"${c.getName} is not an instance of ${baseClass.getName}")
      }
      o.asInstanceOf[T]
    }
  }
}
