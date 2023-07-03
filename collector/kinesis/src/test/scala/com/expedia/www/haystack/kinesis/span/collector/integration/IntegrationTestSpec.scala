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

package com.expedia.www.haystack.kinesis.span.collector.integration

import java.io.File
import java.util.concurrent.Executors

import com.expedia.www.haystack.kinesis.span.collector.App
import org.scalatest._

class IntegrationTestSpec extends WordSpec with GivenWhenThen with Matchers with LocalKinesisProducer with LocalKafkaConsumer
  with OptionValues with BeforeAndAfterAll {

  private val executor = Executors.newSingleThreadExecutor()

  override def beforeAll(): Unit = {
    // check if the stream exists, if not create one
    createStreamIfNotExists()

    new File("/app").mkdir()

    executor.submit(new Runnable {
      override def run(): Unit = App.main(null)
    })
    // wait for few sec to let app start
    Thread.sleep(60000)
  }

  override def afterAll(): Unit = {
    // check if the stream exists, if not create one
    shutdownKinesisClient()
  }
}