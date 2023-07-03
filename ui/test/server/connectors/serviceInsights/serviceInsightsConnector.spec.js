/*
 * Copyright 2019 Expedia Group
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

const connector = proxyquire('../../../../server/connectors/serviceInsights/serviceInsightsConnector', {
    './fetcher': (serviceName) => ({
        fetch: () =>
            new Promise((resolve) =>
                resolve({
                    serviceName,
                    spans: [
                        {
                            traceId: 100,
                            spanId: 1,
                            serviceName,
                            operationName: 'operation',
                            tags: []
                        },
                        {
                            traceId: 100,
                            spanId: 2,
                            parentSpanId: 1,
                            serviceName: 'mock-downstream-service',
                            operationName: 'operation',
                            tags: []
                        }
                    ]
                })
            )
    })
});

describe('serviceInsightsConnector.getServiceInsightsForService', () => {
    it('should initialize promise and return data', (done) => {
        // given
        const options = {
            serviceName: 'mock-service',
            startTime: 1000,
            endTime: 2000
        };

        // when
        connector
            .getServiceInsightsForService(options)
            .then((result) => {
                // then
                expect(result).to.have.nested.property('summary.tracesConsidered', 1);
                expect(result)
                    .to.have.property('nodes')
                    .with.lengthOf(2);
                done();
            })
            .done();
    });

    it('should support relationships to filter', (done) => {
        // given
        const options = {
            serviceName: 'mock-service',
            startTime: 1000,
            endTime: 2000,
            relationship: 'upstream'
        };

        // when
        connector
            .getServiceInsightsForService(options)
            .then((result) => {
                // then
                expect(result)
                    .to.have.property('nodes')
                    .with.lengthOf(1); // downstream filtered out
                done();
            })
            .done();
    });
});
