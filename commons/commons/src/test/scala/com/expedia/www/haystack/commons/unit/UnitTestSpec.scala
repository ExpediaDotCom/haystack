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

package com.expedia.www.haystack.commons.unit

import java.util.UUID

import com.expedia.open.tracing.{Log, Span, Tag}
import org.scalatest._
import org.scalatest.easymock.EasyMockSugar

trait UnitTestSpec extends WordSpec with GivenWhenThen with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with EasyMockSugar {

  val SERVER_SEND_EVENT = "ss"
  val SERVER_RECV_EVENT = "sr"
  val CLIENT_SEND_EVENT = "cs"
  val CLIENT_RECV_EVENT = "cr"
  protected def computeCurrentTimeInSecs: Long = {
    System.currentTimeMillis() / 1000L
  }

  private[commons] def generateTestSpan(serviceName: String, operation: String, duration: Long, client: Boolean, server: Boolean): Span = {
    generateTestSpan(UUID.randomUUID().toString, serviceName, operation, duration, client, server)
  }

  private[commons] def generateTestSpan(spanId: String, serviceName: String, operation: String, duration: Long, client: Boolean, server: Boolean): Span = {
    val ts = System.currentTimeMillis() - (10 * 1000)
    generateTestSpan(spanId, ts, serviceName, operation, duration, client, server)
  }

  private[commons] def generateTestSpan(spanId: String, ts: Long, serviceName: String, operation: String, duration: Long, client: Boolean, server: Boolean): Span = {


    val spanBuilder = Span.newBuilder()
    spanBuilder.setTraceId(UUID.randomUUID().toString)
    spanBuilder.setSpanId(spanId)
    spanBuilder.setServiceName(serviceName)
    spanBuilder.setOperationName(operation)
    spanBuilder.setStartTime(ts)
    spanBuilder.setDuration(duration)

    val logBuilder = Log.newBuilder()
    if (client) {
      logBuilder.setTimestamp(ts)
      logBuilder.addFields(Tag.newBuilder().setKey("event").setVStr(CLIENT_SEND_EVENT).build())
      spanBuilder.addLogs(logBuilder.build())
      logBuilder.clear()
      logBuilder.setTimestamp(ts + duration)
      logBuilder.addFields(Tag.newBuilder().setKey("event").setVStr(CLIENT_RECV_EVENT).build())
      spanBuilder.addLogs(logBuilder.build())
    }

    if (server) {
      logBuilder.setTimestamp(ts)
      logBuilder.addFields(Tag.newBuilder().setKey("event").setVStr(SERVER_RECV_EVENT).build())
      spanBuilder.addLogs(logBuilder.build())
      logBuilder.clear()
      logBuilder.setTimestamp(ts + duration)
      logBuilder.addFields(Tag.newBuilder().setKey("event").setVStr(SERVER_SEND_EVENT).build())
      spanBuilder.addLogs(logBuilder.build())
    }

    spanBuilder.build()
  }

}
