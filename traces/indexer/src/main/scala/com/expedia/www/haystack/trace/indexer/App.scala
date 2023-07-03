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

package com.expedia.www.haystack.trace.indexer

import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.commons.health.{HealthController, UpdateHealthStatusFile}
import com.expedia.www.haystack.commons.logger.LoggerUtils
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trace.indexer.config.ProjectConfiguration
import org.slf4j.LoggerFactory

object App extends MetricsSupport {
  private val LOGGER = LoggerFactory.getLogger(App.getClass)

  private var stream: StreamRunner = _
  private var appConfig: ProjectConfiguration = _

  def main(args: Array[String]): Unit = {
    startJmxReporter()

    try {
      appConfig = new ProjectConfiguration

      HealthController.addListener(new UpdateHealthStatusFile(appConfig.healthStatusFilePath))

      stream = new StreamRunner(
        appConfig.kafkaConfig,
        appConfig.spanAccumulateConfig,
        appConfig.elasticSearchConfig,
        appConfig.backendConfig,
        appConfig.serviceMetadataWriteConfig,
        appConfig.indexConfig)

      Runtime.getRuntime.addShutdownHook(new Thread {
        override def run(): Unit = {
          LOGGER.info("Shutdown hook is invoked, tearing down the application.")
          shutdown()
        }
      })

      stream.start()

      // mark the status of app as 'healthy'
      HealthController.setHealthy()
    } catch {
      case ex: Exception =>
        LOGGER.error("Observed fatal exception while running the app", ex)
        shutdown()
        System.exit(1)
    }
  }

  private def shutdown(): Unit = {
    if(stream != null) stream.close()
    if(appConfig != null) appConfig.close()
    LoggerUtils.shutdownLogger()
  }

  private def startJmxReporter() = {
    val jmxReporter = JmxReporter.forRegistry(metricRegistry).build()
    jmxReporter.start()
  }
}
