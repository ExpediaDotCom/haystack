/*
 * Copyright 2018 Expedia Group
 *
 *         Licensed under the Apache License, Version 2.0 (the 'License');
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an 'AS IS' BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */

const Q = require('q');

const fetcher = require('../../operations/restFetcher');
const config = require('../../../config/config');
const converter = require('./converter');
const extractor = require('../haystack/graphDataExtractor');

const dependenciesFetcher = fetcher('getDependencies');

const connector = {};
const baseZipkinUrl = config.connectors.serviceGraph.zipkinUrl;

function fetchServiceGraph(from, to) {
  const endTs = parseInt(to, 10);
  const lookback = endTs - parseInt(from, 10);

  return dependenciesFetcher
        .fetch(`${baseZipkinUrl}/dependencies?endTs=${endTs}&lookback=${lookback}`)
        .then(data => extractor.extractGraphFromEdges(converter.toHaystackServiceEdges(data)));
}

connector.getServiceGraphForTimeLine = (from, to) => Q.fcall(() => fetchServiceGraph(from, to));

module.exports = connector;
