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
package com.expedia.www.haystack.service.graph.graph.builder.service

import javax.servlet.Servlet

import com.expedia.www.haystack.service.graph.graph.builder.config.entities.ServiceConfiguration
import org.eclipse.jetty.server.{HttpConfiguration, HttpConnectionFactory, Server, ServerConnector}
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory

class HttpService(config: ServiceConfiguration, resources: Map[String, Servlet]) extends AutoCloseable{
  private val LOGGER = LoggerFactory.getLogger(classOf[HttpService])

  // TODO move server creation to a supplier
  private val server = {
    // threadpool to run servlets
    val threadPool = new QueuedThreadPool(config.threads.max, config.threads.min, config.threads.idleTimeout)

    // building jetty server
    val server = new Server(threadPool)

    // configuring jetty's http parameters
    val httpConnector = new ServerConnector(server, new HttpConnectionFactory(new HttpConfiguration))
    httpConnector.setPort(config.http.port)
    httpConnector.setIdleTimeout(config.http.idleTimeout)
    server.addConnector(httpConnector)

    // adding servlets
    val context = new ServletContextHandler(server, "/")
    resources.foreach(
      resource => {
        LOGGER.info(s"adding servlet ${resource._2} at ${resource._1}")
        context.addServlet(new ServletHolder(resource._2), resource._1)
      }
    )

    // built jetty server object
    LOGGER.info("jetty server constructed")
    server
  }


  def start(): Unit = {
    server.start()
  }

  def close(): Unit = {
    server.stop()
    server.destroy()
  }
}
