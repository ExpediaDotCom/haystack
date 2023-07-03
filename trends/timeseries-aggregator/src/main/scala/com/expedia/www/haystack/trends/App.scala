/*
 *
 *     Copyright 2017 Expedia, Inc.
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

package com.expedia.www.haystack.trends

import java.util.function.Supplier

import com.expedia.www.haystack.commons.health.{HealthStatusController, UpdateHealthStatusFile}
import com.expedia.www.haystack.commons.kstreams.app.{Main, StateChangeListener, StreamsFactory, StreamsRunner}
import com.expedia.www.haystack.trends.config.AppConfiguration
import com.expedia.www.haystack.trends.kstream.Streams
import com.netflix.servo.util.VisibleForTesting
import org.apache.kafka.streams.Topology


object App extends Main {

  /**
    * Creates a valid instance of StreamsRunner.
    *
    * StreamsRunner is created with a valid StreamsFactory instance and a listener that observes
    * state changes of the kstreams application.
    *
    * StreamsFactory in turn is created with a Topology Supplier and kafka.StreamsConfig. Any failure in
    * StreamsFactory is gracefully handled by StreamsRunner to shut the application off
    *
    * Core logic of this application is in the `Streams` instance - which is a topology supplier. The
    * topology of this application is built in this class.
    *
    * @return A valid instance of `StreamsRunner`
    */

  override def createStreamsRunner(): StreamsRunner = {
    val ProjectConfiguration = new AppConfiguration()

    val healthStatusController = new HealthStatusController
    healthStatusController.addListener(new UpdateHealthStatusFile(ProjectConfiguration.healthStatusFilePath))

    val stateChangeListener = new StateChangeListener(healthStatusController)

    createStreamsRunner(ProjectConfiguration, stateChangeListener)
  }

  @VisibleForTesting
  private[trends] def createStreamsRunner(ProjectConfiguration: AppConfiguration,
                                          stateChangeListener: StateChangeListener): StreamsRunner = {
    //create the topology provider
    val kafkaConfig = ProjectConfiguration.kafkaConfig
    val streams: Supplier[Topology] = new Streams(ProjectConfiguration)

    val streamsFactory = new StreamsFactory(streams, kafkaConfig.streamsConfig, kafkaConfig.consumeTopic)

    new StreamsRunner(streamsFactory, stateChangeListener)
  }
}







