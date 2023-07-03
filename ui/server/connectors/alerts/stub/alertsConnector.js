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

function getRandomTimeStamp() {
    const currentTime = ((new Date()).getTime());
    return (currentTime - Math.floor((Math.random() * 5000 * 60)));
}

function generateAnomaly() {
    const currentTime = ((new Date()).getTime() / 1000);
    const timestamp = (currentTime - Math.floor((Math.random() * 2000 * 60)));
    const expectedvalue = Math.floor(Math.random() * 100000);
    const observedvalue = Math.floor(expectedvalue * (Math.random() * 100));
    return {
        observedvalue,
        expectedvalue,
        timestamp,
        strength: observedvalue % 2 ? 'STRONG' : 'WEAK'
    };
}

function getAlerts() {
    return [
        {
            operationName: 'tarley-1',
            type: 'duration',
            isUnhealthy: true,
            timestamp: getRandomTimeStamp()
        },
        {
            operationName: 'tarley-1',
            type: 'failure-span',
            isUnhealthy: true,
            timestamp: getRandomTimeStamp()
        },
        {
            operationName: 'tully-1',
            type: 'duration',
            isUnhealthy: false,
            timestamp: getRandomTimeStamp()
        },
        {
            operationName: 'tully-1',
            type: 'failure-span',
            isUnhealthy: false,
            timestamp: getRandomTimeStamp()
        },
        {
            operationName: 'tully-1',
            type: 'duration',
            isUnhealthy: false,
            timestamp: getRandomTimeStamp()
        },        {
            operationName: 'tully-1',
            type: 'failure-span',
            isUnhealthy: false,
            timestamp: getRandomTimeStamp()
        },
        {
            operationName: 'dondarrion-1',
            type: 'duration',
            isUnhealthy: false,
            timestamp: getRandomTimeStamp()
        },
        {
            operationName: 'dondarrion-1',
            type: 'failure-span',
            isUnhealthy: false,
            timestamp: getRandomTimeStamp()
        }
    ];
}

const anomalies = [
    generateAnomaly(),
    generateAnomaly(),
    generateAnomaly(),
    generateAnomaly(),
    generateAnomaly(),
    generateAnomaly(),
    generateAnomaly(),
    generateAnomaly(),
    generateAnomaly(),
    generateAnomaly()
];

const connector = {};

connector.getServiceAlerts = (service, query) => Q.fcall(() => getAlerts(query));

connector.getAnomalies = () => Q.fcall(() => anomalies);

connector.getServiceUnhealthyAlertCount = () => Q.fcall(() => Math.floor(Math.random() * 3));

module.exports = connector;
