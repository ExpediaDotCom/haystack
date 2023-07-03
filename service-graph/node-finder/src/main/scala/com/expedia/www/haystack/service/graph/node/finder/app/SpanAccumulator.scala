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

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.commons.graph.GraphEdgeTagCollector
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.service.graph.node.finder.model.{LightSpan, ServiceNodeMetadata, SpanPair, SpanPairBuilder}
import com.expedia.www.haystack.service.graph.node.finder.utils.SpanUtils
import com.netflix.servo.util.VisibleForTesting
import org.apache.commons.lang3.StringUtils
import org.apache.kafka.streams.processor._
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory

import scala.collection.mutable

class SpanAccumulatorSupplier(storeName: String,
                              accumulatorInterval: Int,
                              tagCollector: GraphEdgeTagCollector) extends
  ProcessorSupplier[String, Span] {
  override def get(): Processor[String, Span] = new SpanAccumulator(storeName, accumulatorInterval, tagCollector)
}

class SpanAccumulator(storeName: String,
                      accumulatorInterval: Int,
                      tagCollector: GraphEdgeTagCollector)
  extends Processor[String, Span] with MetricsSupport {

  private val LOGGER = LoggerFactory.getLogger(classOf[SpanAccumulator])
  private val processMeter = metricRegistry.meter("span.accumulator.process")
  private val aggregateMeter = metricRegistry.meter("span.accumulator.aggregate")
  private val forwardMeter = metricRegistry.meter("span.accumulator.emit")

  // map to store spanId -> span data. Used for checking child-parent relationship
  private var spanMap = mutable.HashMap[String, mutable.HashSet[LightSpan]]()

  // map to store parentSpanId -> span data. Used for checking child-parent relationship
  private var parentSpanMap = mutable.HashMap[String, mutable.HashSet[LightSpan]]()

  private var processorContext: ProcessorContext = _
  private var metadataStore: KeyValueStore[String, ServiceNodeMetadata] = _

  override def init(context: ProcessorContext): Unit = {
    processorContext = context
    context.schedule(accumulatorInterval, PunctuationType.STREAM_TIME, getPunctuator(context))
    metadataStore = context.getStateStore(storeName).asInstanceOf[KeyValueStore[String, ServiceNodeMetadata]]
    LOGGER.info(s"${this.getClass.getSimpleName} initialized")
  }

  override def process(key: String, span: Span): Unit = {
    processMeter.mark()

    //find the span type
    val spanType = SpanUtils.getSpanType(span)

    if (SpanUtils.isAccumulableSpan(span)) {

      val lightSpan = LightSpan(span.getSpanId,
        span.getParentSpanId,
        span.getStartTime / 1000, //startTime is in microseconds, so divide it by 1000 to send MS
        span.getServiceName,
        span.getOperationName,
        span.getDuration,
        spanType,
        tagCollector.collectTags(span))

      //add new light span to the span map and parent map
      spanMap.getOrElseUpdate(span.getSpanId, mutable.HashSet[LightSpan]()).add(lightSpan)
      if (StringUtils.isNotEmpty(span.getParentSpanId)) {
        parentSpanMap.getOrElseUpdate(span.getParentSpanId, mutable.HashSet[LightSpan]()).add(lightSpan)
      }

      processSpan(lightSpan) foreach {
        spanPair =>
          if (isValidMerge(spanPair)) forward(processorContext, spanPair)
          cleanupSpanMap(spanPair)
          aggregateMeter.mark()
      }
    }
  }

  override def punctuate(timestamp: Long): Unit = {}

  override def close(): Unit = {}

  //forward all complete spans
  private def forward(context: ProcessorContext, spanPair: SpanPair): Unit = {
    LOGGER.debug("Forwarding complete SpanPair: {}", spanPair)
    context.forward(spanPair.getId, spanPair)
    forwardMeter.mark()
  }

  /**
    * process the given light span to check whether it can form a span pair
    *
    * @param span incoming span to be processed
    * @return sequence of span pair whether complete or incomplete
    */
  private def processSpan(span: LightSpan): Seq[SpanPair] = {
    val possibleSpanPairs = spanMap(span.spanId)

    //matched span, whether complete or incomplete based on their service
    val spanPairs = mutable.ListBuffer[SpanPair]()

    //same spanId is present in spanMap
    if (possibleSpanPairs.size > 1) {
      spanPairs += SpanPairBuilder.createSpanPair(possibleSpanPairs.head, possibleSpanPairs.tail.head)
    } else {
      //look for its parent ie if its parentId is in span map
      spanMap.get(span.parentSpanId) match {
        case Some(parentSpan) => spanPairs += SpanPairBuilder.createSpanPair(parentSpan.head, span)
        case _ =>
      }
      //look for its child ie if its spanId is in parent map
      parentSpanMap.get(span.spanId) match {
        case Some(childSpans) => spanPairs ++= childSpans.map(childSpan => SpanPairBuilder.createSpanPair(childSpan, span))
        case _ =>
      }
    }
    spanPairs
  }

  @VisibleForTesting
  def getPunctuator(context: ProcessorContext): Punctuator = {
    (timestamp: Long) => {
      //we keep a span only until timeToKeep time and leave the rest in place and see
      //if they get their matching span pair before timeToKeep
      val timeToKeep = timestamp - accumulatorInterval  //in milliSec
      LOGGER.debug(s"Punctuate called with $timestamp. TimeToKeep is $timeToKeep. Map sizes are ${spanMap.values.flatten[LightSpan].size} & ${parentSpanMap.size}")

      //if the span is within the time limit, we will keep them, otherwise discard
      spanMap = spanMap.filter {
        case (_, ls) => ls.exists(sp => sp.isLaterThan(timeToKeep))
      }
      parentSpanMap = parentSpanMap.filter {
        case (_, ls) => ls.exists(sp => sp.isLaterThan(timeToKeep))
      }

      // commit the current processing progress
      context.commit()
    }
  }

  @VisibleForTesting
  def spanCount: Int = spanMap.values.flatten[LightSpan].size

  @VisibleForTesting
  def internalSpanMap = spanMap.toMap

  /**
    * spans in a span pair to be cleaned up from the parent span map.
    * Not removing it from spanMap since there could be multiple children for it in case of same service.
    *
    * @param spanPair span pair with client / server  spans
    */
  private def cleanupSpanMap(spanPair: SpanPair): Unit = {
    spanPair.getBackingSpans.foreach(ls => {
      parentSpanMap.remove(ls.spanId)
    })
  }

  private def isValidMerge(spanPair: SpanPair): Boolean = {
    if (spanPair.isComplete) {
      val metadata = metadataStore.get(spanPair.getServerSpan.serviceName)
      if (metadata == null) {
        true
      } else {
        // if current merge matches with the recorded style, or it is shared span merge style, then accept it
        (metadata.useSharedSpan == spanPair.IsSharedSpan) || spanPair.IsSharedSpan
      }
    } else {
      false
    }
  }
}
