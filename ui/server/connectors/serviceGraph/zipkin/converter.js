/*
 * Copyright 2018 Expedia Group
 *
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
 
const config = require('../../../config/config');

// Zipkin dependency data is bucketed daily. Also the data is usually sampled.
// This means that people interpreting this will likely be misled, mistaking
// traced requests per bucket for requests per window. For example, a bucket
// holds up to one day of data, but if just past midnight it could be only
// one second of data!
const WINDOW_SIZE_IN_SECS =  config.connectors.serviceGraph && config.connectors.serviceGraph.windowSizeInSecs;

// In Zipkin dependency link, we do not have tags or operation name. So we
// return a constant operation name of "unknown" for now.
function toHaystackEdge(dependencyLink) {
  const res = {
      source: {
          name: dependencyLink.parent
      },
      destination: {
          name: dependencyLink.child
      },
      // Zipkin doesn't aggregate operation -> operation, rather service -> service
      operation: 'unknown',
      stats: {
          count: (dependencyLink.callCount / WINDOW_SIZE_IN_SECS),
          errorCount: 0
      }
  };
  if (dependencyLink.errorCount) {
    res.stats.errorCount = (dependencyLink.errorCount  / WINDOW_SIZE_IN_SECS);
  }
  return res;
}

function toHaystackServiceEdges(dependencyLinks) {
  return dependencyLinks.map(dependencyLink => toHaystackEdge(dependencyLink));
}

const converter = {};

// exported for testing
converter.toHaystackEdge = toHaystackEdge;
converter.toHaystackServiceEdges = toHaystackServiceEdges;

module.exports = converter;
