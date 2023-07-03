/*
 * Copyright 2018 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the License);
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an AS IS BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
/* eslint-disable max-len */

import {expect} from 'chai';

const axios = require('axios');
const MockAdapter = require('axios-mock-adapter');
const trendsConnector = require('../../../../../server/connectors/trends/haystack/trendsConnector');

const from = '1530828169';
const to = '1530829069';

describe('trendsConnector', () => {
    let server;


    before(() => {
        server = new MockAdapter(axios);
        server.onGet(`undefined/render?target=seriesByTag('name%3Dsuccess-span'%2C'serviceName%3D~.*'%2C'interval%3DOneMinute'%2C'stat%3Dcount')&from=${from}&to=${to}`).reply(200, []);
        server.onGet(`undefined/render?target=seriesByTag('name%3Dfailure-span'%2C'serviceName%3D~.*'%2C'interval%3DOneMinute'%2C'stat%3Dcount')&from=${from}&to=${to}`).reply(200, []);
        server.onGet(`undefined/render?target=seriesByTag('name%3Dduration'%2C'serviceName%3D~.*'%2C'interval%3DOneMinute'%2C'stat%3D*_99')&from=${from}&to=${to}`).reply(200, []);
        server.onGet(`undefined/render?target=seriesByTag('name%3D~(received-span)%7C(success-span)%7C(failure-span)%7C(duration)'%2C'serviceName%3Dtest-service'%2C'operationName%3D~.*'%2C'interval%3DOneMinute'%2C'stat%3D~(count)%7C(%5C*_99)')&from=${from}&to=${to}`).reply(200, []);
        server.onGet(`undefined/render?target=seriesByTag('name%3Dlatency'%2C'serviceName%3D~'%2C'operationName%3D~'%2C'interval%3DOneHour'%2C'stat%3D~(mean)%7C(%5C*_99)')&from=${from}&to=${to}`).reply(200, []);
        server.onGet(`undefined/render?target=seriesByTag('name%3D~(received-span)%7C(success-span)%7C(failure-span)%7C(duration)'%2C'serviceName%3Dtest-service'%2C'operationName%3Dtest-operation'%2C'interval%3DOneMinute'%2C'stat%3D~(count)%7C(mean)%7C(%5C*_95)%7C(%5C*_99)')&from=${from}&to=${to}`).reply(200, []);
        server.onGet(`undefined/render?target=seriesByTag('name%3D~(success-span)%7C(failure-span)%7C(duration)'%2C'serviceName%3Dtest-service'%2C'interval%3DOneMinute'%2C'stat%3D~(count)%7C(mean)%7C(%5C*_95)%7C(%5C*_99)')&from=${from}&to=${to}`).reply(200, []);
        server.onGet(`undefined/render?target=seriesByTag('name%3D~(success-span)%7C(failure-span)%7C(duration)'%2C'serviceName%3Dtest-service'%2C'interval%3DOneMinute'%2C'stat%3D~(count)%7C(%5C*_99)')&from=${from}&to=${to}`).reply(200, []);
    });

    after(() => {
        server = null;
    });

    it('encodes and decodes correctly', () => trendsConnector.getServicePerfStats(3000, from * 1000, to * 1000).then(result => expect(result).to.be.empty));

    it('collects operation names', () => trendsConnector.getOperationNames('test-service', from, to).then(result => expect(result).to.be.empty));

    it('gets edge latency', () => trendsConnector.getEdgeLatency([], from, to).then(result => expect(result).to.be.empty));

    it('gets operation trends', () => trendsConnector.getOperationTrends('test-service', 'test-operation', 60000, from * 1000, to * 1000).then(result => expect(Object.keys(result)).to.have.length(6)));

    it('gets operation stats', () => trendsConnector.getOperationStats('test-service', 60000, from * 1000, to * 1000).then(result => expect(result).to.have.length(0)));

    it('gets service trends', () => trendsConnector.getServiceTrends('test-service', 60000, from * 1000, to * 1000).then(result => expect(Object.keys(result)).to.have.length(6)));

    it('gets service stats', () => trendsConnector.getServiceStats('test-service', 60000, from * 1000, to * 1000).then(result => expect(result).to.have.length(1)));
});
