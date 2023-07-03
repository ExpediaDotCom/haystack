/*
 *  Copyright 2017 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */

package com.expedia.www.haystack.trace.reader.services

import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.google.protobuf.GeneratedMessageV3
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object GrpcHandler {
  protected val LOGGER: Logger = LoggerFactory.getLogger(classOf[GrpcHandler])
}

/**
  * Handler for Grpc response
  * populates responseObserver with response object or error accordingly
  * takes care of corresponding logging and updating counters
  *
  * @param operationName : name of operation
  * @param executor      : executor service on which handler is invoked
  */

class GrpcHandler(operationName: String)(implicit val executor: ExecutionContextExecutor) extends MetricsSupport {
  private val metricFriendlyOperationName = operationName.replace('/', '.')
  private val timer = metricRegistry.timer(metricFriendlyOperationName)
  private val failureMeter = metricRegistry.meter(s"$metricFriendlyOperationName.failures")

  import GrpcHandler._

  def handle[Rs](request: GeneratedMessageV3, responseObserver: StreamObserver[Rs])(op: => Future[Rs]): Unit = {
    val time = timer.time()
    op onComplete {
      case Success(response) =>
        responseObserver.onNext(response)
        responseObserver.onCompleted()
        time.stop()
        LOGGER.debug(s"service invocation for operation=$operationName and request=${request.toString} completed successfully")

      case Failure(ex) =>
        responseObserver.onError(Status.fromThrowable(ex).asRuntimeException())
        failureMeter.mark()
        time.stop()
        LOGGER.error(s"service invocation for operation=$operationName and request=${request.toString} failed with error", ex)
    }
  }
}
