package com.expedia.www.haystack.service.graph.graph.builder

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

import com.expedia.www.haystack.commons.entities.{GraphEdge, GraphVertex, TagKeys}
import com.expedia.www.haystack.commons.health.HealthStatusController
import com.expedia.www.haystack.commons.kstreams.serde.graph.{GraphEdgeKeySerde, GraphEdgeValueSerde}
import com.expedia.www.haystack.service.graph.graph.builder.config.AppConfiguration
import com.expedia.www.haystack.service.graph.graph.builder.kafka.KafkaController
import com.expedia.www.haystack.service.graph.graph.builder.model.{EdgeStats, OperationGraph, ServiceGraph}
import com.expedia.www.haystack.service.graph.graph.builder.service.HttpService
import org.apache.http.client.fluent.Request
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyWindowStore}
import org.expedia.www.haystack.commons.scalatest.IntegrationSuite
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import org.scalatest.BeforeAndAfterAll

import scala.collection.JavaConverters._
import scala.util.Random

@IntegrationSuite
class AppSpec extends TestSpec with BeforeAndAfterAll {

  val kafkaController: KafkaController = createKafkaController()
  private val appConfig = new AppConfiguration("integration/local.conf")
  var stream: KafkaStreams = _
  var service: HttpService = _

  implicit val formats = DefaultFormats

  override def beforeAll {
    //start kafka and zk
    kafkaController.startService()

    //ensure test topics are present
    kafkaController.createTopics(List(appConfig.kafkaConfig.consumerTopic))

    //start topology
    stream = App.createStream(appConfig.kafkaConfig, new HealthStatusController)
    stream.start()

    //start service
    service = App.createService(appConfig.serviceConfig, stream, appConfig.kafkaConfig)
    service.start()

    //time for kstreams to initialize completely
    Thread.sleep(20000)
  }

