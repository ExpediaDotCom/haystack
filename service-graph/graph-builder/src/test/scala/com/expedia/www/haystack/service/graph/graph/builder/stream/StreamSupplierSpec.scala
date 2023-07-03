package com.expedia.www.haystack.service.graph.graph.builder.stream

import java.util
import java.util.Collections
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

import com.expedia.www.haystack.commons.health.HealthStatusController
import org.apache.kafka.clients.admin.{AdminClient, ListTopicsResult}
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.streams.{StreamsConfig, Topology}
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}

class StreamSupplierSpec extends FunSpec with Matchers with MockitoSugar {
  private val ConsumerTopic = "ConsumerTopic"

  private val topologySupplier = mock[Supplier[Topology]]
  private val healthController = mock[HealthStatusController]
  private val streamsConfig = mock[StreamsConfig]

  describe("StreamSupplier constructor") {
    it ("should require the topologySupplier argument to be non-null") {
      an [IllegalArgumentException] should be thrownBy
        new StreamSupplier(null, healthController, streamsConfig, ConsumerTopic)
    }
    it ("should require the healthController argument to be non-null") {
      an [IllegalArgumentException] should be thrownBy
        new StreamSupplier(topologySupplier, null, streamsConfig, ConsumerTopic)
    }
    it ("should require the streamsConfig argument to be non-null") {
      an [IllegalArgumentException] should be thrownBy
        new StreamSupplier(topologySupplier, healthController, null, ConsumerTopic)
    }
    it ("should require the consumerTopic argument to be non-null") {
      an [IllegalArgumentException] should be thrownBy
        new StreamSupplier(topologySupplier, healthController, streamsConfig, null)
    }
    verifyNoMoreInteractions(topologySupplier, healthController, streamsConfig)
  }

  private val adminClient = mock[AdminClient]
  private val listTopicsResult: ListTopicsResult = mock[ListTopicsResult]
  private val kafkaFuture: KafkaFuture[util.Set[String]] = mock[KafkaFuture[util.Set[String]]]

  describe("StreamSupplier.get()") {
    it("should throw an exception if the consumer topic does exist") {
      when(adminClient.listTopics()).thenReturn(listTopicsResult)
      when(listTopicsResult.names()).thenReturn(kafkaFuture)
      val nonExistentTopic = "NonExistent" + ConsumerTopic
      when(kafkaFuture.get()).thenReturn(Collections.singleton(ConsumerTopic))

      val streamSupplier = new StreamSupplier(topologySupplier, healthController, streamsConfig, nonExistentTopic, adminClient)
      val thrown = the [streamSupplier.TopicNotPresentException] thrownBy streamSupplier.get
      thrown.getTopic shouldEqual nonExistentTopic

      verify(adminClient).listTopics()
      verify(listTopicsResult).names()
      verify(kafkaFuture).get()
      verify(adminClient).close(5, TimeUnit.SECONDS)
      verifyNoMoreInteractions(topologySupplier, healthController, streamsConfig, adminClient, listTopicsResult, kafkaFuture)
      reset(topologySupplier, healthController, streamsConfig, adminClient, listTopicsResult, kafkaFuture)
    }
  }
}
