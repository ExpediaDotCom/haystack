/*
 *  Copyright 2018 Expedia, Inc.
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

package com.expedia.www.haystack.http.span.collector.integration.tests

import akka.http.scaladsl.model.ContentTypes
import com.expedia.open.tracing.Tag.TagType
import com.expedia.open.tracing.{Span, Tag}
import com.expedia.www.haystack.http.span.collector.integration.IntegrationTestSpec
import com.expedia.www.haystack.http.span.collector.json.{Log => JLog, Span => JSpan, Tag => JTag}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class HttpSpanCollectorSpec extends IntegrationTestSpec {

  implicit val formats = DefaultFormats
  private val StartTimeMicros = System.currentTimeMillis() * 1000
  private val DurationMicros = 42

  "Http span collector" should {

    // this test is primarily to work around issue with Kafka docker image
    // it fails for first put for some reasons
    "connect with http and kafka" in {

      Given("a valid span")
      val spanBytes = Span.newBuilder().setTraceId("traceid").setSpanId("span-id-1").build().toByteArray

      When("the span is sent over http")
      postHttp(List(spanBytes, spanBytes))

      Then("it should be pushed to kafka")
      readRecordsFromKafka(0, 1.second).headOption
    }

    "read valid proto spans from kafka if produced proto spans on http" in {

      Given("valid proto spans")
      val span_1 = Span.newBuilder().setTraceId("trace-id-1").setSpanId("span-id-1").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros).build().toByteArray
      val span_2 = Span.newBuilder().setTraceId("trace-id-1").setSpanId("span-id-2").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros).build().toByteArray
      val span_3 = Span.newBuilder().setTraceId("trace-id-2").setSpanId("span-id-3").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros).build().toByteArray
      val span_4 = Span.newBuilder().setTraceId("trace-id-2").setSpanId("span-id-4").setOperationName("operation")
        .setServiceName("service").setStartTime(StartTimeMicros).setDuration(DurationMicros).build().toByteArray

      When("the span is sent to http span collector")
      postHttp(List(span_1, span_2, span_3, span_4))

      Then("it should be pushed to kafka with partition key as its trace id")
      val records = readRecordsFromKafka(4, 5.seconds)
      records should not be empty
      val spans = records.map(Span.parseFrom)
      spans.map(_.getTraceId).toSet should contain allOf("trace-id-1", "trace-id-2")
      spans.map(_.getSpanId) should contain allOf("span-id-1", "span-id-2", "span-id-3", "span-id-4")
    }


    "read valid proto spans from kafka if produced json spans" in {
      Given("valid json spans")
      val tags = List(JTag("number", 100), JTag("some-string", "str"), JTag("some-boolean", true), JTag("some-double", 10.5))
      val logs = List(JLog(StartTimeMicros, List(JTag("errorcode", 1))))
      val spanJsonBytesList = List(
        JSpan("trace-id-1", "span-id-1", None, "service1", "operation1", StartTimeMicros, DurationMicros, tags, Nil),
        JSpan("trace-id-2", "span-id-2", Some("parent-span-id-2"), "service2", "operation2", StartTimeMicros, DurationMicros, tags, logs),
        JSpan("trace-id-3", "span-id-3", None, "service3", "operation3", StartTimeMicros, DurationMicros, tags, Nil),
        JSpan("trace-id-1", "span-id-4", None, "service1", "operation1", StartTimeMicros, DurationMicros, Nil, Nil)
      ).map(jsonSpan => Serialization.write(jsonSpan).getBytes("utf-8"))

      When("the span is sent to http span collector")
      postHttp(spanJsonBytesList, ContentTypes.`application/json`)

      Then("it should be pushed to kafka with partition key as its trace id")
      val records = readRecordsFromKafka(4, 5.seconds)
      records should not be empty
      val spans = records.map(Span.parseFrom)
      spans.map(_.getServiceName).toSet should contain allOf("service1", "service2", "service3")
      spans.map(_.getOperationName).toSet should contain allOf("operation1", "operation2", "operation3")
      spans.map(_.getStartTime).toSet.head shouldBe StartTimeMicros
      spans.map(_.getDuration).toSet.head shouldBe DurationMicros
      spans.map(_.getTraceId).toSet should contain allOf("trace-id-1", "trace-id-2", "trace-id-3")
      spans.map(_.getSpanId) should contain allOf("span-id-1", "span-id-2", "span-id-3", "span-id-4")

      val span2 = spans.find(_.getTraceId == "trace-id-2").get
      span2.getParentSpanId shouldBe "parent-span-id-2"
      span2.getTagsCount shouldBe tags.size

      tags.foreach { jTag =>
        val tag = span2.getTagsList.asScala.find(_.getKey.equalsIgnoreCase(jTag.key)).get
        protoTagValue(tag) shouldBe jTag.value
      }

      span2.getLogsCount shouldBe logs.size
      logs foreach { jLog =>
        val logFields = span2.getLogsList.asScala.find(_.getTimestamp == jLog.timestamp).get.getFieldsList.asScala
        jLog.fields.foreach { jTag =>
          val tag = logFields.find(_.getKey.equalsIgnoreCase(jTag.key)).get
          protoTagValue(tag) shouldBe jTag.value
        }
      }
    }

    "isActive endpoint should work" in {
      Given("an empty http request")
      When("/isActive endpoint is called")
      val response = isActiveHttpCall()
      Then("response should be active")
      response shouldBe "ACTIVE"
    }
  }

  private def protoTagValue(tag: Tag): Any = {
    tag.getType match {
      case TagType.STRING => tag.getVStr
      case TagType.BOOL => tag.getVBool
      case TagType.LONG => tag.getVLong
      case TagType.DOUBLE => tag.getVDouble
      case _ => fail("fail to find the proto tag value")
    }
  }
}