  describe("graph-builder application") {
    it("should add new edges in ktable") {
      Given("running stream topology")

      When("getting new edges")
      //send test data to source topic
      val producer = kafkaController.createProducer(
        appConfig.kafkaConfig.consumerTopic,
        new GraphEdgeKeySerde().serializer(), new GraphEdgeValueSerde().serializer()
      )

      val random = new Random
      val source = random.nextString(4)
      val destination = random.nextString(4)
      val operation = random.nextString(4)
      val time = System.currentTimeMillis()

      //send sample data
      produceRecord(producer, source, destination, operation, time)

      Then("edges should be added to edges ktable")
      //read data from ktable to validate
      val store: ReadOnlyWindowStore[GraphEdge, EdgeStats] =
      stream.store(appConfig.kafkaConfig.producerTopic, QueryableStoreTypes.windowStore[GraphEdge, EdgeStats]())

      val storeIterator = store.all()
      val filteredEdges = storeIterator.asScala.toList.filter(
        edge => {
          val gEdge = edge.key.key
          gEdge.source == GraphVertex(source) && gEdge.destination == GraphVertex(destination) && gEdge.operation == operation && gEdge.sourceTimestamp == 0
        })

      filteredEdges.length should be(1)
      filteredEdges.head.value.count should be(1)
    }

    it("should add only one row for duplicate edges in ktable") {
      Given("running stream topology")

      When("getting duplicate edges")
      //send test data to source topic
      val producer = kafkaController.createProducer(
        appConfig.kafkaConfig.consumerTopic,
        new GraphEdgeKeySerde().serializer(), new GraphEdgeValueSerde().serializer())

      val random = new Random
      val source = random.nextString(4)
      val destination = random.nextString(4)
      val operation = random.nextString(4)
      val time = System.currentTimeMillis()

      //send sample data
      produceDuplicateRecord(producer, 3, source, destination, operation, time)

      Then("only one edge should be added to edges ktable")
      //read data from ktable to validate
      val store: ReadOnlyWindowStore[GraphEdge, EdgeStats] =
        stream.store(appConfig.kafkaConfig.producerTopic, QueryableStoreTypes.windowStore[GraphEdge, EdgeStats]())

      val storeIterator = store.all()
      val filteredEdges = storeIterator.asScala.toList.filter(
        edge => {
          val gEdge = edge.key.key
          gEdge.source == GraphVertex(source) && gEdge.destination == GraphVertex(destination) && gEdge.operation == operation && gEdge.sourceTimestamp == 0
        })

      filteredEdges.length should be(1)
      filteredEdges.head.value.count should be(3)
    }

    it("should make servicegraph queriable through http") {
      Given("running stream topology")

      When("getting new edge")
      //send test data to source topic
      val producer = kafkaController.createProducer(
        appConfig.kafkaConfig.consumerTopic,
        new GraphEdgeKeySerde().serializer(), new GraphEdgeValueSerde().serializer())
      val random = new Random
      val source = random.nextInt().toString
      val destination = random.nextInt().toString
      val operation = random.nextString(4)
      val time = System.currentTimeMillis()

      //send sample data
      produceRecord(producer, source, destination, operation, time, Map("tag1" -> "testtagval1", TagKeys.ERROR_KEY -> "true"))

      Then("servicegraph endpoint should return the new edge")
      val edgeJson = Request
        .Get(s"http://localhost:${appConfig.serviceConfig.http.port}/servicegraph")
        .execute()
        .returnContent()
        .asString()

      val serviceGraph = Serialization.read[ServiceGraph](edgeJson)
      val filteredEdges = serviceGraph.edges.filter(
        edge => edge.source.name == source && edge.destination.name == destination)

      filteredEdges.length should be(1)
      filteredEdges.head.stats.count shouldBe 1
      filteredEdges.head.stats.errorCount shouldBe 1
      filteredEdges.head.source.tags.size should be(1)
      filteredEdges.head.source.tags.get("tag1") should be (Some("testtagval1"))
    }

    it("should make operationgraph queriable through http") {
      Given("running stream topology")

      When("getting new edge")
      //send test data to source topic
      val producer = kafkaController.createProducer(
        appConfig.kafkaConfig.consumerTopic,
        new GraphEdgeKeySerde().serializer(), new GraphEdgeValueSerde().serializer())
      val random = new Random
      val source = random.nextInt().toString
      val destination = random.nextInt().toString
      val operation = random.nextInt().toString
      val time = System.currentTimeMillis()

      //send sample data
      produceRecord(producer, source, destination, operation, time)

      Then("operationgraph endpoint should return the new edge")
      val edgeJson = Request
        .Get(s"http://localhost:${appConfig.serviceConfig.http.port}/operationgraph")
        .execute()
        .returnContent()
        .asString()

      val operationGraph = Serialization.read[OperationGraph](edgeJson)
      val filteredEdges = operationGraph.edges.filter(
        edge => edge.source == source && edge.destination == destination && edge.operation == operation)

      filteredEdges.length should be(1)
    }
  }

  override def afterAll {
    //stop service & topology
    service.close()
    stream.close()

    //stop kafka and zk
    kafkaController.stopService()
  }

  private def createKafkaController(): KafkaController = {
    val zkProperties = new Properties
    zkProperties.load(classOf[AppSpec].getClassLoader.getResourceAsStream("integration/zookeeper.properties"))

    val kafkaProperties = new Properties
    kafkaProperties.load(classOf[AppSpec].getClassLoader.getResourceAsStream("integration/kafka-server.properties"))

    new KafkaController(kafkaProperties, zkProperties)
  }

  private def produceRecord(producer: KafkaProducer[GraphEdge, GraphEdge], source: String, destination: String,
                            operation: String, time: Long, sourceEdgetags: Map[String, String] = Map()): Unit = {
    sendRecord(producer, source, destination, operation, time, sourceEdgetags)

    // flush and sleep for couple of seconds for streams to process
    producer.flush()
    Thread.sleep(2000)
  }

  private def produceDuplicateRecord(producer: KafkaProducer[GraphEdge, GraphEdge], count: Int, source: String, destination: String, operation: String, time: Long): Unit = {
    for (i <- 0 until count) sendRecord(producer, source, destination, operation, time)

    // flush and sleep for couple of seconds for streams to process
    producer.flush()
    Thread.sleep(2000)
  }

  private def sendRecord(producer: KafkaProducer[GraphEdge, GraphEdge], source: String, destination: String,
                         operation: String, time: Long, sourceEdgeTags: Map[String, String] = Map()): Unit = {
    val edge = GraphEdge(GraphVertex(source, sourceEdgeTags), GraphVertex(destination), operation, time)
    producer.send(new ProducerRecord[GraphEdge, GraphEdge](appConfig.kafkaConfig.consumerTopic, edge, edge))
  }
}
