package com.expedia.www.haystack.commons.kstreams

import com.codahale.metrics.Histogram
import com.expedia.www.haystack.commons.metrics.MetricsSupport

trait IteratorAgeMetricSupport extends MetricsSupport {

  val iteratorAge: Histogram = metricRegistry.histogram("kafka.iterator.age.ms")

  def updateIteratorAge(timeInMs: Long): Unit = {
    iteratorAge.update(System.currentTimeMillis() - timeInMs)
  }
}
