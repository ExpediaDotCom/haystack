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
package com.expedia.www.haystack.service.graph.node.finder.app

import com.expedia.metrics.MetricData
import com.expedia.www.haystack.TestSpec
import org.apache.kafka.streams.processor.ProcessorContext
import org.easymock.EasyMock._

class LatencyProducerSpec extends TestSpec {
  describe("latency producer") {
    it("should produce latency metric for complete SpanPair") {
      Given("a valid SpanPair instance")
      val spanPair = validSpanPair()
      val context = mock[ProcessorContext]
      val latencyProducer = new LatencyProducer
      When("process is invoked with a complete SpanPair")
      expecting {
        context.forward(anyString(), isA(classOf[MetricData])).once()
        context.commit().once()
      }
      replay(context)
      latencyProducer.init(context)
      latencyProducer.process(spanPair.getId, spanPair)
      Then("it should produce a metric point in the context")
      verify(context)
    }
    it("should produce no metrics for invalid SpanPair") {
      Given("an incomplete SpanPair instance")
      val spanPair = invalidSpanPair()
      val context = mock[ProcessorContext]
      val latencyProducer = new LatencyProducer
      When("process is invoked with a complete SpanPair")
      expecting {
        context.commit().once()
      }
      replay(context)
      latencyProducer.init(context)
      latencyProducer.process(spanPair.getId, spanPair)
      Then("it should produce no metric points in the context")
      verify(context)
    }
  }
  describe("latency producer supplier") {
    it("should supply a valid producer") {
      Given("a supplier instance")
      val supplier = new LatencyProducerSupplier
      When("a producer is request")
      val producer = supplier.get()
      Then("should yield a valid producer")
      producer should not be null
    }
  }
}
