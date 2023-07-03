package com.expedia.www.haystack.service.graph.graph.builder

import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.commons.kstreams.app.ManagedService
import com.expedia.www.haystack.commons.logger.LoggerUtils
import com.expedia.www.haystack.service.graph.graph.builder.ManagedApplication._
import org.slf4j.Logger

import scala.util.Try

object ManagedApplication {
  val StartMessage = "Starting the given topology and service"
  val HttpStartMessage = "HTTP service started successfully"
  val StreamStartMessage = "Kafka stream started successfully"
  val HttpStopMessage = "Shutting down HTTP service"
  val StreamStopMessage = "Shutting down Kafka stream"
  val JmxReporterStopMessage = "Shutting down JMX Reporter"
  val LoggerStopMessage = "Shutting down logger. Bye!"
}

class ManagedApplication(service: ManagedService, stream: ManagedService, jmxReporter: JmxReporter, logger: Logger) {

  require(service != null)
  require(stream != null)
  require(jmxReporter != null)
  require(logger != null)

  def start(): Unit = {
    try {
      jmxReporter.start()
      logger.info(StartMessage)

      service.start()
      logger.info(HttpStartMessage)

      stream.start()
      logger.info(StreamStartMessage)
    } catch {
      case ex: Exception =>
        logger.error("Observed fatal exception while starting the app", ex)
        stop()
        System.exit(1)
    }
  }

  /**
    * This method stops the given `StreamsRunner` and `JmxReporter` is they have been
    * previously started. If not, this method does nothing
    */
  def stop(): Unit = {
      logger.info(HttpStopMessage)
      Try(service.stop())

      logger.info(StreamStopMessage)
      Try(stream.stop())

      logger.info(JmxReporterStopMessage)
      Try(jmxReporter.close())

      logger.info(LoggerStopMessage)
      Try(LoggerUtils.shutdownLogger())
  }
}
