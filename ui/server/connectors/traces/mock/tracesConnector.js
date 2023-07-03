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

const {generateMockTraceSpans, getMockServiceNames, getMockOperationNames} = require('./tracesGenerator');

const connector = {};

connector.getServices = () => Q.fcall(() => getMockServiceNames());

connector.getOperations = () => Q.fcall(() => getMockOperationNames());

function getValue(min, max) {
    return _.round(Math.random() * (max - min) + min, 0);
}

function getRandomValues(granularity, dataPoints, from) {
    const valuesArr = [];
    _.range(dataPoints).forEach((i) => valuesArr.push({x: from + i * granularity, y: getValue(0, 3000)}));
    return valuesArr;
}

connector.getTimeline = (query) =>
    Q.fcall(() => {
        const granularity = (query.endTime - query.startTime) / 15;
        const range = query.endTime - query.startTime;
        const points = range / granularity;

        return getRandomValues(granularity, points, parseInt(query.startTime, 10));
    });

connector.getSearchableKeys = () =>
    Q.fcall(() => ({
        serviceName: {isRangeQuery: false},
        operationName: {isRangeQuery: false},
        traceId: {isRangeQuery: false}
    }));

// TODO: support these such that this connector can be used by Traces tab (currently only supports Service Insights tab)
connector.getLatencyCost = () =>
    Q.fcall(() => {
        throw new Error('Unsupported by mock connector.');
    });
connector.getTrace = () =>
    Q.fcall(() => {
        throw new Error('Unsupported by mock connector.');
    });
connector.getRawTrace = () =>
    Q.fcall(() => {
        throw new Error('Unsupported by mock connector.');
    });
connector.getRawSpan = () =>
    Q.fcall(() => {
        throw new Error('Unsupported by mock connector.');
    });

connector.findTraces = () =>
    Q.fcall(() => {
        throw new Error('Unsupported by mock connector.');
    });

connector.findTracesFlat = () => Q.fcall(() => generateMockTraceSpans());

connector.getRawTraces = () =>
    Q.fcall(() => {
        throw new Error('Unsupported by mock connector.');
    });

module.exports = connector;
