/*
 *  Copyright 2019, Expedia Group.
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

package com.expedia.www.haystack.trace.reader.stores

import com.expedia.open.tracing.api._
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.trace.commons.clients.es.document.TraceIndexDoc
import com.expedia.www.haystack.trace.commons.config.entities.{TraceStoreBackends, WhitelistIndexFieldConfiguration}
import com.expedia.www.haystack.trace.reader.config.entities.ElasticSearchConfiguration
import com.expedia.www.haystack.trace.reader.stores.readers.es.ElasticSearchReader
import com.expedia.www.haystack.trace.reader.stores.readers.es.query.{FieldValuesQueryGenerator, ServiceMetadataQueryGenerator, TraceCountsQueryGenerator, TraceSearchQueryGenerator}
import com.expedia.www.haystack.trace.reader.stores.readers.grpc.GrpcTraceReaders
import io.searchbox.core.SearchResult
import org.elasticsearch.index.IndexNotFoundException
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future}

class EsIndexedTraceStore(traceStoreBackendConfig: TraceStoreBackends,
                          elasticSearchConfiguration: ElasticSearchConfiguration,
                          whitelistedFieldsConfiguration: WhitelistIndexFieldConfiguration)(implicit val executor: ExecutionContextExecutor)
  extends TraceStore with MetricsSupport with ResponseParser {
  private val LOGGER = LoggerFactory.getLogger(classOf[EsIndexedTraceStore])

  private val traceReader: GrpcTraceReaders = new GrpcTraceReaders(traceStoreBackendConfig)
  private val esReader: ElasticSearchReader = new ElasticSearchReader(elasticSearchConfiguration.clientConfiguration, elasticSearchConfiguration.awsRequestSigningConfiguration)
  private val traceSearchQueryGenerator = new TraceSearchQueryGenerator(elasticSearchConfiguration.spansIndexConfiguration, ES_NESTED_DOC_NAME, whitelistedFieldsConfiguration)
  private val traceCountsQueryGenerator = new TraceCountsQueryGenerator(elasticSearchConfiguration.spansIndexConfiguration, ES_NESTED_DOC_NAME, whitelistedFieldsConfiguration)
  private val fieldValuesQueryGenerator = new FieldValuesQueryGenerator(elasticSearchConfiguration.spansIndexConfiguration, ES_NESTED_DOC_NAME, whitelistedFieldsConfiguration)
  private val serviceMetadataQueryGenerator = new ServiceMetadataQueryGenerator(elasticSearchConfiguration.serviceMetadataIndexConfiguration)

  private val esCountTraces = (request: TraceCountsRequest, useSpecificIndices: Boolean) => {
    esReader.count(traceCountsQueryGenerator.generate(request, useSpecificIndices))
  }

  private val esSearchTraces = (request: TracesSearchRequest, useSpecificIndices: Boolean) => {
    esReader.search(traceSearchQueryGenerator.generate(request, useSpecificIndices))
  }

  private def handleIndexNotFoundResult(result: Future[SearchResult],
                                        retryFunc: () => Future[SearchResult]): Future[SearchResult] = {
    result.recoverWith {
      case _: IndexNotFoundException => retryFunc()
    }
  }

  override def searchTraces(request: TracesSearchRequest): Future[Seq[Trace]] = {
    // search ES with specific indices
    val esResult = esSearchTraces(request, true)
    // handle the response and retry in case of IndexNotFoundException
    handleIndexNotFoundResult(esResult, () => esSearchTraces(request, false)).flatMap(result => extractTraces(result))
  }

  private def extractTraces(result: SearchResult): Future[Seq[Trace]] = {
    val traceIdKey = "traceid"

    // go through each hit and fetch trace for parsed traceId
    val sourceList = result.getSourceAsStringList
    if (sourceList != null && sourceList.size() > 0) {
      val traceIds = sourceList
        .asScala
        .map(source => extractStringFieldFromSource(source, traceIdKey))
        .filter(!_.isEmpty)
        .toSet[String] // de-dup traceIds
        .toList

      traceReader.readTraces(traceIds)
    } else {
      Future.successful(Nil)
    }
  }

  override def getTrace(traceId: String): Future[Trace] = traceReader.readTraces(List(traceId)).map(_.head)

  override def getFieldNames: Future[FieldNames] = {
    val fields = whitelistedFieldsConfiguration.whitelistIndexFields.distinct.sortBy(_.name)
    val builder = FieldNames.newBuilder()

    fields.foreach {
      f => {
        builder.addNames(f.name)
        builder.addFieldMetadata(FieldMetadata.newBuilder().setIsRangeQuery(f.enableRangeQuery))
      }
    }

    Future.successful(builder.build())
  }

  private def readFromServiceMetadata(request: FieldValuesRequest): Option[Future[Seq[String]]] = {
    val serviceMetadataConfig = elasticSearchConfiguration.serviceMetadataIndexConfiguration
    if (!serviceMetadataConfig.enabled) return None

    if (request.getFieldName.toLowerCase == TraceIndexDoc.SERVICE_KEY_NAME && request.getFiltersCount == 0) {
      Some(esReader
        .search(serviceMetadataQueryGenerator.generateSearchServiceQuery())
        .map(extractServiceMetadata))
    } else if (request.getFieldName.toLowerCase == TraceIndexDoc.OPERATION_KEY_NAME
      && (request.getFiltersCount == 1)
      && request.getFiltersList.get(0).getName.toLowerCase == TraceIndexDoc.SERVICE_KEY_NAME) {
      Some(esReader
        .search(serviceMetadataQueryGenerator.generateSearchOperationQuery(request.getFilters(0).getValue))
        .map(extractOperationMetadataFromSource(_, request.getFieldName.toLowerCase)))
    } else {
      LOGGER.info("read from service metadata request isn't served by elasticsearch")
      None
    }
  }


  override def getFieldValues(request: FieldValuesRequest): Future[Seq[String]] = {
    readFromServiceMetadata(request).getOrElse(
      esReader
        .search(fieldValuesQueryGenerator.generate(request))
        .map(extractFieldValues(_, request.getFieldName.toLowerCase)))
  }

  override def getTraceCounts(request: TraceCountsRequest): Future[TraceCounts] = {
    // search ES with specific indices
    val esResponse = esCountTraces(request, true)

    // handle the response and retry in case of IndexNotFoundException
    handleIndexNotFoundResult(esResponse, () => esCountTraces(request, false))
      .map(result => mapSearchResultToTraceCount(request.getStartTime, request.getEndTime, result))
  }

  override def getRawTraces(request: RawTracesRequest): Future[Seq[Trace]] = {
    traceReader.readTraces(request.getTraceIdList.asScala.toList)
  }

  override def close(): Unit = {
    traceReader.close()
    esReader.close()
  }
}
