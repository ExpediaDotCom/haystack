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
const _ = require('lodash');
const grpc = require('grpc');

const config = require('../../../config/config');
const servicesConnector = config.connectors.traces && require('../../services/servicesConnector'); // eslint-disable-line
const trendsConnector = require('../../trends/haystack/trendsConnector');

const fetcher = require('../../operations/grpcFetcher');
const services = require('../../../../static_codegen/anomaly/anomalyReader_grpc_pb');
const messages = require('../../../../static_codegen/anomaly/anomalyReader_pb');
const MetricpointNameEncoder = require('../../utils/encoders/MetricpointNameEncoder');

const metricpointNameEncoder = new MetricpointNameEncoder(config.encoder);

const grpcOptions = config.grpcOptions || {};

const connector = {};
const client = new services.AnomalyReaderClient(
    `${config.connectors.alerts.haystackHost}:${config.connectors.alerts.haystackPort}`,
    grpc.credentials.createInsecure(),
    grpcOptions); // TODO make client secure
const alertTypes = ['duration', 'failure-span'];
const getAnomaliesFetcher = fetcher('getAnomalies', client);
const alertFreqInSec = config.connectors.alerts.alertFreqInSec || 300; // TODO make this based on alert type


function fetchOperations(serviceName) {
    if (servicesConnector) {
        servicesConnector.getOperations(serviceName);
    }
    return trendsConnector.getOperationNames(serviceName);
}

function sameOperationAndType(alertToCheck, operationName, type) {
    if (!alertToCheck) {
        return false;
    }
    const operationToCheck = alertToCheck.labelsMap.find(label => label[0] === 'operationName');
    const typeToCheck = alertToCheck.labelsMap.find(label => label[0] === 'metric_key');
    return ((operationToCheck && operationToCheck[1] === operationName) && typeToCheck && typeToCheck[1] === type);
}

function parseOperationAlertsResponse(data) {
    const fullAnomalyList = data.searchanomalyresponseList;
    const mappedAndMergedResponse = fullAnomalyList.map((anomalyResponse, baseIterationIndex) => {
        if (anomalyResponse === null) return null;
        const operationLabel = anomalyResponse.labelsMap.find(label => label[0] === 'operationName');
        if (operationLabel) {
            const operationName = operationLabel[1];
            const type = anomalyResponse.labelsMap.find(label => label[0] === 'metric_key')[1];
            let anomaliesList = anomalyResponse.anomaliesList;

            fullAnomalyList.slice(baseIterationIndex + 1, fullAnomalyList.length).forEach((alertToCheck, checkIndex) => {
                if (sameOperationAndType(alertToCheck, operationName, type)) {
                    anomaliesList = _.merge(anomaliesList, alertToCheck.anomaliesList);
                    fullAnomalyList[baseIterationIndex + checkIndex + 1] = null;
                }
            });

            const latestUnhealthy = _.maxBy(anomaliesList, anomaly => anomaly.timestamp);
            const timestamp = latestUnhealthy && latestUnhealthy.timestamp * 1000;
            const isUnhealthy =  (timestamp && timestamp >= (Date.now() - (alertFreqInSec * 1000)));

            return {
                operationName,
                type,
                isUnhealthy,
                timestamp
            };
        }

        return null;
    });

    return _.filter(mappedAndMergedResponse, a => a !== null);
}

function fetchAlerts(serviceName, interval, from, stat, key) {
    const request = new messages.SearchAnamoliesRequest();
    request.getLabelsMap()
        .set('serviceName', metricpointNameEncoder.encodeMetricpointName(decodeURIComponent(serviceName)))
        .set('interval', interval)
        .set('mtype', 'gauge')
        .set('product', 'haystack')
        .set('stat', stat)
        .set('metric_key', key);
    request.setStarttime(Math.trunc(from / 1000));
    request.setEndtime(Math.trunc(Date.now() / 1000));
    request.setSize(-1);

    return getAnomaliesFetcher
        .fetch(request)
        .then(pbResult => parseOperationAlertsResponse(messages.SearchAnomaliesResponse.toObject(false, pbResult)));
}

function fetchOperationAlerts(serviceName, interval, from) {
    return Q.all([fetchAlerts(serviceName, interval, from, '*_99', 'duration'), fetchAlerts(serviceName, interval, from, 'count', 'failure-span')])
        .then(stats => (_.merge(stats[0], stats[1])));
}

function mergeOperationsWithAlerts({operationAlerts, operations}) {
    if (operations && operations.length) {
        return _.flatten(operations.map(operation => alertTypes.map((alertType) => {
            const operationAlert = operationAlerts.find(alert => (alert.operationName.toLowerCase() === operation.toLowerCase() && alert.type === alertType));

            if (operationAlert !== undefined) {
                return {
                    ...operationAlert
                };
            }
            return {
                operationName: operation,
                type: alertType,
                isUnhealthy: false,
                timestamp: null
            };
        })));
    }

    return _.flatten(alertTypes.map(alertType => (_.filter(operationAlerts, alert => (alert.type === alertType)))));
}

function returnAnomalies(data) {
    if (!data || !data.length || !data[0].anomaliesList.length) {
        return [];
    }

    return _.flatten(data.map((anomaly) => {
        const strength = anomaly.labelsMap.find(label => label[0] === 'anomalyLevel')[1];
        return anomaly.anomaliesList.map(a => ({strength, ...a}));
    }));
}

function getActiveAlertCount(operationAlerts) {
    return operationAlerts.filter(opAlert => opAlert.isUnhealthy).length;
}

connector.getServiceAlerts = (serviceName, interval) => {
    // todo: calculate "from" value based on selected interval
    const oneDayAgo = Math.trunc((Date.now() - (24 * 60 * 60 * 1000)));
    return Q.all([fetchOperations(decodeURIComponent(serviceName)), fetchOperationAlerts(serviceName, interval, oneDayAgo)])
        .then(stats => mergeOperationsWithAlerts({
                operations: stats[0],
                operationAlerts: stats[1]
            })
        );
};

connector.getAnomalies = (serviceName, operationName, alertType, from, interval) => {
    const stat = alertType === 'failure-span' ? 'count' : '*_99';

    const request = new messages.SearchAnamoliesRequest();
    request.getLabelsMap()
        .set('serviceName', metricpointNameEncoder.encodeMetricpointName(decodeURIComponent(serviceName)))
        .set('operationName', metricpointNameEncoder.encodeMetricpointName(decodeURIComponent(operationName)))
        .set('product', 'haystack')
        .set('metric_key', alertType)
        .set('stat', stat)
        .set('interval', interval)
        .set('mtype', 'gauge');
    request.setStarttime(Math.trunc(from / 1000));
    request.setEndtime(Math.trunc(Date.now() / 1000));
    request.setSize(-1);

    return getAnomaliesFetcher
        .fetch(request)
        .then(pbResult => returnAnomalies(messages.SearchAnomaliesResponse.toObject(false, pbResult).searchanomalyresponseList));
};

connector.getServiceUnhealthyAlertCount = (serviceName, interval) =>
    fetchOperationAlerts(serviceName, interval, Math.trunc((Date.now() - (5 * 60 * 1000))))
        .then(result => getActiveAlertCount(result));

module.exports = connector;
