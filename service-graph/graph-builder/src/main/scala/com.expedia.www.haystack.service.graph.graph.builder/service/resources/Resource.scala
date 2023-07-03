/*
 *
 *     Copyright 2018 Expedia, Inc.
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
package com.expedia.www.haystack.service.graph.graph.builder.service.resources

import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.expedia.www.haystack.commons.metrics.MetricsSupport
import org.apache.http.entity.ContentType
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

abstract class Resource(endpointName: String) extends HttpServlet with MetricsSupport {
  private val LOGGER = LoggerFactory.getLogger(classOf[Resource])
  private val timer = metricRegistry.timer(endpointName)
  private val failureCount = metricRegistry.meter(s"$endpointName.failure")

  implicit val formats = DefaultFormats

  protected override def doGet(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val time = timer.time()

    Try(get(request)) match {
      case Success(getResponse) =>
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType)
        response.setStatus(HttpServletResponse.SC_OK)
        response.getWriter.print(Serialization.write(getResponse))
        LOGGER.info(s"accesslog: ${request.getRequestURI} completed successfully")

      case Failure(ex) =>
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType)
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        response.getWriter.print(Serialization.write(new Error(ex.getMessage)))
        failureCount.mark()
        LOGGER.error(s"accesslog: ${request.getRequestURI} failed", ex)
    }

    response.getWriter.flush()
    time.stop()
  }

  // endpoint method for child resources to inherit
  protected def get(request: HttpServletRequest): Object

  class Error(message: String, error: Boolean = true)
}
