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

package com.expedia.www.haystack.kinesis.span.collector

import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.collector.commons.MetricsSupport
import com.expedia.www.haystack.collector.commons.health.{HealthController, UpdateHealthStatusFile}
import com.expedia.www.haystack.collector.commons.logger.LoggerUtils
import com.expedia.www.haystack.kinesis.span.collector.config.ProjectConfiguration
import com.expedia.www.haystack.kinesis.span.collector.pipeline.KinesisToKafkaPipeline
import org.slf4j.LoggerFactory

object App extends MetricsSupport {
  private val LOGGER = LoggerFactory.getLogger(App.getClass)

  private var pipeline: KinesisToKafkaPipeline = _
  private var jmxReporter: JmxReporter = _

  def main(args: Array[String]): Unit = {
    startJmxReporter()

    addShutdownHook()

    import ProjectConfiguration._
    try {

      healthStatusFile().foreach(statusFile => HealthController.addListener(new UpdateHealthStatusFile(statusFile)))

      pipeline = new KinesisToKafkaPipeline(kafkaProducerConfig(), externalKafkaConfig(), kinesisConsumerConfig(), extractorConfiguration(), additionalTagConfig(), pluginConfiguration())
      pipeline.run()
    } catch {
      case ex: Exception =>
        LOGGER.error("Observed fatal exception while running the app", ex)
        shutdown()
        System.exit(1)
    }
  }

  private def addShutdownHook(): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = {
        LOGGER.info("Shutdown hook is invoked, tearing down the application.")
        shutdown()
      }
    }))
  }

  private def shutdown(): Unit = {
    if (pipeline != null) pipeline.close()
    if (jmxReporter != null) jmxReporter.stop()
    LoggerUtils.shutdownLogger()
  }

  private def startJmxReporter() = {
    jmxReporter = JmxReporter.forRegistry(metricRegistry).build()
    jmxReporter.start()
  }
}
