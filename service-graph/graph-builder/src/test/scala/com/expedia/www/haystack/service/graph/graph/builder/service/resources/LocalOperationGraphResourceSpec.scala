package com.expedia.www.haystack.service.graph.graph.builder.service.resources

import com.expedia.www.haystack.service.graph.graph.builder.model.{OperationGraph, OperationGraphEdge}
import com.expedia.www.haystack.service.graph.graph.builder.service.fetchers.LocalOperationEdgesFetcher
import com.expedia.www.haystack.service.graph.graph.builder.service.utils.QueryTimestampReader
import javax.servlet.http.HttpServletRequest
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}

class LocalOperationGraphResourceSpec extends FunSpec with Matchers with MockitoSugar {
  private implicit val timestampReader: QueryTimestampReader = mock[QueryTimestampReader]
  private class LocalOperationGraphResourceChild(localEdgesFetcher: LocalOperationEdgesFetcher)
    extends LocalOperationGraphResource(localEdgesFetcher: LocalOperationEdgesFetcher) {
    override def get(request: HttpServletRequest): OperationGraph = {
      super.get(request)
    }
  }

  describe("LocalOperationGraphResource.get()") {
    val localEdgesFetcher = mock[LocalOperationEdgesFetcher]
    val request = mock[HttpServletRequest]
    val operationGraphEdges = mock[List[OperationGraphEdge]]

    val OperationGraphEdgesLength = 42
    val From: Long = 271828
    val To: Long = 371415

    val localOperationGraphResource = new LocalOperationGraphResourceChild(localEdgesFetcher)

    it ("should read an OperationGraph with the correct timestamps") {
      when(timestampReader.fromTimestamp(request)).thenReturn(From)
      when(timestampReader.toTimestamp(request)).thenReturn(To)
      when(localEdgesFetcher.fetchEdges(From, To)).thenReturn(operationGraphEdges)
      when(operationGraphEdges.length).thenReturn(OperationGraphEdgesLength)

      val localGraph = localOperationGraphResource.get(request)

      assert(localGraph.edges ==  operationGraphEdges)

      verify(timestampReader).fromTimestamp(request)
      verify(timestampReader).toTimestamp(request)
      verify(localEdgesFetcher).fetchEdges(From, To)
      verify(operationGraphEdges).length
      Mockito.verifyNoMoreInteractions(localEdgesFetcher, request, operationGraphEdges)
    }
  }
}
