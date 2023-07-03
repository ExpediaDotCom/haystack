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

package com.expedia.www.haystack.http.span.collector.integration

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait HttpProducer {
  protected implicit val system = ActorSystem()
  protected implicit val materializer = ActorMaterializer()
  protected implicit val executionContext = system.dispatcher
  private val http = Http(system)

  def postHttp(records: List[Array[Byte]], contentType: ContentType = ContentTypes.`application/octet-stream`): Unit = {
    records foreach { record =>
      val entity = HttpEntity(contentType, record)
      val request = HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/span", entity = entity)
      http.singleRequest(request) onComplete {
        case Failure(ex) => println(s"Failed to post, reason: $ex")
        case Success(response) => println(s"Server responded with $response")
      }
    }
  }

  def isActiveHttpCall(): String = {
    val responseFuture = http.singleRequest(HttpRequest(uri = "http://localhost:8080/isActive"))
      .flatMap(response => response.entity.toStrict(5.seconds).map(_.data))
      .map(p => new String(p.compact.toArray[Byte]))
    Await.result(responseFuture, 5.seconds)
  }
}
