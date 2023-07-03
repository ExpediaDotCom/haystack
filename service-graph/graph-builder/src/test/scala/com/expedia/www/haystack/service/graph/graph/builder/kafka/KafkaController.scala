package com.expedia.www.haystack.service.graph.graph.builder.kafka

import java.util.Properties
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import kafka.server.RunningAsBroker
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.Try

class KafkaController(kafkaProperties: Properties, zooKeeperProperties: Properties) {
  require(kafkaProperties != null)
  require(zooKeeperProperties != null)

  private val LOGGER = LoggerFactory.getLogger(classOf[KafkaController])

  private val zkPort = zooKeeperProperties.getProperty("clientPort").toInt
  private val kafkaPort = kafkaProperties.getProperty("port").toInt

  lazy val zkUrl: String = "localhost:" + zkPort
  lazy val kafkaUrl: String = "localhost:" + kafkaPort

  private val kafkaPropertiesWithZk = new Properties
  kafkaPropertiesWithZk.putAll(kafkaProperties)
  kafkaPropertiesWithZk.put("zookeeper.connect", zkUrl)
  private val kafkaServer = new KafkaLocal(kafkaPropertiesWithZk)

  def startService(): Unit = {
    //start zk
    val zookeeper = new ZooKeeperLocal(zooKeeperProperties)
    new Thread(zookeeper).start()
    Thread.sleep(2000)

    //start kafka
    kafkaServer.start()
    Thread.sleep(2000)

    // check kafka status
    if (kafkaServer.state().currentState != RunningAsBroker.state) {
      throw new IllegalStateException("Kafka server is not in a running state")
    }

    //lifecycle message
    LOGGER.info("Kafka started and listening : {}", kafkaUrl)
  }

  def stopService(): Unit = {
    //stop kafka
    kafkaServer.stop()

    //lifecycle message
    LOGGER.info("Kafka stopped")
  }

  def createTopics(topics: List[String]): Unit = {
    if (topics.nonEmpty) {
      val adminClient = AdminClient.create(getBootstrapProperties)
      try {
        adminClient.createTopics(topics.map(topic => new NewTopic(topic, 1, 1)).asJava)
        adminClient.listTopics().names().get().forEach(s => LOGGER.info("Available topic : {}", s))
      }
      finally {
        Try(adminClient.close(5, TimeUnit.SECONDS))
      }
    }
  }

  def createProducer[K, V] (topic: String, keySerializer: Serializer[K],
                            valueSerializer: Serializer[V]) : KafkaProducer[K, V] = {
    val properties = getBootstrapProperties
    properties.put(ProducerConfig.CLIENT_ID_CONFIG, topic + "Producer")
    new KafkaProducer[K, V](properties, keySerializer, valueSerializer)
  }

  private def getBootstrapProperties: Properties = {
    val properties = new Properties()
    properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, List(kafkaUrl).asJava)
    properties
  }
}

class InvalidStateException(message: String) extends RuntimeException(message) {}
