package com.expedia.www.haystack.commons.kstreams.app

import com.expedia.www.haystack.commons.health.HealthStatusController
import com.expedia.www.haystack.commons.unit.UnitTestSpec
import org.easymock.EasyMock._

class StateChangeListenerSpec extends UnitTestSpec {
  "StateChangeListener" should {
    "set the health status to healthy when requested" in {
      Given("a valid instance of StateChangeListener")
      val healthStatusController = mock[HealthStatusController]
      val stateChangeListener = new StateChangeListener(healthStatusController)
      When("set healthy is invoked")
      expecting {
        healthStatusController.setHealthy().once()
      }
      replay(healthStatusController)
      stateChangeListener.state(true)
      Then("it should set health status to healthy")
      verify(healthStatusController)
    }
    "set the health status to unhealthy when requested" in {
      Given("a valid instance of StateChangeListener")
      val healthStatusController = mock[HealthStatusController]
      val stateChangeListener = new StateChangeListener(healthStatusController)
      When("set unhealthy is invoked")
      expecting {
        healthStatusController.setUnhealthy().once()
      }
      replay(healthStatusController)
      stateChangeListener.state(false)
      Then("it should set health status to healthy")
      verify(healthStatusController)
    }
    "set application status to unhealthy when an un caught exception is raised" in {
      Given("a valid instance of StateChangeListener")
      val healthStatusController = mock[HealthStatusController]
      val stateChangeListener = new StateChangeListener(healthStatusController)
      val exception = new IllegalArgumentException
      val thread = new Thread("Thread-1")
      When("an uncaught exception is raised")
      expecting {
        healthStatusController.setUnhealthy().once()
      }
      replay(healthStatusController)
      stateChangeListener.uncaughtException(thread, exception)
      Then("it should set the status to unhealthy")
      verify(healthStatusController)
    }
  }

}
