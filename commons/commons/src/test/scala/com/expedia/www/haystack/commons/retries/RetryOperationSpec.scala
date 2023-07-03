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

import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetryOperationSpec extends FunSpec with Matchers {
  describe("Retry Operation handler") {
    it("should not retry if main async function runs successfully") {
      @volatile var onSuccessCalled = 0
      val mainFuncCalled = new AtomicInteger(0)

      RetryOperation.withRetryBackoff((callback) => {
        mainFuncCalled.incrementAndGet()
        Future {
          Thread.sleep(500)
          callback.onResult("xxxx")
        }
      },
        RetryOperation.Config(maxRetries = 3, backOffInMillis = 100, backoffFactor = 1.5),
        onSuccess = (result: String) => {
          result.toString shouldEqual "xxxx"
          onSuccessCalled = onSuccessCalled + 1
        }, onFailure = (_) => {
          fail("onFailure callback should not be called")
        })

      Thread.sleep(3000)
      mainFuncCalled.get() shouldBe 1
      onSuccessCalled shouldBe 1
    }
  }

  it("should retry for async function  if callback says retry but should not fail as last attempt succeeds") {
    @volatile var onSuccessCalled = 0
    val retryConfig = RetryOperation.Config(maxRetries = 3, backOffInMillis = 100, backoffFactor = 1.5)
    val mainFuncCalled = new AtomicInteger(0)

    RetryOperation.withRetryBackoff((callback) => {
      val count = mainFuncCalled.incrementAndGet()
      if (count > 1) {
        callback.lastError() should not be null
      } else {
        callback.lastError() shouldBe null
      }
      if (count <= retryConfig.maxRetries) {
        Future {
          Thread.sleep(200)
          callback.onError(new RuntimeException("error"), retry = true)
        }
      } else {
        Future {
          Thread.sleep(200)
          callback.onResult("xxxxx")
        }
      }
    },
      retryConfig,
      onSuccess = (result: String) => {
        result shouldEqual "xxxxx"
        onSuccessCalled = onSuccessCalled + 1
      }, onFailure = (_) => {
        fail("onFailure should not be called")
      })

    Thread.sleep(4000)
    mainFuncCalled.get() shouldBe retryConfig.maxRetries + 1
    onSuccessCalled shouldBe 1
  }

  it("should retry for async function if callback asks for a retry and fail finally as all attempts fail") {
    @volatile var onFailureCalled = 0
    val retryConfig = RetryOperation.Config(maxRetries = 2, backOffInMillis = 100, backoffFactor = 1.5)
    val mainFuncCalled = new AtomicInteger(0)

    val error = new RuntimeException("error")
    RetryOperation.withRetryBackoff((callback) => {
      mainFuncCalled.incrementAndGet()
      Future {
        Thread.sleep(500)
        callback.onError(error, retry = true)
      }
    },
      retryConfig,
      onSuccess = (_: Any) => {
        fail("onSuccess should not be called")
      }, onFailure = (ex) => {
        assert(ex.isInstanceOf[MaxRetriesAttemptedException])
        ex.getCause shouldBe error
        onFailureCalled = onFailureCalled + 1
      })

    Thread.sleep(4000)
    mainFuncCalled.get() shouldBe (retryConfig.maxRetries + 1)
    onFailureCalled shouldBe 1
  }

  it("should not retry if main async function runs successfully") {
    var mainFuncCalled = 0
    val resp = RetryOperation.executeWithRetryBackoff(() => {
      mainFuncCalled = mainFuncCalled + 1
      "success"
    }, RetryOperation.Config(3, 100, 2))

    mainFuncCalled shouldBe 1
    resp.get shouldEqual "success"
  }

  it("should retry for function if callback says retry but should not fail as last attempt succeeds") {
    var mainFuncCalled = 0
    val retryConfig = RetryOperation.Config(3, 100, 2)
    val resp = RetryOperation.executeWithRetryBackoff(() => {
      mainFuncCalled = mainFuncCalled + 1
      if(mainFuncCalled - 1 < retryConfig.maxRetries) throw new RuntimeException else "success"
    }, retryConfig)

    mainFuncCalled shouldBe retryConfig.maxRetries + 1
    resp.get shouldEqual "success"
  }

  it("should retry for function if callback asks for a retry and fail finally as all attempts fail") {
    var mainFuncCalled = 0
    val retryConfig = RetryOperation.Config(3, 100, 2)
    val error = new RuntimeException("error")
    val resp = RetryOperation.executeWithRetryBackoff(() => {
      mainFuncCalled = mainFuncCalled + 1
      throw error
    }, retryConfig)

    mainFuncCalled shouldBe retryConfig.maxRetries + 1
    resp.isFailure shouldBe true
  }

  it("retry operation backoff config should return the next backoff config") {
    val retry = RetryOperation.Config(3, 1000, 1.5)

    var nextBackoffConfig = retry.nextBackOffConfig
    nextBackoffConfig.maxRetries shouldBe 3
    nextBackoffConfig.nextBackOffConfig.backoffFactor shouldBe 1.5
    nextBackoffConfig.backOffInMillis shouldBe 1500

    nextBackoffConfig = nextBackoffConfig.nextBackOffConfig
    nextBackoffConfig.maxRetries shouldBe 3
    nextBackoffConfig.nextBackOffConfig.backoffFactor shouldBe 1.5
    nextBackoffConfig.backOffInMillis shouldBe 2250
  }
}
