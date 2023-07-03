package com.expedia.www.haystack.service.graph.graph.builder

import java.security.Permission

import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.commons.kstreams.app.ManagedService
import com.expedia.www.haystack.service.graph.graph.builder.ManagedApplication._
import org.mockito.Mockito.{times, verify, verifyNoMoreInteractions, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FunSpec, SequentialNestedSuiteExecution}
import org.slf4j.Logger

sealed case class ExitException(status: Int) extends SecurityException("System.exit() was called") {
}

sealed class NoExitSecurityManager extends SecurityManager {
  override def checkPermission(perm: Permission): Unit = {}

  override def checkPermission(perm: Permission, context: Object): Unit = {}

  override def checkExit(status: Int): Unit = {
    super.checkExit(status)
    throw ExitException(status)
  }
}

class ManagedApplicationSpec extends FunSpec with MockitoSugar with BeforeAndAfterAll with SequentialNestedSuiteExecution {

  override def beforeAll(): Unit = System.setSecurityManager(new NoExitSecurityManager())
  override def afterAll(): Unit = System.setSecurityManager(null)

  describe("ManagedApplication constructor") {
    val (mocks: List[AnyRef], service: ManagedService, stream: ManagedService, jmxReporter: JmxReporter, logger: Logger) = createAndBundleMocks
    it ("should throw an IllegalArgumentException if passed a null service") {
      assertThrows[IllegalArgumentException] {
        new ManagedApplication(null, stream, jmxReporter, logger)
      }
    }
    it ("should throw an IllegalArgumentException if passed a null stream") {
      assertThrows[IllegalArgumentException] {
        new ManagedApplication(service, null, jmxReporter, logger)
      }
    }
    it ("should throw an IllegalArgumentException if passed a null jmxReporter") {
      assertThrows[IllegalArgumentException] {
        new ManagedApplication(service, stream, null, logger)
      }
    }
    it ("should throw an IllegalArgumentException if passed a null logger") {
      assertThrows[IllegalArgumentException] {
        new ManagedApplication(service, stream, jmxReporter, null)
      }
    }
    verifyNoMoreInteractionsForAllMocks(mocks)
  }

  describe("ManagedApplication start") {
    val (mocks: List[AnyRef], service: ManagedService, stream: ManagedService, jmxReporter: JmxReporter, logger: Logger) = createAndBundleMocks
    val managedApplication = new ManagedApplication(service, stream, jmxReporter, logger)
    it ("should start all dependencies when called") {
      managedApplication.start()
      verify(service).start()
      verify(logger).info(StartMessage)
      verify(stream).start()
      verify(logger).info(HttpStartMessage)
      verify(jmxReporter).start()
      verify(logger).info(StreamStartMessage)
    }
    it ("should call System.exit() when an exception is thrown") {
      when(service.start()).thenThrow(new NullPointerException)
      assertThrows[ExitException] {
        managedApplication.start()
      }
      verify(service, times(2)).start()
    }
    verifyNoMoreInteractionsForAllMocks(mocks)
  }

  describe("ManagedApplication stop") {
    val (mocks: List[AnyRef], service: ManagedService, stream: ManagedService, jmxReporter: JmxReporter, logger: Logger) = createAndBundleMocks
    it ("should stop all dependencies when called") {
      val managedApplication = new ManagedApplication(service, stream, jmxReporter, logger)
      managedApplication.stop()
      verify(logger).info(HttpStopMessage)
      verify(service).stop()
      verify(logger).info(StreamStopMessage)
      verify(stream).stop()
      verify(logger).info(JmxReporterStopMessage)
      verify(jmxReporter).close()
      verify(logger).info(LoggerStopMessage)
    }
    verifyNoMoreInteractionsForAllMocks(mocks)
  }

  private def createMocks(): List[AnyRef] =
  {
    val service = mock[ManagedService]
    val stream = mock[ManagedService]
    val jmxReporter = mock[JmxReporter]
    val logger = mock[Logger]
    List(service, stream, jmxReporter, logger)
  }

  private def createAndBundleMocks = {
    val mocks = createMocks()
    val (service: ManagedService, stream: ManagedService, jmxReporter: JmxReporter, logger: Logger) = bundleMocks(mocks)
    (mocks, service, stream, jmxReporter, logger)
  }

  private def bundleMocks(mocks: List[AnyRef]) = {
    val service = mocks.head.asInstanceOf[ManagedService]
    val stream = mocks(1).asInstanceOf[ManagedService]
    val jmxReporter = mocks(2).asInstanceOf[JmxReporter]
    val logger = mocks(3).asInstanceOf[Logger]
    (service, stream, jmxReporter, logger)
  }

  private def verifyNoMoreInteractionsForAllMocks(mocks: List[AnyRef]): Unit = {
    verifyNoMoreInteractions(mocks.head)
    verifyNoMoreInteractions(mocks(1))
    verifyNoMoreInteractions(mocks(2))
    verifyNoMoreInteractions(mocks(3))
  }

}
