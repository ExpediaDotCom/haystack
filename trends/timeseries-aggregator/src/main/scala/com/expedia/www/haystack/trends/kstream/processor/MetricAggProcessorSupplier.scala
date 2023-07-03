/*
 *
 *     Copyright 2017 Expedia, Inc.
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
package com.expedia.www.haystack.trends.kstream.processor

import com.codahale.metrics.{Counter, Meter}
import com.expedia.metrics.MetricData
import com.expedia.www.haystack.commons.entities.encoders.Encoder
import com.expedia.www.haystack.commons.entities.Interval
import com.expedia.www.haystack.commons.metrics.MetricsSupport
import com.expedia.www.haystack.commons.util.MetricDefinitionKeyGenerator.generateKey
import com.expedia.www.haystack.trends.aggregation.TrendMetric
import com.expedia.www.haystack.trends.aggregation.metrics._
import com.expedia.www.haystack.trends.aggregation.rules.MetricRuleEngine
import org.apache.kafka.streams.kstream.internals._
import org.apache.kafka.streams.processor.{AbstractProcessor, Processor, ProcessorContext}
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory

class MetricAggProcessorSupplier(trendMetricStoreName: String, encoder: Encoder) extends KStreamAggProcessorSupplier[String, String, MetricData, TrendMetric] with MetricRuleEngine with MetricsSupport {

  private var sendOldValues: Boolean = false
  private val LOGGER = LoggerFactory.getLogger(this.getClass)

  def get: Processor[String, MetricData] = {
    new MetricAggProcessor(trendMetricStoreName)
  }


  def enableSendingOldValues() {
    sendOldValues = true
  }

  override def view(): KTableValueGetterSupplier[String, TrendMetric] = new KTableValueGetterSupplier[String, TrendMetric]() {

    override def get(): KTableValueGetter[String, TrendMetric] = new TrendMetricAggregateValueGetter()

    override def storeNames(): Array[String] = Array[String](trendMetricStoreName)

    private class TrendMetricAggregateValueGetter extends KTableValueGetter[String, TrendMetric] {

      private var store: KeyValueStore[String, TrendMetric] = _

      @SuppressWarnings(Array("unchecked")) def init(context: ProcessorContext) {
        store = context.getStateStore(trendMetricStoreName).asInstanceOf[KeyValueStore[String, TrendMetric]]
      }

      def get(key: String): TrendMetric = store.get(key)
    }
  }

  /**
    * This is the Processor which contains the map of unique trends consumed from the assigned partition and the corresponding trend metric for each trend
    * Each trend is uniquely identified by the metricPoint key - which is a combination of the name and the list of tags. Its backed by a state store which keeps this map and has the
    * ability to restore the map if/when the app restarts or when the assigned kafka partitions change
    *
    * @param trendMetricStoreName - name of the key-value state store
    */
  private class MetricAggProcessor(trendMetricStoreName: String) extends AbstractProcessor[String, MetricData] {
    private var trendMetricStore: KeyValueStore[String, TrendMetric] = _


    private var trendsCount: Counter = _
    private val invalidMetricPointMeter: Meter = metricRegistry.meter("metricprocessor.invalid")

    @SuppressWarnings(Array("unchecked"))
    override def init(context: ProcessorContext) {
      super.init(context)
      trendsCount = metricRegistry.counter(s"metricprocessor.trendcount.${context.taskId()}")
      trendMetricStore = context.getStateStore(trendMetricStoreName).asInstanceOf[KeyValueStore[String, TrendMetric]]
      trendsCount.dec(trendsCount.getCount)
      trendsCount.inc(trendMetricStore.approximateNumEntries())
      LOGGER.info(s"Triggering init for metric agg processor for task id ${context.taskId()}")
    }

    /**
      * tries to fetch the trend metric based on the key, if it exists it updates the trend metric else it tries to create a new trend metric and adds it to the store      *
      *
      * @param key         - key in the kafka record - should be metricPoint.getKey
      * @param metricData  - metricData
      */
    def process(key: String, metricData: MetricData): Unit = {
      if (key != null && metricData.getValue > 0) {

        // first get the matching windows
        Option(trendMetricStore.get(key)).orElse(createTrendMetric(metricData)).foreach(trendMetric => {
          trendMetric.compute(metricData)

          /*
         we finally put the updated trend metric back to the store since we want the changelog the state store with the latest state of the trend metric, if we don't put the metric
         back and update the mutable metric, the kstreams would not capture the change and app wouldn't be able to restore to the same state when the app comes back again.
         */
          if (trendMetric.shouldLogToStateStore) {
            trendMetricStore.put(key, trendMetric)
          }

          //retrieve the computed metrics and push it to the kafka topic.
          trendMetric.getComputedMetricPoints(metricData).foreach(metricPoint => {
            context().forward(generateKey(metricData.getMetricDefinition), metricPoint)
          })
        })
      } else {
        invalidMetricPointMeter.mark()
      }
    }

    private def createTrendMetric(value: MetricData): Option[TrendMetric] = {
      findMatchingMetric(value).map {
        case AggregationType.Histogram =>
          trendsCount.inc()
          TrendMetric.createTrendMetric(Interval.all, value, HistogramMetricFactory)
        case AggregationType.Count =>
          trendsCount.inc()
          TrendMetric.createTrendMetric(Interval.all, value, CountMetricFactory)
      }
    }
  }
}
