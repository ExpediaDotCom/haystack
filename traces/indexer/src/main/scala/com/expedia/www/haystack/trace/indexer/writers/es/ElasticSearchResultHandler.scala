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

package com.expedia.www.haystack.trace.indexer.writers.es

import com.codahale.metrics.{Meter, Timer}
import com.expedia.www.haystack.commons.retries.RetryOperation
import io.searchbox.client.JestResultHandler
import io.searchbox.core.BulkResult
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

class ElasticSearchResultHandler(timer: Timer.Context, failureMeter: Meter, retryOp: RetryOperation.Callback)
  extends JestResultHandler[BulkResult] {

  protected val LOGGER: Logger = LoggerFactory.getLogger(classOf[ElasticSearchResultHandler])

  /**
    * this callback is invoked when the elastic search writes is completed with success or warnings
    *
    * @param result bulk result
    */
  def completed(result: BulkResult): Unit = {
    timer.close()

    // group the failed items as per status and log once for such a failed item
    if (result.getFailedItems != null) {
      result.getFailedItems.asScala.groupBy(_.status) foreach {
        case (statusCode, failedItems) =>
          failureMeter.mark(failedItems.size)
          LOGGER.error(s"Index operation has failed with status=$statusCode, totalFailedItems=${failedItems.size}, " +
            s"errorReason=${failedItems.head.errorReason}, errorType=${failedItems.head.errorType}")
      }
    }
    retryOp.onResult(result)
  }

  /**
    * this callback is invoked when the writes to elastic search fail completely
    *
    * @param ex the exception contains the reason of failure
    */
  def failed(ex: Exception): Unit = {
    timer.close()
    failureMeter.mark()
    LOGGER.error("Fail to write the documents in elastic search with reason:", ex)
    retryOp.onError(ex, retry = true)
  }
}
