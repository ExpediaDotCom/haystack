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


import {expect} from 'chai';

const proxyquire = require('proxyquire');

function MockFetcher() {
    return {
        fetch: () => Promise.resolve({
            getValuesList: () => [
                'value-1',
                '$/special/values',
                '#/special/.values',
                'values'
            ]
        })
    };
}


describe('tracesConnector.getServices', () => {
    function createTracesConnectorWithConfig(config) {
        return proxyquire('../../../../../server/connectors/traces/haystack/tracesConnector', {
            '../../operations/grpcFetcher': MockFetcher,
            '../../../config/config': config
        });
    }

    function createTracesConnector(servicesFilter) {
        const config = {connectors: {traces: {servicesFilter}, trends: {connectorName: 'stub'}}};
        return createTracesConnectorWithConfig(config);
    }

    it('should not filter out the services if no servicesFilter is configured', () => {
        const config = {connectors: {traces: {}, trends: {connectorName: 'stub'}}};
        const tracesConnector = createTracesConnectorWithConfig(config);
        return tracesConnector.getServices().then(result => expect(result.length).to.equal(4));
    });

    it('should not filter out the services if an empty servicesFilter is configured', () => {
        const tracesConnector = createTracesConnector([]);
        return tracesConnector.getServices().then(result => expect(result.length).to.equal(4));
    });

    it('should filter out the services that match the regex if a servicesFilter is configured - 1', () => {
        const tracesConnector = createTracesConnector([new RegExp('value')]);
        return tracesConnector.getServices().then(result => expect(result.length).to.equal(0));
    });

    it('should filter out the services that match the regex if a servicesFilter is configured - 2', () => {
        const tracesConnector = createTracesConnector([new RegExp('^\\$/special/.*$')]);
        return tracesConnector.getServices().then(result => expect(result.length).to.equal(3));
    });

    it('should filter out the services that match the regex if a servicesFilter is configured - 3', () => {
        const tracesConnector = createTracesConnector([new RegExp('^#/special/.*$')]);
        return tracesConnector.getServices().then(result => expect(result.length).to.equal(3));
    });

    it('should filter out the services that match the regex if a servicesFilter is configured - 4', () => {
        const tracesConnector = createTracesConnector([new RegExp('^value-1$')]);
        return tracesConnector.getServices().then(result => expect(result.length).to.equal(3));
    });
});
