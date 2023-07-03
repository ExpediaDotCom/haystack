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

package com.expedia.www.haystack.trace.reader.readers.transformers

import com.expedia.open.tracing.Span
import com.expedia.www.haystack.trace.commons.utils.SpanUtils
import com.expedia.www.haystack.trace.reader.readers.utils.{MutableSpanForest, SpanTree}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.collection.{Seq, mutable}

/**
  * Fixes clock skew between parent and child spans
  * If any child spans reports a startTime earlier then parent span's startTime or
  * an endTime later then the parent span's endTime, then
  * the child span's startTime or endTime will be shifted. Where the shift is
  * set equal to the parent's startTime or endTime depending on which is off.
  */
class ClockSkewFromParentTransformer extends SpanTreeTransformer {

  case class SpanTreeWithParent(spanTree: SpanTree, parent: Option[Span])

  override def transform(forest: MutableSpanForest): MutableSpanForest = {
    val underlyingSpans = new mutable.ListBuffer[Span]
    forest.getAllTrees.foreach(tree => {
      adjustSkew(underlyingSpans, List(SpanTreeWithParent(tree, None)))
    })
    forest.updateUnderlyingSpans(underlyingSpans)
  }

  @tailrec
  private def adjustSkew(fixedSpans: ListBuffer[Span], spanTrees: Seq[SpanTreeWithParent]): Unit = {
    if (spanTrees.isEmpty) return

    // collect the child trees that need to be corrected for clock skew
    val childTrees = mutable.ListBuffer[SpanTreeWithParent]()

    spanTrees.foreach(e => {
      val rootSpan = e.spanTree.span
      var adjustedSpan = rootSpan
      e.parent match {
        case Some(parentSpan) =>
          adjustedSpan = adjustSpan(rootSpan, parentSpan)
          fixedSpans += adjustedSpan
        case _ => fixedSpans += rootSpan
      }
      childTrees ++= e.spanTree.children.map(tree => SpanTreeWithParent(tree, Some(adjustedSpan)))
    })

    adjustSkew(fixedSpans, childTrees)
  }

  private def adjustSpan(child: Span, parent: Span): Span = {
    var shift = 0L
    if (child.getStartTime < parent.getStartTime) {
      shift = parent.getStartTime - child.getStartTime
    }
    val childEndTime = SpanUtils.getEndTime(child)
    val parentEndTime = SpanUtils.getEndTime(parent)
    if (parentEndTime < childEndTime + shift) {
      shift = parentEndTime - childEndTime
    }
    if (shift == 0L) {
      child
    } else {
      Span.newBuilder(child).setStartTime(child.getStartTime + shift).build()
    }
  }
}
