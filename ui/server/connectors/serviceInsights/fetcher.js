/*
 * Copyright 2019 Expedia Group
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

const Q = require('q');
const _ = require('lodash');

const config = require('../../config/config');

const tracesConnector = require(`../../connectors/traces/${config.connectors.traces.connectorName}/tracesConnector`); // eslint-disable-line import/no-dynamic-require
const logger = require('../../utils/logger').withIdentifier('fetcher.serviceInsights');
const metrics = require('../../utils/metrics');

const TRACE_LIMIT = config.connectors.serviceInsights.traceLimit;

const fetcher = (fetcherName) => ({
    fetch(options) {
        const {serviceName, operationName, traceId, startTime, endTime} = options;

        // local vars
        const deferred = Q.defer();
        const timer = metrics.timer(`fetcher_${fetcherName}`).start();

        // use given limit or default
        const limit = options.limit || TRACE_LIMIT;


        // traces api expects strings
        const spanLevelFilters = JSON.stringify([JSON.stringify({
            serviceName,
            operationName,
            traceId
        })]);

        // use traces connector
        tracesConnector
            .findTracesFlat({
                startTime,
                endTime,
                limit,
                spanLevelFilters
            })
            .then((traces) => {
                // check for 1 or more traces
                const hasTraces = traces && traces.length > 0;
                if (hasTraces) {
                    // flat map childSpans to flat array
                    const spans = _.flatten(traces);

                    // complete timer
                    timer.end();

                    // log success message
                    logger.info(`fetch successful: ${fetcherName}`);

                    // resolve promise
                    deferred.resolve({
                        serviceName,
                        spans,
                        traceLimitReached: !!(traces.length === limit)
                    });
                } else {
                    // log no traces found message
                    logger.info(`fetch successful with no traces: ${fetcherName}`);

                    // complete timer
                    timer.end();

                    // resolve promise
                    deferred.resolve({serviceName, spans: [], traceLimitReached: false});
                }
            });

        // return promise
        return deferred.promise;
    }
});

module.exports = fetcher;
