package com.expedia.www.haystack.trends.feature.tests.kstreams.store

import com.expedia.www.haystack.trends.aggregation.TrendMetric
import com.expedia.www.haystack.trends.feature.FeatureSpec
import com.expedia.www.haystack.trends.kstream.store.HaystackStoreBuilder
import org.apache.kafka.streams.state.internals.{InMemoryKeyValueLoggedStore, MemoryNavigableLRUCache, MeteredKeyValueStore}

class HaystackStoreBuilderSpec extends FeatureSpec {

  feature("Haystack Store Builder should build appropriate store for haystack metrics") {

    scenario("build store with changelog enabled") {

      Given("a haystack store builder")
      val storeName = "test-store"
      val cacheSize = 100
      val storeBuilder = new HaystackStoreBuilder(storeName, cacheSize)

      When("change logging is enabled")
      storeBuilder.withCachingEnabled()
      val store = storeBuilder.build()

      Then("it should build a metered lru-cache based changelogging store")
      store.isInstanceOf[MeteredKeyValueStore[String, TrendMetric]] shouldBe true
    }
  }
}
