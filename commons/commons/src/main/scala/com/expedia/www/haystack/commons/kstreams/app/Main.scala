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
package com.expedia.www.haystack.commons.kstreams.app

import java.util.concurrent.atomic.AtomicBoolean

import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.commons.logger.LoggerUtils
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import org.slf4j.LoggerFactory

/**
  * Starting point of a Kafka Streams application. One should extend this
  * trait and provide a valid instance of `StreamsRunner` by overriding
  * createStreamsRunner method to create and start a Kafka Streams application
  */
trait Main extends MetricsSupport {

  def main(args: Array[String]): Unit = {
    //create an instance of the application
    val jmxReporter: JmxReporter = JmxReporter.forRegistry(metricRegistry).build()
    val app = new Application(createStreamsRunner(), jmxReporter)

    //start the application
    app.start()

    //add a shutdown hook
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = app.stop()
    })
  }

  /**
    * This method should create and return a new instance of the `StreamsRunner` class
    * <p>That instance will be started and stopped as part of the application lifecycle
    * @return Instance of `StreamsRunner` to be managed
    */
  def createStreamsRunner(): StreamsRunner
}

/**
  * This is the main application class. This controls the application
  * start and shutdown actions
  *
  * @param streamsRunner instance of StreamsRunner to start and stop the
  *                      streams application
  */
class Application(streamsRunner: StreamsRunner, jmxReporter: JmxReporter) extends MetricsSupport {

  private val LOGGER = LoggerFactory.getLogger(classOf[Application])
  private val running = new AtomicBoolean(false)

  require(streamsRunner != null)
  require(jmxReporter != null)

  /**
    * Starts the given `StreamsRunner` and `JmxReporter` instances
    */
  def start(): Unit = {
    //start JMX reporter for metricRegistry
    jmxReporter.start()

    //start the topology
    streamsRunner.start()

    //initialized
    running.set(true)
  }

  /**
    * This method stops the given `StreamsRunner` and `JmxReporter` is they have been
    * previously started. If not, this method does nothing
    */
  def stop(): Unit = {
    if (running.getAndSet(false)) {
      LOGGER.info("Shutting down topology")
      streamsRunner.close()

      LOGGER.info("Shutting down jmxReporter")
      jmxReporter.close()

      LOGGER.info("Shutting down logger. Bye!")
      LoggerUtils.shutdownLogger()
    }
  }
}


