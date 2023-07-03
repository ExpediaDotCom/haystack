/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.expedia.www.haystack.commons.metrics

import com.codahale.metrics.{Metric, MetricRegistry}

trait MetricsSupport {
  val metricRegistry: MetricRegistry = MetricsRegistries.metricRegistry
}

object MetricsRegistries {

  val metricRegistry = new MetricRegistry()

  implicit class MetricRegistryExtension(val metricRegistry: MetricRegistry) extends AnyVal {

    def getOrAddGauge[T](expectedName: String, gauge: com.codahale.metrics.Gauge[T]): Boolean = {
      val existingGauges = metricRegistry.getGauges((existingName: String, _: Metric) => {
        existingName.equalsIgnoreCase(expectedName)
      })

      if (existingGauges == null || existingGauges.size() == 0) {
        metricRegistry.register(expectedName, gauge)
        true
      } else {
        false
      }
    }
  }
}
