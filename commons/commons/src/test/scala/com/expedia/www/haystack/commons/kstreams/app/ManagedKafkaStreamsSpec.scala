package com.expedia.www.haystack.commons.kstreams.app

import java.util.concurrent.TimeUnit

import com.expedia.www.haystack.commons.unit.UnitTestSpec
import org.apache.kafka.streams.KafkaStreams
import org.easymock.EasyMock._

class ManagedKafkaStreamsSpec extends UnitTestSpec {
  "ManagedKafkaStreams" should {
    "start the underlying kafkaStreams when started" in {
      Given("a fully configured ManagedKafkaStreams instance")
      val kafkaStreams = mock[KafkaStreams]
      val managedKafkaStreams = new ManagedKafkaStreams(kafkaStreams)
      When("start is invoked")
      expecting {
        kafkaStreams.start().once()
      }
      replay(kafkaStreams)
      managedKafkaStreams.start()
      Then("it should start the KafkaStreams application")
      verify(kafkaStreams)
    }
    "close the KafkaStreams when stopped" in {
      Given("a fully configured ManagedKafkaStreams instance")
      val kafkaStreams = mock[KafkaStreams]
      val managedKafkaStreams = new ManagedKafkaStreams(kafkaStreams)
      When("stop is invoked")
      expecting {
        kafkaStreams.start().once()
        kafkaStreams.close(0, TimeUnit.SECONDS).andReturn(true).once()
      }
      replay(kafkaStreams)
      managedKafkaStreams.start()
      Then("it should close the KafkaStreams application")
      managedKafkaStreams.stop()
      verify(kafkaStreams)
    }
    "not do anything when stop is called without starting" in  {
      Given("a fully configured ManagedKafkaStreams instance")
      val kafkaStreams = mock[KafkaStreams]
      val managedKafkaStreams = new ManagedKafkaStreams(kafkaStreams)
      When("stop is invoked without starting")
      replay(kafkaStreams)
      Then("it should do nothing")
      managedKafkaStreams.stop()
      verify(kafkaStreams)
    }
    "close the KafkaStreams with the given timeout when stopped" in {
      Given("a fully configured ManagedKafkaStreams instance with a timeout")
      val kafkaStreams = mock[KafkaStreams]
      val managedKafkaStreams = new ManagedKafkaStreams(kafkaStreams, 5)
      When("stop is invoked")
      expecting {
        kafkaStreams.start().once()
        kafkaStreams.close(5, TimeUnit.SECONDS).andReturn(true).once()
      }
      replay(kafkaStreams)
      managedKafkaStreams.start()
      Then("it should close the KafkaStreams application with the given timeout")
      managedKafkaStreams.stop()
      verify(kafkaStreams)
    }
  }

}
