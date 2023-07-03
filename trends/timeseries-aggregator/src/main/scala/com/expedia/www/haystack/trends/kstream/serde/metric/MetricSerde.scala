package com.expedia.www.haystack.trends.kstream.serde.metric

import com.expedia.www.haystack.trends.aggregation.metrics.Metric

trait MetricSerde {
  def serialize(metric:Metric): Array[Byte]
  def deserialize(data: Array[Byte]) : Metric
}
