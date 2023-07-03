package com.expedia.www.haystack.trends.aggregation.entities

/**
  * This enumeration contains all the supported statistics we want to emit for a given histogram metric
  */
object StatValue extends Enumeration {
  type StatValue = Value

  val MEAN = Value("mean")
  val MAX = Value("max")
  val MIN = Value("min")
  val COUNT = Value("count")
  val STDDEV = Value("std")
  val PERCENTILE_95 = Value("*_95")
  val PERCENTILE_99 = Value("*_99")
  val MEDIAN = Value("*_50")

}
