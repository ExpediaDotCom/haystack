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

package com.expedia.www.haystack.http.span.collector

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Route}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.codahale.metrics.JmxReporter
import com.expedia.www.haystack.collector.commons.sink.kafka.KafkaRecordSink
import com.expedia.www.haystack.collector.commons.{MetricsSupport, ProtoSpanExtractor, SpanDecoratorFactory}
import com.expedia.www.haystack.http.span.collector.json.Span
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.sys._
import scala.util.Try

object WebServer extends App with MetricsSupport {
  val LOGGER = LoggerFactory.getLogger(WebServer.getClass)

  // setup kafka sink
  private val kafkaSink = new KafkaRecordSink(ProjectConfiguration.kafkaProducerConfig(), ProjectConfiguration.externalKafkaConfig())
  private val kvExtractor = new ProtoSpanExtractor(ProjectConfiguration.extractorConfig(),
    LoggerFactory.getLogger(classOf[ProtoSpanExtractor]),
    SpanDecoratorFactory.get(ProjectConfiguration.pluginConfiguration(), ProjectConfiguration.additionalTagConfig(), LOGGER))

  private val http = ProjectConfiguration.httpConfig

  // setup actor system
  implicit val system: ActorSystem = ActorSystem("span-collector", ProjectConfiguration.config)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val formats: DefaultFormats.type = DefaultFormats

  // start jmx reporter
  private val jmxReporter = JmxReporter.forRegistry(metricRegistry).build()
  jmxReporter.start()

  // start http server on given host and port
  val bindingFuture = Http(system).bindAndHandle(routes(), http.host, http.port)
  LOGGER.info(s"Server is now listening at http://${http.host}:${http.port}")

  addShutdownHook { shutdownHook() }

  def routes(): Route = {
    // build the routes
    path("span") {
      post {
        extractRequest {
          req =>
            if (http.authenticator(req)) {
              val spanBytes = req.entity
                .dataBytes
                .runFold(ByteString.empty) { case (acc, b) => acc ++ b }
                .map(_.compact.toArray[Byte])

              req.entity.contentType match {
                case ContentTypes.`application/json` =>
                  complete {
                    processJsonSpan(spanBytes)
                  }
                case _ =>
                  complete {
                    processProtoSpan(spanBytes)
                  }
              }
            } else {
              reject(AuthorizationFailedRejection)
            }
        }
      }
    } ~
      path("isActive") {
        get {
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "ACTIVE"))
        }
      }
  }

  def processProtoSpan(spanBytes: Future[Array[Byte]]): Future[StatusCode] = {
    spanBytes
      .map(kvExtractor.extractKeyValuePairs)
      .map(kvPairs => {
        kvPairs foreach { kv => kafkaSink.toAsync(kv) }
        StatusCode.int2StatusCode(StatusCodes.Accepted.intValue)
      })
  }

  def processJsonSpan(dataBytes: Future[Array[Byte]]): Future[StatusCode] = {
    processProtoSpan(
      dataBytes
        .map(bytes => Serialization.read[Span](new String(bytes)))
        .map(span => span.toProto))
  }

  def shutdownHook(): Unit = {
    LOGGER.info("Terminating Server ...")
    bindingFuture
      .flatMap(_.unbind())
      .onComplete { _ => close() }
    Await.result(system.whenTerminated, 30.seconds)
  }

  def close(): Unit = {
    Try(kafkaSink.close())
    Try(http.authenticator.close())
    materializer.shutdown()
    system.terminate()
    jmxReporter.close()
  }
}
