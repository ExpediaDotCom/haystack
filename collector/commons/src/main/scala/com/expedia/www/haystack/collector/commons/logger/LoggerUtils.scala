/*
 *
 *     Copyright 2017 Expedia, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 */

package com.expedia.www.haystack.collector.commons.logger

import org.slf4j.{ILoggerFactory, LoggerFactory}

object LoggerUtils {

  /**
    * shutdown the logger using reflection.
    * for logback, it calls stop() method on loggerContext
    * for log4j, it calls close() method on log4j context
    */
  def shutdownLogger(): Unit = {
    val factory = LoggerFactory.getILoggerFactory
    shutdownLoggerWithFactory(factory)
  }

  // just visible for testing
  def shutdownLoggerWithFactory(factory: ILoggerFactory): Unit = {
    val clazz = factory.getClass
    try {
      clazz.getMethod("stop").invoke(factory) // logback
    } catch {
      case _: ReflectiveOperationException =>
        try {
          clazz.getMethod("close").invoke(factory) // log4j
        } catch {
          case _: Exception =>
        }
      case _: Exception =>
    }
  }
}
