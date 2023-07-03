package com.expedia.www.haystack.commons.util

import com.expedia.metrics.{MetricDefinition, TagCollection}

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap

object MetricDefinitionKeyGenerator {

  def generateKey(metricDefinition: MetricDefinition): String = {
    List(s"key=${metricDefinition.getKey}", getTagsAsString(metricDefinition.getTags),
      getTagsAsString(metricDefinition.getMeta)).filter(!_.isEmpty).mkString(",")
  }

  def getTagsAsString(tags: TagCollection): String = {
    ListMap(tags.getKv.asScala.toSeq.sortBy(_._1): _*).map(tag => s"${tag._1}=${tag._2}").mkString(",")
  }

}
