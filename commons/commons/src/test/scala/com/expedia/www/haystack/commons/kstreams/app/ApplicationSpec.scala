package com.expedia.www.haystack.commons.kstreams.app

import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.commons.unit.UnitTestSpec
import org.easymock.EasyMock._

class ApplicationSpec extends UnitTestSpec {

  "Application" should {

    "require an instance of StreamsRunner" in {
      Given("only a valid instance of jmxReporter")
      val streamsRunner : StreamsRunner = null
      val jmxReporter = mock[JmxReporter]
      When("an instance of Application is created")
      Then("it should throw an exception")
      intercept[IllegalArgumentException] {
        new Application(streamsRunner, jmxReporter)
      }
    }
    "require an instance of JmxReporter" in {
      Given("only a valid instance of StreamsRunner")
      val streamsRunner : StreamsRunner = mock[StreamsRunner]
      val jmxReporter = null
      When("an instance of Application is created")
      Then("it should throw an exception")
      intercept[IllegalArgumentException] {
        new Application(streamsRunner, jmxReporter)
      }
    }
    "start both JmxReporter and StreamsRunner at start" in {
      Given("a fully configured application")
      val streamsRunner : StreamsRunner = mock[StreamsRunner]
      val jmxReporter = mock[JmxReporter]
      val application = new Application(streamsRunner, jmxReporter)
      When("application is started")
      expecting {
        streamsRunner.start().once()
        jmxReporter.start().once()
      }
      replay(streamsRunner, jmxReporter)
      application.start()
      Then("it should call start on both streamsRunner and jmxReporter")
      verify(streamsRunner, jmxReporter)
    }
    "close both JmxReporter and StreamsRunner at stop" in {
      Given("a fully configured and running application")
      val streamsRunner : StreamsRunner = mock[StreamsRunner]
      val jmxReporter = mock[JmxReporter]
      val application = new Application(streamsRunner, jmxReporter)
      When("application is stopped")
      expecting {
        streamsRunner.start().once()
        jmxReporter.start().once()
        streamsRunner.close().once()
        jmxReporter.close().once()
      }
      replay(streamsRunner, jmxReporter)
      application.start()
      application.stop()
      Then("it should call close on both streamsRunner and jmxReporter")
      verify(streamsRunner, jmxReporter)
    }

  }
}
