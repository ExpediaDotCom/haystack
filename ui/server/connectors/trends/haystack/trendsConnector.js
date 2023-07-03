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

const Q = require('q');
const config = require('../../../config/config');
const _ = require('lodash');
const fetcher = require('../../operations/restFetcher');
const MetricpointNameEncoder = require('../../utils/encoders/MetricpointNameEncoder');

const trendsFetcher = fetcher('trends');

const defaultFrom = Math.ceil((Date.now() / 1000) - (60 * 60));
const defaultUntil = Math.ceil(Date.now() / 1000);
const connector = {};
const metricTankUrl = config.connectors.trends && config.connectors.trends.metricTankUrl;
const metricpointNameEncoder = new MetricpointNameEncoder(config.encoder);

function createServicesOperationsTarget(services, operations, timeWindow, metricStats, metricNames) {
    return encodeURIComponent(`seriesByTag('name=${metricNames}','serviceName=${services}','operationName=${operations}','interval=${timeWindow}','stat=${metricStats}')`);
}

function createOperationTarget(service, operationName, timeWindow, metricStats, metricNames) {
    return encodeURIComponent(`seriesByTag('name=${metricNames}','serviceName=${service}','operationName=${operationName}','interval=${timeWindow}','stat=${metricStats}')`);
}

function getServiceTargetStat(service, timeWindow, metricStats, metricNames) {
    return encodeURIComponent(`seriesByTag('name=${metricNames}','serviceName=${service}','interval=${timeWindow}','stat=${metricStats}')`);
}

function convertGranularityToTimeWindow(timespan) {
    switch (timespan) {
        case '60000':
            return 'OneMinute';
        case '300000':
            return 'FiveMinute';
        case '900000':
            return 'FifteenMinute';
        case '3600000':
            return 'OneHour';
        default:
            return 'OneMinute';
    }
}

function convertEpochTimeInSecondsToMillis(timestamp) {
    return timestamp * 1000;
}

function toMilliseconds(micro) {
    return Math.ceil(micro / 1000);
}

function groupResponseByServiceOperation(data) {
    return data.map((op) => {
        const tags = op.tags;

        const serviceName = tags.serviceName ? metricpointNameEncoder.decodeMetricpointName(tags.serviceName) : null;
        const operationName = tags.operationName ? metricpointNameEncoder.decodeMetricpointName(tags.operationName) : null;
        const trendStat = `${tags.stat}.${tags.name}`;

        return {
            serviceName,
            operationName,
            [trendStat]: op.datapoints.map(datapoint => ({
                value: datapoint[0] ? datapoint[0] : 0,
                timestamp: convertEpochTimeInSecondsToMillis(datapoint[1])
            }))
        };
    });
}

function fetchTrendValues(target, from, until) {
    return trendsFetcher
        .fetch(`${metricTankUrl}/render?target=${target}&from=${from}&to=${until}`, { 'x-org-id': 1})
        .then(data => groupResponseByServiceOperation(data));
}

function extractTrendPointsForSingleServiceOperation(operationTrends, trendStat) {
    const dataPoints = operationTrends.find(trend => trendStat in trend);
    const trendStatDataPoints = dataPoints ? dataPoints[trendStat] : [];

    if (trendStatDataPoints.length && trendStatDataPoints[trendStatDataPoints.length - 1].value === null) {
        trendStatDataPoints.pop();
    }

    return trendStatDataPoints;
}

function dataPointsSum(dataPoints) {
    return dataPoints.reduce(((accumulator, dataPoint) => (accumulator + dataPoint.value)), 0);
}

function toSuccessPercent(successPoints, failurePoints) {
    const successCount = successPoints.reduce(((accumulator, dataPoint) => (accumulator + dataPoint.value)), 0);
    const failureCount = failurePoints.reduce(((accumulator, dataPoint) => (accumulator + dataPoint.value)), 0);

    return 100 - ((failureCount / (successCount + failureCount)) * 100);
}

function toSuccessPercentPoints(successCount, failureCount) {
    const successTimestamps = successCount.map(point => point.timestamp);
    const failureTimestamps = failureCount.map(point => point.timestamp);
    const timestamps = _.uniq([...successTimestamps, ...failureTimestamps]);

    return _.compact(timestamps.map((timestamp) => {
        const successItem = _.find(successCount, x => (x.timestamp === timestamp));
        const successVal = (successItem && successItem.value) ? successItem.value : 0;

        const failureItem = _.find(failureCount, x => (x.timestamp === timestamp));
        const failureVal = (failureItem && failureItem.value) ? failureItem.value : 0;

        if (successVal + failureVal) {
            return {
                value: (100 - ((failureVal / (successVal + failureVal)) * 100)),
                timestamp
            };
        }
        return null;
    }));
}

