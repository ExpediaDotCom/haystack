package com.expedia.www.haystack.commons.kstreams.app

import com.expedia.www.haystack.commons.health.HealthStatusController
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KafkaStreams.StateListener
import org.slf4j.LoggerFactory

/**
  * Watches the state of a KafkaStreams application and sets the health of the process
  * using the provided `HealthStatusController` instance
  * @param healthStatusController required instance of `HealthStatusController` that manages
  *                               the state of the current process
  */
class StateChangeListener(healthStatusController: HealthStatusController) extends StateListener
  with Thread.UncaughtExceptionHandler {

  require(healthStatusController != null)

  private val LOGGER = LoggerFactory.getLogger(classOf[StateChangeListener])

  /**
    * Method to set the status of the application
    * @param healthy sets the state as healthy if this is true and unhealthy if the state is false
    */
  def state(healthy : Boolean) : Unit =
    if (healthy) {
      healthStatusController.setHealthy()
    }
    else {
      healthStatusController.setUnhealthy()
    }

  /**
    * This method is called when state of the KafkaStreams application changes.
    *
    * @param newState new state
    * @param oldState previous state
    */
  override def onChange(newState: KafkaStreams.State, oldState: KafkaStreams.State): Unit = {
    LOGGER.info(s"State change event called with newState=$newState and oldState=$oldState")
  }

  /**
    * This method is invoked when the given thread terminates due to the
    * given uncaught exception.
    * @param t the thread that had an unhandled exception
    * @param e the exception that caused the thread to terminate
    */
  override def uncaughtException(t: Thread, e: Throwable): Unit = {
    LOGGER.error(s"uncaught exception occurred running kafka streams for thread=${t.getName}", e)
    state(false)
  }
}
