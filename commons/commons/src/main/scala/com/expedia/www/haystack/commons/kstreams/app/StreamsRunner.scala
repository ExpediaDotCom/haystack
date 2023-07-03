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

import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

class StreamsRunner(streamsFactory: StreamsFactory, stateChangeListener: StateChangeListener) extends AutoCloseable {

  private val LOGGER = LoggerFactory.getLogger(classOf[StreamsRunner])
  private var managedStreams : ManagedService = _

  require(streamsFactory != null, "valid streamsFactory is required")
  require(stateChangeListener != null, "valid stateChangeListener is required")

  def start(): Unit = {
    LOGGER.info("Starting the given topology.")

    Try(streamsFactory.create(stateChangeListener)) match {
      case Success(streams) =>
        managedStreams = streams
        managedStreams.start()
        stateChangeListener.state(true)
        LOGGER.info("KafkaStreams started successfully")
      case Failure(e) =>
        LOGGER.error(s"KafkaStreams failed to start : ${e.getMessage}", e)
        stateChangeListener.state(false)
    }
  }

  def close(): Unit = {
    if (managedStreams != null) {
      managedStreams.stop()
    }
  }
}


