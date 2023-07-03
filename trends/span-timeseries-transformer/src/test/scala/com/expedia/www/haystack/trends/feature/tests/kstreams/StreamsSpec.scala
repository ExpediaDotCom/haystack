package com.expedia.www.haystack.trends.feature.tests.kstreams

import com.expedia.www.haystack.trends.Streams
import com.expedia.www.haystack.trends.feature.FeatureSpec
import org.apache.kafka.streams.StreamsBuilder


class StreamsSpec extends FeatureSpec {

  feature("Streams should build a topology") {

    scenario("a valid kafka configuration") {

      Given("an valid kafka configuration")

      val appConfig = mockAppConfig
      val streams = new Streams(appConfig.kafkaConfig, appConfig.transformerConfiguration)
      val streamBuilder = mock[StreamsBuilder]


      When("the stream topology is built")
      val topology = streams.get()

      Then("it should be able to build a successful topology")
      topology should not be null

      Then("then it should return an empty state store")
      topology.describe().globalStores().isEmpty shouldBe true
    }
  }
}
