package com.expedia.www.haystack.trends.feature.tests.kstreams.processor

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.trends.feature.FeatureSpec
import com.expedia.www.haystack.trends.kstream.processor.AdditionalTagsProcessor
import org.apache.kafka.streams.processor.ProcessorContext
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock

import scala.collection.mutable.ListBuffer

class AdditionalTagsProcessorSupplierSpec extends FeatureSpec {

  feature("Additional Tags processor supplier should add additional tags") {

    val appConfiguration = mockAppConfig

    scenario("should add additional tags if any and forward metricData") {

      Given("an additional tags processor")

      val keys = Map("product" -> "haystack")
      val listBuffer = ListBuffer[MetricData]()
      val metricData = getMetricData("success-count", keys, 10, System.currentTimeMillis())
      val additionalTagsProcessor = new AdditionalTagsProcessor(appConfiguration.additionalTags)
      val processorContext = Mockito.mock(classOf[ProcessorContext])
      additionalTagsProcessor.init(processorContext)

      when(processorContext.forward(anyString(), any(classOf[MetricData]))).thenAnswer((invocationOnMock: InvocationOnMock) => {
        listBuffer += invocationOnMock.getArgument[MetricData](1)
            })


      When("additional tags processor is passed with metric data")
      additionalTagsProcessor.process("abc", metricData)


      Then("additional tags are added to metric data")
      val metricDataForwaded = listBuffer.toList
      metricDataForwaded.length shouldEqual 1
      metricDataForwaded.head.getMetricDefinition.getTags.getKv.containsKey("k1") shouldEqual true
      metricDataForwaded.head.getMetricDefinition.getTags.getKv.containsKey("k2") shouldEqual true

    }


    scenario("should not add additional tags if additional tags and forward metricData") {

      Given("an additional tags processor")

      val keys = Map("product" -> "haystack")
      val listBuffer = ListBuffer[MetricData]()
      val metricData = getMetricData("success-count", keys, 10, System.currentTimeMillis())
      val additionalTagsProcessor = new AdditionalTagsProcessor(Map())
      val processorContext = Mockito.mock(classOf[ProcessorContext])
      additionalTagsProcessor.init(processorContext)

      when(processorContext.forward(anyString(), any(classOf[MetricData]))).thenAnswer((invocationOnMock: InvocationOnMock) => {
        listBuffer += invocationOnMock.getArgument[MetricData](1)
      })


      When("additional tags processor is passed with metric data")
      additionalTagsProcessor.process("abc", metricData)


      Then("additional tags are added to metric data")
      val metricDataForwaded = listBuffer.toList
      metricDataForwaded.length shouldEqual 1
      metricDataForwaded.head shouldEqual metricData

    }

  }


}
