/*
 * Copyright 2019 Expedia Group
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

const fetcher = require('./fetcher');
const extractor = require('./graphDataExtractor');

const connector = {};

function fetchServiceInsights(options) {
    const {serviceName, operationName, traceId, startTime, endTime, limit, relationship} = options;
    const relationshipFilter = relationship ? relationship.split(',') : [];
    return fetcher(serviceName)
        .fetch({serviceName, operationName, traceId, startTime, endTime, limit})
        .then((data) => extractor.extractNodesAndLinks(data, relationshipFilter));
}

/**
 * getServiceInsightsForService
 *
 * @param {object} options - Object with the following options:
 * - serviceName - service to get Service Insights for (required, unless traceId is provided)
 * - operationName - operation to filter for (optional)
 * - traceId - single trace to get Service Insights for (optional)
 * - startTime - filter for traces after this time in microseconds (required)
 * - endTime - filter for traces before this time in microseconds (required)
 * - limit - override the default config limit on number of traces to fetch (optional)
 * - relationship - comma-separated list of relationships to include (optional)
 */
connector.getServiceInsightsForService = (options) => Q.fcall(() => fetchServiceInsights(options));

module.exports = connector;
