/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.www.haystack.collector.commons.unit

import com.expedia.www.haystack.collector.commons.logger.LoggerUtils
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FunSpec, Matchers}
import org.slf4j.{ILoggerFactory, Logger}

class LoggerUtilsSpec extends FunSpec with Matchers with EasyMockSugar {

  describe("Logger Utils") {
    it("should close the logger if it has stop method for e.g. logback") {
      val logger = mock[Logger]
      var isStopped = false

      val loggerFactory = new ILoggerFactory {
        override def getLogger(s: String): Logger = logger
        def stop(): Unit = isStopped = true
      }

      whenExecuting(logger) {
        LoggerUtils.shutdownLoggerWithFactory(loggerFactory)
        isStopped shouldBe true
      }
    }

    it("should close the logger if it has close method for e.g. log4j") {
      val logger = mock[Logger]
      var isStopped = false

      val loggerFactory = new ILoggerFactory {
        override def getLogger(s: String): Logger = logger
        def close(): Unit = isStopped = true
      }

      whenExecuting(logger) {
        LoggerUtils.shutdownLoggerWithFactory(loggerFactory)
        isStopped shouldBe true
      }
    }

    it("should not able to close the logger if it has neither stop/close method") {
      val logger = mock[Logger]
      var isStopped = false

      val loggerFactory = new ILoggerFactory {
        override def getLogger(s: String): Logger = logger
        def shutdown(): Unit = isStopped = true
      }

      whenExecuting(logger) {
        LoggerUtils.shutdownLoggerWithFactory(loggerFactory)
        isStopped shouldBe false
      }
    }
  }
}