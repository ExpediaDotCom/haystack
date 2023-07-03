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
package com.expedia.www.haystack.commons.retries

import scala.annotation.tailrec
import scala.util.{Failure, Try}

object RetryOperation {

  /**
    * retry configuration
    * @param maxRetries maximum number of retry attempts
    * @param backOffInMillis initial backkoff in millis
    * @param backoffFactor exponential backoff that gets applied on the previousBackoff value
    */
  case class Config(maxRetries: Int, backOffInMillis: Long, backoffFactor: Double) {
    /**
      * @return next back off config after applying the exponential factor to initialBackOffInMillis
      */
    def nextBackOffConfig: Config = this.copy(backOffInMillis = Math.ceil(backOffInMillis * backoffFactor).toLong)
  }

  trait Callback {
    def onResult[T](result: T): Unit

    def onError(ex: Throwable, retry: Boolean): Unit

    def lastError(): Throwable
  }

  /**
    * executes the given function with a retry on failures
    *
    * @param f           main function to execute and retry if fail
    * @param retryConfig retry configuration with max retry count, backoff values
    * @tparam T result object from the main 'f' function
    */
  def executeWithRetryBackoff[T](f: () => T, retryConfig: Config): Try[T] = {
    executeWithRetryBackoff(f, 0, retryConfig)
  }

  @tailrec
  private def executeWithRetryBackoff[T](f: () => T, currentRetryCount: Int, retryConfig: Config): Try[T] = {
    Try {
      f()
    } match {
      case Failure(reason) if currentRetryCount < retryConfig.maxRetries && !reason.isInstanceOf[InterruptedException] =>
        Thread.sleep(retryConfig.backOffInMillis)
        executeWithRetryBackoff(f, currentRetryCount + 1, retryConfig.nextBackOffConfig)
      case result@_ => result
    }
  }

  /**
    * executes the given async function with a retry on failures
    *
    * @param f           main function to execute and retry if fail
    * @param retryConfig retry configuration with max retry count, backoff values
    * @param onSuccess   this callback is called if the main 'f' function executes with success
    * @param onFailure   this callback is called if the main 'f' function fails after all reattempts
    * @tparam T result object from the main 'f' function
    */
  def withRetryBackoff[T](f: (Callback) => Unit,
                          retryConfig: Config,
                          onSuccess: (T) => Unit,
                          onFailure: (Exception) => Unit): Unit = {
    withRetryBackoff(f, 0, retryConfig, onSuccess, onFailure)
  }

  private def withRetryBackoff[T](f: (Callback) => Unit,
                                  currentRetry: Int,
                                  retryConfig: Config,
                                  onSuccess: (T) => Unit,
                                  onFailure: (Exception) => Unit,
                                  lastSeenError: Throwable = null): Unit = {
    try {
      val retryResult = new Callback {
        override def onResult[Any](result: Any): Unit = {
          onSuccess(result.asInstanceOf[T])
        }

        override def onError(ex: Throwable, retry: Boolean): Unit = {
          if (retry && currentRetry < retryConfig.maxRetries) {
            Thread.sleep(retryConfig.backOffInMillis)
            withRetryBackoff(f, currentRetry + 1, retryConfig.nextBackOffConfig, onSuccess, onFailure, ex)
          } else {
            onFailure(new MaxRetriesAttemptedException(s"max retries=${retryConfig.maxRetries} have reached and all attempts have failed!", ex))
          }
        }

        override def lastError(): Throwable = lastSeenError
      }
      f(retryResult)
    } catch {
      case ex: Exception => onFailure(ex)
    }
  }
}
