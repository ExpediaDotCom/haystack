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

import {expect} from 'chai';
import proxyquire from 'proxyquire';
import {spy} from 'sinon';
import {observe} from 'mobx';

const mockAxios = {
    get: (url) => {
        let mockError = false;
        if (/node-web-ui-errors/.test(url)) {
            mockError = true;
        }
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                if (mockError) {
                    // eslint-disable-next-line prefer-promise-reject-errors
                    return reject({});
                }
                return resolve({
                    data: {
                        summary: {
                            violations: 1
                        },
                        nodes: [],
                        links: []
                    }
                });
            }, 50);
        });
    }
};

const mockAxiosSpy = spy(mockAxios, 'get');

const {ServiceInsightsStore} = proxyquire('../../../../../src/components/serviceInsights/stores/serviceInsightsStore', {
    axios: mockAxios
});

describe('serviceInsightsStore', () => {
    let store = null;
    const handleErrorSpy = spy(ServiceInsightsStore, 'handleError');

    before(() => {
        store = new ServiceInsightsStore();
        handleErrorSpy.reset();
    });

    it('should fetch a list of service insights', (done) => {
        // given, when
        store.fetchServiceInsights({
            serviceName: 'node-web-ui',
            startTime: 1000,
            endTime: 2000
        });
        observe(store.promiseState, () => {
            store.promiseState.case({
                fulfilled: (result) => {
                    // then
                    expect(mockAxiosSpy.calledWith('/api/serviceInsights?serviceName=node-web-ui&startTime=1000&endTime=2000')).to.equal(true);
                    expect(result.data.summary.violations).to.equal(1);
                    done();
                },
                rejected: () => {
                    expect.fail('mobx action failed.');
                    done();
                }
            });
        });
    });

    it('should support optional parameters', (done) => {
        // given, when
        store.fetchServiceInsights({
            serviceName: 'node-web-ui',
            startTime: 1000,
            endTime: 2000,
            relationship: 'all',
            operationName: 'some-operation',
            traceId: 'some-trace'
        });
        observe(store.promiseState, () => {
            store.promiseState.case({
                fulfilled: (result) => {
                    // then
                    expect(
                        mockAxiosSpy.calledWith(
                            '/api/serviceInsights?serviceName=node-web-ui&operationName=some-operation&traceId=some-trace&startTime=1000&endTime=2000&relationship=all'
                        )
                    ).to.equal(true);
                    expect(result.data.summary.violations).to.equal(1);
                    done();
                },
                rejected: () => {
                    expect.fail('mobx action failed.');
                    done();
                }
            });
        });
    });

    it('should handle errors', (done) => {
        // given, when
        store.fetchServiceInsights({
            serviceName: 'node-web-ui-errors',
            startTime: 1000,
            endTime: 2000
        });
        observe(store.promiseState, () => {
            store.promiseState.case({
                fulfilled: (result) => {
                    expect.fail(`should have not succeeded with result: ${result}`);
                },
                rejected: (result) => {
                    // then
                    expect(handleErrorSpy.calledWith({})).to.equal(true);
                    expect(JSON.stringify(result)).to.equal('{}');
                    done();
                }
            });
        });
    });
});
