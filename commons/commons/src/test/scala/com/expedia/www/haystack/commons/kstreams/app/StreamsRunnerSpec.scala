package com.expedia.www.haystack.commons.kstreams.app

import com.expedia.www.haystack.commons.unit.UnitTestSpec
import org.easymock.EasyMock._

class StreamsRunnerSpec extends UnitTestSpec {
  "StreamsRunner" should {
    "start managed KStreams when the factory successfully creates one" in {
      Given("a StreamsFactory")
      val factory = mock[StreamsFactory]
      And("a StateChangeListener")
      val stateChangeListener = mock[StateChangeListener]
      val managedService = mock[ManagedService]
      val streamsRunner = new StreamsRunner(factory, stateChangeListener)
      When("streamsRunner is asked to start the application")
      expecting {
        factory.create(stateChangeListener).andReturn(managedService).once()
        managedService.start().once()
        stateChangeListener.state(true).once()
      }
      replay(factory, managedService, stateChangeListener)
      streamsRunner.start()
      Then("it should create an instance of managed streams from the given factory and start it. " +
        "It should also set the state to healthy")
      verify(factory, managedService, stateChangeListener)
    }
    "set the state to unhealthy when the factory fails to create one" in {
      Given("a StreamsFactory")
      val factory = mock[StreamsFactory]
      And("a StateChangeListener")
      val stateChangeListener = mock[StateChangeListener]
      val managedService = mock[ManagedService]
      val streamsRunner = new StreamsRunner(factory, stateChangeListener)
      When("streamsRunner is asked to start the application and factory fails")
      expecting {
        factory.create(stateChangeListener).andThrow(new RuntimeException).once()
        stateChangeListener.state(false).once()
      }
      replay(factory, managedService, stateChangeListener)
      streamsRunner.start()
      Then("it should attempt tp create an instance of managed streams from the given factory. " +
        "It should also set the state to unhealthy")
      verify(factory, managedService, stateChangeListener)
    }
  }
}
