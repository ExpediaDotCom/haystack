package com.expedia.www.haystack.commons.kafka

import java.util.Properties

import kafka.metrics.KafkaMetricsReporter
import kafka.server.{BrokerState, KafkaConfig, KafkaServer}

class KafkaLocal(val kafkaProperties: Properties) {
  val kafkaConfig: KafkaConfig = KafkaConfig.fromProps(kafkaProperties)
  val kafka: KafkaServer = new KafkaServer(kafkaConfig, kafkaMetricsReporters = List[KafkaMetricsReporter]())

  def start(): Unit = {
    kafka.startup()
  }

  def stop(): Unit = {
    kafka.shutdown()
  }

  def state(): BrokerState = {
    kafka.brokerState
  }
}