function toCountPoints(successCount, failureCount) {
    const successTimestamps = successCount.map(point => point.timestamp);
    const failureTimestamps = failureCount.map(point => point.timestamp);
    const timestamps = _.uniq([...successTimestamps, ...failureTimestamps]);

    return _.compact(timestamps.map((timestamp) => {
        const successItem = _.find(successCount, x => (x.timestamp === timestamp));
        const successVal = (successItem && successItem.value) ? successItem.value : 0;

        const failureItem = _.find(failureCount, x => (x.timestamp === timestamp));
        const failureVal = (failureItem && failureItem.value) ? failureItem.value : 0;

        return {
            value: successVal + failureVal,
            timestamp
        };
    }));
}

function extractServicePerfStats({successValues, failureValues, tp99Values}) {
    const trendResults = [];

    const groupedByServiceName = _.groupBy(successValues.concat(successValues, failureValues, tp99Values), val => val.serviceName);
    Object.keys(groupedByServiceName).forEach((service) => {
        const serviceTrends = groupedByServiceName[service];
        const successCount = dataPointsSum(extractTrendPointsForSingleServiceOperation(serviceTrends, 'count.success-span'));
        const failureCount = dataPointsSum(extractTrendPointsForSingleServiceOperation(serviceTrends, 'count.failure-span'));
        const successPercent = ((successCount / (successCount + failureCount)) * 100);

        const opKV = {
            serviceName: service,
            successPercent,
            failureCount,
            successCount,
            totalCount: successCount + failureCount
        };

        trendResults.push(opKV);
    });
    return trendResults;
}

function extractServiceSummary(serviceTrends) {
    const successCount = extractTrendPointsForSingleServiceOperation(serviceTrends, 'count.success-span');
    const failureCount = extractTrendPointsForSingleServiceOperation(serviceTrends, 'count.failure-span');
    const countPoints = toCountPoints(successCount, failureCount);
    const tp99DurationPoints = extractTrendPointsForSingleServiceOperation(serviceTrends, '*_99.duration');
    const latestTp99DurationDatapoint = _.findLast(tp99DurationPoints, point => point.value);

    return [{
        type: 'Incoming Requests',
        totalCount: dataPointsSum(countPoints),
        countPoints,
        avgSuccessPercent: toSuccessPercent(successCount, failureCount),
        successPercentPoints: toSuccessPercentPoints(successCount, failureCount),
        latestTp99Duration: latestTp99DurationDatapoint && latestTp99DurationDatapoint.value,
        tp99DurationPoints
    }];
}

function extractOperationSummary(values) {
    const groupedByOperationName = _.groupBy(values, val => val.operationName);
    return Object.keys(groupedByOperationName).map((operationName) => {
        const operationTrends = groupedByOperationName[operationName];

        const successPoints = extractTrendPointsForSingleServiceOperation(operationTrends, 'count.success-span');
        const failurePoints = extractTrendPointsForSingleServiceOperation(operationTrends, 'count.failure-span');
        const countPoints = toCountPoints(successPoints, failurePoints);
        const tp99DurationPoints = extractTrendPointsForSingleServiceOperation(operationTrends, '*_99.duration');
        const latestTp99DurationDatapoint = _.findLast(tp99DurationPoints, point => point.value);

        return {
            operationName,
            totalCount: dataPointsSum(countPoints),
            countPoints,
            avgSuccessPercent: toSuccessPercent(successPoints, failurePoints),
            successPercentPoints: toSuccessPercentPoints(successPoints, failurePoints),
            latestTp99Duration: latestTp99DurationDatapoint && latestTp99DurationDatapoint.value,
            tp99DurationPoints,
            failurePoints
        };
    });
}

function getServicePerfStatsResults(timeWindow, from, until) {
    const SuccessTarget = getServiceTargetStat('~.*', timeWindow, 'count', 'success-span');
    const FailureTarget = getServiceTargetStat('~.*', timeWindow, 'count', 'failure-span');
    const tp99Target = getServiceTargetStat('~.*', timeWindow, '*_99', 'duration');


    return Q.all([
        fetchTrendValues(SuccessTarget, from, until),
        fetchTrendValues(FailureTarget, from, until),
        fetchTrendValues(tp99Target, from, until)
    ]).then(values => extractServicePerfStats({
            successValues: values[0],
            failureValues: values[1],
            tp99Values: values[2]
        })
    );
}

function getServiceSummaryResults(serviceName, timeWindow, from, until) {
    const target = getServiceTargetStat(serviceName, timeWindow, '~(count)|(\\*_99)', '~(success-span)|(failure-span)|(duration)');

    return fetchTrendValues(target, from, until)
        .then(values => extractServiceSummary(values));
}

