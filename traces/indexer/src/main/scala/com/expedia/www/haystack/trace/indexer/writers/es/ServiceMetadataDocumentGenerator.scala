/*
 *  Copyright 2018 Expedia, Group.
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

import java.time.Instant

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trace.commons.clients.es.document.ServiceMetadataDoc
import com.expedia.www.haystack.trace.commons.utils.SpanUtils
import com.expedia.www.haystack.trace.indexer.config.entities.ServiceMetadataWriteConfiguration
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable

class ServiceMetadataDocumentGenerator(config: ServiceMetadataWriteConfiguration) extends MetricsSupport {

  private var serviceMetadataMap = new mutable.HashMap[String, mutable.Set[String]]()
  private var allOperationCount: Int = 0
  private var lastFlushInstant = Instant.MIN

  private def shouldFlush: Boolean = {
    config.flushIntervalInSec == 0 || Instant.now().minusSeconds(config.flushIntervalInSec).isAfter(lastFlushInstant)
  }

  private def areStatementsReadyToBeExecuted(): Seq[ServiceMetadataDoc] = {
    if (serviceMetadataMap.nonEmpty && (shouldFlush || allOperationCount > config.flushOnMaxOperationCount)) {
      val statements = serviceMetadataMap.flatMap {
        case (serviceName, operationList) =>
          createServiceMetadataDoc(serviceName, operationList)
      }

      lastFlushInstant = Instant.now()
      serviceMetadataMap = new mutable.HashMap[String, mutable.Set[String]]()
      allOperationCount = 0
      statements.toSeq
    } else {
      Nil
    }
  }

  /**
    * get the list of unique service metadata documents contained in the list of spans
    *
    * @param spans : list of service metadata
    * @return
    */
  def getAndUpdateServiceMetadata(spans: Iterable[Span]): Seq[ServiceMetadataDoc] = {
    this.synchronized {
      spans.foreach(span => {
        if (StringUtils.isNotEmpty(span.getServiceName) && StringUtils.isNotEmpty(span.getOperationName)) {
          val operationsList = serviceMetadataMap.getOrElseUpdate(span.getServiceName, mutable.Set[String]())
          if (operationsList.add(span.getOperationName)) {
            allOperationCount += 1
          }
        }
      })
      areStatementsReadyToBeExecuted()
    }
  }

  /**
    * @return index document that can be put in elastic search
    */
  def createServiceMetadataDoc(serviceName: String, operationList: mutable.Set[String]): List[ServiceMetadataDoc] = {
    operationList.map(operationName => ServiceMetadataDoc(serviceName, operationName)).toList

  }
}
