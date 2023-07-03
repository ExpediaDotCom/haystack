package com.expedia.www.haystack.trends.kstream.serde.metric

import java.nio.ByteBuffer

import com.expedia.www.haystack.commons.entities.Interval
import com.expedia.www.haystack.commons.entities.Interval.Interval
import com.expedia.www.haystack.trends.aggregation.TrendHdrHistogram
import com.expedia.www.haystack.trends.aggregation.metrics.{HistogramMetric, Metric}
import com.expedia.www.haystack.trends.config.AppConfiguration
import com.expedia.www.haystack.trends.config.entities.HistogramUnit
import org.HdrHistogram.Histogram
import org.msgpack.core.MessagePack
import org.msgpack.value.{Value, ValueFactory}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Serde which lets us serialize and deserilize the histogram metric, this is used when we serialize/deserialize the windowedMetric which can internally contain count or histogram metric
  * It uses messagepack to pack the object into bytes
  */
object HistogramMetricSerde extends MetricSerde {
  private val LOGGER = LoggerFactory.getLogger(HistogramMetricSerde.getClass)

  private val intHistogramKey = "intHistogram"
  private val intervalKey = "interval"
  private val unitKey = "unit"
  private val highestTrackableValKey = "highestTrackableVal"

  override def serialize(metric: Metric): Array[Byte] = {

    val histogramMetric = metric.asInstanceOf[HistogramMetric]
    val packer = MessagePack.newDefaultBufferPacker()
    val runningHistogram = histogramMetric.getRunningHistogram
    val serializedHistogram = ByteBuffer.allocate(runningHistogram.getEstimatedFootprintInBytes)
    runningHistogram.encodeIntoByteBuffer(serializedHistogram)
    val metricData = Map[Value, Value](
      ValueFactory.newString(intHistogramKey) -> ValueFactory.newBinary(serializedHistogram.array()),
      ValueFactory.newString(intervalKey) -> ValueFactory.newString(metric.getMetricInterval.name),
      ValueFactory.newString(unitKey) -> ValueFactory.newString(histogramMetric.getRunningHistogram.unit.toString),
      ValueFactory.newString(highestTrackableValKey) -> ValueFactory.newInteger(histogramMetric.getRunningHistogram.getHighestTrackableValue)
    )
    packer.packValue(ValueFactory.newMap(metricData.asJava))
    packer.toByteArray

  }

  override def deserialize(data: Array[Byte]): HistogramMetric = {
    val metric = MessagePack.newDefaultUnpacker(data).unpackValue().asMapValue().map()
    val serializedHistogram = metric.get(ValueFactory.newString(intHistogramKey)).asBinaryValue().asByteArray
    val interval: Interval = Interval.fromName(metric.get(ValueFactory.newString(intervalKey)).asStringValue().toString)

    // before the unit concept is introduced, default histogram recorded value's unit was micros
    val unitValue = metric.get(ValueFactory.newString(unitKey))
    val histogramUnit = if(unitValue == null) HistogramUnit.default else HistogramUnit.from(unitValue.asStringValue().toString)

    val highestTrackableVal = metric.get(ValueFactory.newString(highestTrackableValKey))
    val maxTrackableHistogramVal = if (highestTrackableVal == null) Int.MaxValue else highestTrackableVal.asIntegerValue().toInt

    try {
      val hdrHistogram = Histogram.decodeFromByteBuffer(ByteBuffer.wrap(serializedHistogram), maxTrackableHistogramVal)
      new HistogramMetric(interval, TrendHdrHistogram(hdrHistogram, histogramUnit))
    } catch {
      case ex: Exception =>
        LOGGER.error("Fail to deserialize the hdr histogram with error", ex)
        // create a default hdr histogram using the config
        new HistogramMetric(interval, new TrendHdrHistogram(AppConfiguration.histogramMetricConfiguration))
    }
  }
}
