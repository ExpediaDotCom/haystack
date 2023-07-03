package com.expedia.www.haystack.service.graph.node.finder
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
import java.util.Properties

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.TestSpec
import com.expedia.www.haystack.commons.health.HealthStatusController
import com.expedia.www.haystack.commons.kafka.{InvalidStateException, KafkaController}
import com.expedia.www.haystack.commons.kstreams.app.StateChangeListener
import com.expedia.www.haystack.commons.kstreams.serde.SpanSerializer
import com.expedia.www.haystack.commons.kstreams.serde.metricdata.MetricDataDeserializer
import com.expedia.www.haystack.service.graph.node.finder.config.AppConfiguration
import com.expedia.www.haystack.service.graph.node.finder.utils.SpanUtils
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.streams.KafkaStreams
import org.expedia.www.haystack.commons.scalatest.IntegrationSuite
import org.scalatest.BeforeAndAfter
import org.slf4j.LoggerFactory

@IntegrationSuite
class AppSpec extends TestSpec with BeforeAndAfter {

  private val LOGGER = LoggerFactory.getLogger(classOf[AppSpec])

  private val appConfig = new AppConfiguration("integration/local.conf")
  private val stateChangeListener = new ExtendedStateChangeListener(new HealthStatusController)
  private val streamsRunner = App.createStreamsRunner(appConfig, stateChangeListener)

  val kafkaController: KafkaController = createKafkaController()

  before {
    //start kafka and zk
    kafkaController.startService()

    //ensure test topics are present
    kafkaController.createTopics(List(appConfig.kafkaConfig.protoSpanTopic,
      appConfig.kafkaConfig.serviceCallTopic, appConfig.kafkaConfig.metricsTopic, appConfig.kafkaConfig.metadataConfig.topic))

    //start topology
    streamsRunner.start()

    //time for kstreams to initialize completely·
    waitForStreams()
  }

  describe("node finder application") {
    it("should process spans from kafka and produce latency metrics and graph edges") {
      //send test data to source topic
      val producer = kafkaController.createProducer(appConfig.kafkaConfig.protoSpanTopic,
        classOf[StringSerializer], classOf[SpanSerializer])

      //send sample data
      sendRecords(producer, 5)

      //read data from output topics
      LOGGER.info(s"Consuming topics ${appConfig.kafkaConfig.metricsTopic} and ${appConfig.kafkaConfig.serviceCallTopic}")
      val metricsConsumer = kafkaController.createConsumer(appConfig.kafkaConfig.metricsTopic,
        classOf[StringDeserializer], classOf[MetricDataDeserializer])
      val metricRecords = metricsConsumer.poll(5000)

      val graphConsumer = kafkaController.createConsumer(appConfig.kafkaConfig.serviceCallTopic,
        classOf[StringDeserializer], classOf[StringDeserializer])
      val graphRecords = graphConsumer.poll(5000)

      //check if they are as expected
      metricRecords.count() should be(5)
      graphRecords.count() should be(5)
    }
  }

  after {
    //stop topology
    streamsRunner.close()

    //stop kafka and zk
    kafkaController.stopService()
  }

  private def createKafkaController() : KafkaController = {
    val zkProperties = new Properties
    zkProperties.load(classOf[AppSpec].getClassLoader.getResourceAsStream("integration/zookeeper.properties"))

    val kafkaProperties = new Properties
    kafkaProperties.load(classOf[AppSpec].getClassLoader.getResourceAsStream("integration/kafka-server.properties"))

    new KafkaController(kafkaProperties, zkProperties)
  }

  private def waitForStreams(): Unit = {
    while (!stateChangeListener.currentState.isRunning &&
      (stateChangeListener.currentState == KafkaStreams.State.CREATED)) Thread.sleep(100)

    if (!stateChangeListener.currentState.isRunning) {
      throw new InvalidStateException(stateChangeListener.currentState + " is not expected after startup")
    }
  }

  private def sendRecords(producer: KafkaProducer[String, Span], count: Int) : Unit =  {
    val writer: (Span) => Unit = span => {
      producer.send(new ProducerRecord[String, Span](appConfig.kafkaConfig.protoSpanTopic, span.getSpanId, span))
      LOGGER.info("sent {} span {} : {}", SpanUtils.getSpanType(span).toString, span.getSpanId, span.getStartTime.toString)
    }

    //send 5 simple spans, 5 client spans, 5 server span and 5 client-server span combinations
    for (_ <- 1 to count) produceClientSpan(10000, writer)
    for (_ <- 1 to count) produceServerSpan(9000, writer)
    for (_ <- 1 to count) produceClientAndServerSpans(8000, writer)
    for (_ <- 1 to count) produceSimpleSpan(5000, writer)
    producer.flush()

    //sleep for 30 seconds for streams to process. This is probably too much for local -
    //but depending on the compute in build servers this time varies
    Thread.sleep(30000)
  }

  class ExtendedStateChangeListener(healthStatusController: HealthStatusController)
    extends StateChangeListener(healthStatusController) {
    var currentState: KafkaStreams.State = _

    override def onChange(newState: KafkaStreams.State, oldState: KafkaStreams.State): Unit = {
      super.onChange(newState, oldState)
      currentState = newState
    }
  }
}
