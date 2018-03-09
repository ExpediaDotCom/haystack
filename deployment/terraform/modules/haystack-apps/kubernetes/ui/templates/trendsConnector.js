/*
 * Copyright 2017 Expedia, Inc.
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

const axios = require('axios');
const Q = require('q');
const config = require('../../../config/config');
const _ = require('lodash');
const logger = require('../../../utils/logger').withIdentifier('support:doppler');

const store = {};
const dtsUrl = config.connectors.trends.dopplerUrl;
const queue = config.connectors.trends.queue;
const operationCountTemplateName = config.connectors.trends.operationCountTemplateName;
const operationDurationTemplateName = config.connectors.trends.operationDurationTemplateName;
const operationSuccessTemplateName = config.connectors.trends.operationSuccessTemplateName;
const serviceCountTemplateName = config.connectors.trends.serviceCountTemplateName;
const serviceDurationTemplateName = config.connectors.trends.serviceDurationTemplateName;
const serviceSuccessTemplateName = config.connectors.trends.serviceSuccessTemplateName;

function IsJsonArray(str) {
    try {
        const obj = JSON.parse(str);
        return Array.isArray(obj);
    } catch (e) {
        return false;
    }
}

function parseDopplerResponse(data, trendType, serviceStats) {
    const parsedData = [];

    if (IsJsonArray(data) && serviceStats) {
        JSON.parse(data).forEach((statsType) => {
            const opKV = {
                type: 'Incoming Requests',
                [trendType]: statsType.values.map(datapoints => ({value: datapoints[0], timestamp: datapoints[1]}))
            };
            parsedData.push(opKV);
        });
    } else if (IsJsonArray(data)) {
        JSON.parse(data).forEach((op) => {
            const opKV = {
                operationName: op.keys.operationName,
                [trendType]: op.values.map(datapoints => ({value: datapoints[0], timestamp: datapoints[1]}))
            };
            parsedData.push(opKV);
        });
    } else {
        logger.info(data);
    }

    return parsedData;
}

function getTrendValues(fetchTrendRequestBody, trendType, serviceStats = false) {
    const deferred = Q.defer();
    const postConfig = {
        transformResponse: [data => parseDopplerResponse(data, trendType, serviceStats)]
    };

    axios
    .post(`${dtsUrl}/value/_search`, fetchTrendRequestBody, postConfig)
    .then(response => deferred.resolve(response.data, fetchTrendRequestBody),
        error => deferred.reject(new Error(error)));

    return deferred.promise;
}

function fetchOperationDataPoints(operationTrends, trendType) {
    const dataPoints = operationTrends.find(trend => trendType in trend);
    return (dataPoints ? dataPoints[trendType] : []);
}

function fetchServiceDataPoints(serviceTrend, trendType) {
    const datapoints = [];
    serviceTrend.forEach((trendvalues) => {
        if (trendvalues[trendType]) {
            (trendvalues[trendType].forEach((trendValue) => {
                    if (trendValue) {
                        datapoints.push(trendValue);
                    }
                }
            ));
        }
    });
    return datapoints;
}

function dataPointsSum(dataPoints) {
    return dataPoints.reduce(((accumulator, dataPoint) => (accumulator + dataPoint.value)), 0);
}

function toSuccessPercent(successPoints, failurePoints) {
    const successCount = successPoints.reduce(((accumulator, dataPoint) => (accumulator + dataPoint.value)), 0);
    const failureCount = failurePoints.reduce(((accumulator, dataPoint) => (accumulator + dataPoint.value)), 0);

    if (successCount + failureCount === 0) {
        return null;
    }

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

function groupByOperation({countValues, successValues, failureValues, tp99Values}) {
    const trendResults = [];
    const groupedByOperationName = _.groupBy(countValues.concat(successValues, failureValues, tp99Values), val => val.operationName);

    Object.keys(groupedByOperationName).forEach((operationName) => {
        const operationTrends = groupedByOperationName[operationName];
        const countPoints = fetchOperationDataPoints(operationTrends, 'count');

        if (countPoints.length !== 0) {
            const successCount = fetchOperationDataPoints(operationTrends, 'success');
            const failureCount = fetchOperationDataPoints(operationTrends, 'failure');
            const tp99DurationPoints = fetchOperationDataPoints(operationTrends, 'tp99');
            const latestTp99DurationDatapoint = tp99DurationPoints[tp99DurationPoints.length - 1];

            const opKV = {
                operationName,
                totalCount: dataPointsSum(countPoints),
                countPoints,
                avgSuccessPercent: toSuccessPercent(successCount, failureCount),
                successPercentPoints: toSuccessPercentPoints(successCount, failureCount),
                latestTp99Duration: latestTp99DurationDatapoint && latestTp99DurationDatapoint.value,
                tp99DurationPoints
            };

            trendResults.push(opKV);
        }
    });

    return trendResults;
}

function getDopplerOperationStats(service, timeWindow, from, until) {
    const countTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: operationCountTemplateName,
            serviceName: service,
            value_name: `${timeWindow}-count`
        },
        from,
        until
    };
    const successTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: operationSuccessTemplateName,
            serviceName: service,
            value_name: `${timeWindow}-count`,
            success: 'true'
        },
        from,
        until
    };
    const failureTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: operationSuccessTemplateName,
            serviceName: service,
            value_name: `${timeWindow}-count`,
            success: 'false'
        },
        from,
        until
    };
    const tp99TemplateKeys = {
        keys: {
            queue,
            metricField: 'duration',
            metricType: 'histogram',
            templateName: operationDurationTemplateName,
            serviceName: service,
            value_name: `${timeWindow}-99percentile`
        },
        from,
        until
    };

    return Q.all([
        getTrendValues(countTemplateKeys, 'count'),
        getTrendValues(successTemplateKeys, 'success'),
        getTrendValues(failureTemplateKeys, 'failure'),
        getTrendValues(tp99TemplateKeys, 'tp99')
    ])
    .then(values => groupByOperation({
            countValues: values[0],
            successValues: values[1],
            failureValues: values[2],
            tp99Values: values[3]
        })
    );
}

function convertGranularityToTimeWindow(timespan) {
    switch (timespan) {
        case '60000': return '1min';
        case '300000': return '5min';
        case '900000': return '15min';
        case '3600000': return '1hour';
        default: return '1min';
    }
}

store.getOperationStats = (serviceName, granularity, from, until) => {
    const deffered = Q.defer();
    deffered.resolve(getDopplerOperationStats(serviceName, convertGranularityToTimeWindow(granularity), from, until),
        error => deffered.reject(new Error(error)));

    return deffered.promise;
};

// fetching trends for an individual operation

function fetchTrendValues(trendRequest) {
    const deferred = Q.defer();

    axios
    .post(`${dtsUrl}/value/_search`, trendRequest)
    .then(response => deferred.resolve(response.data[0]
        ? response.data[0].values.map(datapoints => ({value: datapoints[0], timestamp: datapoints[1]}))
        : []));

    return deferred.promise;
}

function getDopplerOperationTrendResults(serviceName, operationName, timeWindow, from, until) {
    const countTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: operationCountTemplateName,
            serviceName,
            value_name: `${timeWindow}-count`,
            operationName
        },
        from,
        until
    };
    const durationTemplateKeys = {
        keys: {
            queue,
            metricField: 'duration',
            metricType: 'histogram',
            templateName: operationDurationTemplateName,
            serviceName,
            value_name: `${timeWindow}-mean`,
            operationName
        },
        from,
        until
    };
    const successTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: operationSuccessTemplateName,
            serviceName,
            value_name: `${timeWindow}-count`,
            success: 'true',
            operationName
        },
        from,
        until
    };
    const failureTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: operationSuccessTemplateName,
            serviceName,
            value_name: `${timeWindow}-count`,
            success: 'false',
            operationName
        },
        from,
        until
    };
    const tp95TemplateKeys = {
        keys: {
            queue,
            metricField: 'duration',
            metricType: 'histogram',
            templateName: operationDurationTemplateName,
            serviceName,
            value_name: `${timeWindow}-95percentile`,
            operationName
        },
        from,
        until
    };
    const tp99TemplateKeys = {
        keys: {
            queue,
            metricField: 'duration',
            metricType: 'histogram',
            templateName: operationDurationTemplateName,
            serviceName,
            value_name: `${timeWindow}-99percentile`,
            operationName
        },
        from,
        until
    };

    return Q.all([
        fetchTrendValues(countTemplateKeys),
        fetchTrendValues(successTemplateKeys),
        fetchTrendValues(failureTemplateKeys),
        fetchTrendValues(durationTemplateKeys),
        fetchTrendValues(tp95TemplateKeys),
        fetchTrendValues(tp99TemplateKeys)
    ])
    .then(results => ({
            count: results[0],
            successCount: results[1],
            failureCount: results[2],
            meanDuration: results[3],
            tp95Duration: results[4],
            tp99Duration: results[5]
        })
    );
}

store.getOperationTrends = (serviceName, operationName, granularity, from, until) => {
    const deffered = Q.defer();

    getDopplerOperationTrendResults(serviceName, operationName, convertGranularityToTimeWindow(granularity), from, until)
    .then(results => deffered.resolve(results));

    return deffered.promise;
};

// fetching stats for Service

function serviceStatsFormatter({countValues, successValues, failureValues, tp99Values}) {
    const trendResults = [];
    const countPoints = countValues[0] && countValues[0].count;
    const type = countValues[0] && countValues[0].type;

    if (countPoints && countPoints.length !== 0) {
        const successCount = successValues[0] && successValues[0].success;
        const failureCount = failureValues[0] && failureValues[0].failure;
        const tp99DurationPoints = tp99Values[0].tp99;
        const latestTp99DurationDatapoint = tp99DurationPoints[tp99DurationPoints.length - 1];

        trendResults.push({
            type,
            totalCount: dataPointsSum(countPoints),
            countPoints,
            avgSuccessPercent: successCount && failureCount ? toSuccessPercent(successCount, failureCount) : null,
            successPercentPoints: successCount && failureCount ? toSuccessPercentPoints(successCount, failureCount) : null,
            latestTp99Duration: latestTp99DurationDatapoint && latestTp99DurationDatapoint.value,
            tp99DurationPoints
        });
    }
    return trendResults;
}

function getDopplerServiceStats(service, timeWindow, from, until) {
    const countTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: serviceCountTemplateName,
            serviceName: service,
            value_name: `${timeWindow}-count`,
            'service-root': 'true'
        },
        from,
        until
    };
    const successTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: serviceSuccessTemplateName,
            serviceName: service,
            value_name: `${timeWindow}-count`,
            error: 'false',
            'service-root': 'true'
        },
        from,
        until
    };
    const failureTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: serviceSuccessTemplateName,
            serviceName: service,
            value_name: `${timeWindow}-count`,
            error: 'true',
            'service-root': 'true'
        },
        from,
        until
    };
    const tp99TemplateKeys = {
        keys: {
            queue,
            metricField: 'duration',
            metricType: 'histogram',
            templateName: serviceDurationTemplateName,
            serviceName: service,
            value_name: `${timeWindow}-99percentile`,
            'service-root': 'true'
        },
        from,
        until
    };

    return Q.all([
        getTrendValues(countTemplateKeys, 'count', true),
        getTrendValues(successTemplateKeys, 'success', true),
        getTrendValues(failureTemplateKeys, 'failure', true),
        getTrendValues(tp99TemplateKeys, 'tp99', true)
    ])
    .then(values => serviceStatsFormatter({
            countValues: values[0],
            successValues: values[1],
            failureValues: values[2],
            tp99Values: values[3]
        })
    );
}

store.getServiceStats = (serviceName, granularity, from, until) => {
    const deffered = Q.defer();
    deffered.resolve(getDopplerServiceStats(serviceName, convertGranularityToTimeWindow(granularity), from, until),
        error => deffered.reject(new Error(error)));

    return deffered.promise;
};


function getDopplerServiceTrendResults(serviceName, timeWindow, from, until) {
    const countTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: serviceCountTemplateName,
            serviceName,
            value_name: `${timeWindow}-count`,
            'service-root': 'true'
        },
        from,
        until
    };
    const durationTemplateKeys = {
        keys: {
            queue,
            metricField: 'duration',
            metricType: 'histogram',
            templateName: serviceDurationTemplateName,
            serviceName,
            value_name: `${timeWindow}-mean`,
            'service-root': 'true'
        },
        from,
        until
    };
    const successTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: serviceSuccessTemplateName,
            serviceName,
            value_name: `${timeWindow}-count`,
            error: 'false',
            'service-root': 'true'
        },
        from,
        until
    };
    const failureTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: serviceSuccessTemplateName,
            serviceName,
            value_name: `${timeWindow}-count`,
            error: 'true',
            'service-root': 'true'
        },
        from,
        until
    };
    const tp95TemplateKeys = {
        keys: {
            queue,
            metricField: 'duration',
            metricType: 'histogram',
            templateName: serviceDurationTemplateName,
            serviceName,
            value_name: `${timeWindow}-95percentile`,
            'service-root': 'true'
        },
        from,
        until
    };
    const tp99TemplateKeys = {
        keys: {
            queue,
            metricField: 'duration',
            metricType: 'histogram',
            templateName: serviceDurationTemplateName,
            serviceName,
            value_name: `${timeWindow}-99percentile`,
            'service-root': 'true'
        },
        from,
        until
    };

    return Q.all([
        fetchTrendValues(countTemplateKeys),
        fetchTrendValues(successTemplateKeys),
        fetchTrendValues(failureTemplateKeys),
        fetchTrendValues(durationTemplateKeys),
        fetchTrendValues(tp95TemplateKeys),
        fetchTrendValues(tp99TemplateKeys)
    ])
    .then(results => ({
            count: results[0],
            successCount: results[1],
            failureCount: results[2],
            meanDuration: results[3],
            tp95Duration: results[4],
            tp99Duration: results[5]
        })
    );
}

store.getServiceTrends = (serviceName, granularity, from, until) => {
    const deffered = Q.defer();

    getDopplerServiceTrendResults(serviceName, convertGranularityToTimeWindow(granularity), from, until)
    .then(results => deffered.resolve(results));

    return deffered.promise;
};

// fetching stats for Service Performance Component

function parseServicePerfTrends(data, trendType) {
    const parsedData = [];

    if (IsJsonArray(data)) {
        JSON.parse(data).forEach((op) => {
            const opKV = {
                serviceName: op.keys.serviceName,
                operationName: op.keys.operationName,
                [trendType]: op.values.map(datapoints => ({value: datapoints[0]}))
            };
            parsedData.push(opKV);
        });
    } else {
        logger.info(data);
    }
    return parsedData;
}

function getServicePerfTrends(fetchTrendRequestBody, trendType) {
    const deferred = Q.defer();
    const postConfig = {
        transformResponse: [data => parseServicePerfTrends(data, trendType)]
    };

    axios
    .post(`${dtsUrl}/value/_search`, fetchTrendRequestBody, postConfig)
    .then(response => deferred.resolve(response.data, fetchTrendRequestBody),
        error => deferred.reject(new Error(error)));

    return deferred.promise;
}

function groupByService({countValues, successValues, failureValues}) {
    const serviceResults = [];
    const groupedByServiceName = _.groupBy(countValues.concat(successValues, failureValues), val => val.serviceName);

    Object.keys(groupedByServiceName).forEach((service) => {
        const serviceTrends = groupedByServiceName[service];
        const count = dataPointsSum(fetchServiceDataPoints(serviceTrends, 'count'));
        const successCount = dataPointsSum(fetchServiceDataPoints(serviceTrends, 'success'));
        const failureCount = dataPointsSum(fetchServiceDataPoints(serviceTrends, 'failure'));
        const successPercent = ((successCount / (successCount + failureCount)) * 100);

        const opKV = {
            serviceName: service,
            successPercent,
            failureCount,
            successCount,
            totalCount: count
        };

        serviceResults.push(opKV);
    });
    return serviceResults;
}


function getDopplerServicePerformanceStats(timeWindow, from, until) {
    const countTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: operationCountTemplateName,
            value_name: `${timeWindow}-count`
        },
        from,
        until
    };
    const successTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: operationSuccessTemplateName,
            value_name: `${timeWindow}-count`,
            success: 'true'
        },
        from,
        until
    };
    const failureTemplateKeys = {
        keys: {
            queue,
            metricType: 'count',
            templateName: operationSuccessTemplateName,
            value_name: `${timeWindow}-count`,
            success: 'false'
        },
        from,
        until
    };

    return Q.all([
        getServicePerfTrends(countTemplateKeys, 'count'),
        getServicePerfTrends(successTemplateKeys, 'success'),
        getServicePerfTrends(failureTemplateKeys, 'failure')
    ])
    .then(values => groupByService({
            countValues: values[0],
            successValues: values[1],
            failureValues: values[2]
        })
    );
}

store.getServicePerfStats = (granularity, from, until) => {
    const deffered = Q.defer();

    deffered.resolve(getDopplerServicePerformanceStats(convertGranularityToTimeWindow(granularity), from, until),
        error => deffered.reject(new Error(error)));

    return deffered.promise;
};

module.exports = store;
