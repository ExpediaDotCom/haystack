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

package com.expedia.www.haystack.http.span.collector.json

import com.expedia.open.tracing.Tag.TagType
import com.expedia.open.tracing.{Log => PLog, Span => PSpan, Tag => PTag}
case class Tag(key: String, value: Any)
case class Log(timestamp: Long, fields: List[Tag])

case class Span(traceId: String,
                spanId: String,
                parentSpanId: Option[String],
                serviceName: String,
                operationName: String,
                startTime: Long,
                duration: Int,
                tags: List[Tag],
                logs: List[Log]) {
  def toProto: Array[Byte] = {
    val span = PSpan.newBuilder()
      .setTraceId(traceId)
      .setSpanId(spanId)
      .setServiceName(serviceName)
      .setOperationName(operationName)
      .setParentSpanId(parentSpanId.getOrElse(""))
      .setStartTime(startTime)
      .setDuration(duration)

    tags.map(tag => span.addTags(createProtoTag(tag)))
    logs.map(log => {
      val l = PLog.newBuilder().setTimestamp(log.timestamp)
      log.fields.foreach(tag => l.addFields(createProtoTag(tag)))
      span.addLogs(l)
    })

    span.build().toByteArray
  }

  private def createProtoTag(tag: Tag): PTag.Builder = {
    tag.value match {
      case _: Int =>
        PTag.newBuilder().setKey(tag.key).setVLong(tag.value.asInstanceOf[Int]).setType(TagType.LONG)
      case _: BigInt =>
        PTag.newBuilder().setKey(tag.key).setVLong(tag.value.asInstanceOf[BigInt].longValue()).setType(TagType.LONG)
      case _: Long =>
        PTag.newBuilder().setKey(tag.key).setVLong(tag.value.asInstanceOf[Long]).setType(TagType.LONG)
      case _: Double =>
        PTag.newBuilder().setKey(tag.key).setVDouble(tag.value.asInstanceOf[Double]).setType(TagType.DOUBLE)
      case _: Float =>
        PTag.newBuilder().setKey(tag.key).setVDouble(tag.value.asInstanceOf[Float].toDouble).setType(TagType.DOUBLE)
      case _: Boolean =>
        PTag.newBuilder().setKey(tag.key).setVBool(tag.value.asInstanceOf[Boolean]).setType(TagType.BOOL)
      case _ => PTag.newBuilder().setKey(tag.key).setVStr(tag.value.toString).setType(TagType.STRING)
    }
  }
}