function getServiceTrendResults(serviceName, timeWindow, from, until) {
    const target = getServiceTargetStat(serviceName, timeWindow, '~(count)|(mean)|(\\*_95)|(\\*_99)', '~(success-span)|(failure-span)|(duration)');

    return fetchTrendValues(target, from, until)
        .then((trends) => {
            const successCount = extractTrendPointsForSingleServiceOperation(trends, 'count.success-span');
            const failureCount = extractTrendPointsForSingleServiceOperation(trends, 'count.failure-span');
            return {
                count: toCountPoints(successCount, failureCount),
                successCount,
                failureCount,
                meanDuration: extractTrendPointsForSingleServiceOperation(trends, 'mean.duration'),
                tp95Duration: extractTrendPointsForSingleServiceOperation(trends, '*_95.duration'),
                tp99Duration: extractTrendPointsForSingleServiceOperation(trends, '*_99.duration')
            };
        });
}

function getOperationSummaryResults(service, timeWindow, from, until) {
    const target = createOperationTarget(service, '~.*', timeWindow, '~(count)|(\\*_99)', '~(received-span)|(success-span)|(failure-span)|(duration)');

    return fetchTrendValues(target, from, until)
        .then(values => extractOperationSummary(values));
}

function getOperationTrendResults(serviceName, operationName, timeWindow, from, until) {
    const target = createOperationTarget(serviceName, operationName, timeWindow, '~(count)|(mean)|(\\*_95)|(\\*_99)', '~(received-span)|(success-span)|(failure-span)|(duration)');

    return fetchTrendValues(target, from, until)
        .then((trends) => {
            const successCount = extractTrendPointsForSingleServiceOperation(trends, 'count.success-span');
            const failureCount = extractTrendPointsForSingleServiceOperation(trends, 'count.failure-span');
            return {
                count: toCountPoints(successCount, failureCount),
                successCount,
                failureCount,
                meanDuration: extractTrendPointsForSingleServiceOperation(trends, 'mean.duration'),
                tp95Duration: extractTrendPointsForSingleServiceOperation(trends, '*_95.duration'),
                tp99Duration: extractTrendPointsForSingleServiceOperation(trends, '*_99.duration')
            };
        });
}

function getEdgeLatencyTrendResults(edges, from, until) {
    const serviceNameRegex = edges.map(e => metricpointNameEncoder.encodeMetricpointName(e.serviceName)).join('|');
    const operationNameRegex = edges.map(e => metricpointNameEncoder.encodeMetricpointName(e.operationName)).join('|');

    const target = createServicesOperationsTarget(`~${serviceNameRegex}`, `~${operationNameRegex}`, 'OneHour', '~(mean)|(\\*_99)', 'latency');

    return fetchTrendValues(target, from, until)
        .then(trends => trends);
}

function getOperationNames(serviceName, from, until) {
    const target = createOperationTarget(serviceName, '~.*', 'OneMinute', '~(count)|(\\*_99)', '~(received-span)|(success-span)|(failure-span)|(duration)');

    return fetchTrendValues(target, from, until)
        .then(values => (_.uniq(values.map(val => (val.operationName))))); // return only unique operation names from Metrictank response
}

// api
connector.getServicePerfStats = (granularity, from, until) =>
    getServicePerfStatsResults(convertGranularityToTimeWindow(granularity), toMilliseconds(from), toMilliseconds(until));

connector.getServiceStats = (serviceName, granularity, from, until) =>
    getServiceSummaryResults(metricpointNameEncoder.encodeMetricpointName(serviceName), convertGranularityToTimeWindow(granularity), toMilliseconds(from), toMilliseconds(until));

connector.getServiceTrends = (serviceName, granularity, from, until) =>
    getServiceTrendResults(metricpointNameEncoder.encodeMetricpointName(serviceName), convertGranularityToTimeWindow(granularity), toMilliseconds(from), toMilliseconds(until));

connector.getOperationStats = (serviceName, granularity, from, until) =>
    getOperationSummaryResults(metricpointNameEncoder.encodeMetricpointName(serviceName), convertGranularityToTimeWindow(granularity), toMilliseconds(from), toMilliseconds(until));

connector.getOperationTrends = (serviceName, operationName, granularity, from, until) =>
    getOperationTrendResults(metricpointNameEncoder.encodeMetricpointName(serviceName), metricpointNameEncoder.encodeMetricpointName(operationName), convertGranularityToTimeWindow(granularity), toMilliseconds(from), toMilliseconds(until));

connector.getEdgeLatency = (edges, from = defaultFrom, until = defaultUntil) => getEdgeLatencyTrendResults(edges, from, until);

connector.getOperationNames = (serviceName, from = defaultFrom, until = defaultUntil) => getOperationNames(metricpointNameEncoder.encodeMetricpointName(serviceName), from, until);

module.exports = connector;
