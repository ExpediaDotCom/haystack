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


import React from 'react';
import { expect } from 'chai';
import axios from 'axios';
import { when } from 'mobx';
import MockAdapter from 'axios-mock-adapter';
import { mount } from 'enzyme';
import sinon from 'sinon';

import {SpansSearchStore} from '../../../../src/components/traces/stores/spansSearchStore';
import SpansView from '../../../../src/components/traces/results/spansView';


const fulfilledPromise = {
    case: ({fulfilled}) => fulfilled()
};

const rejectedPromise = {
    case: ({rejected}) => rejected()
};

const pendingPromise = {
    case: ({pending}) => pending()
};

const stubTrace = [
    {
        traceId: 'x00245a5-g0c4-4c37-55a7-da83def7127a',
        spanId: 'a40165e5-e0c4-4c51-11x7-bb79def7597a',
        serviceName: 'test-service',
        operationName: 'test',
        startTime: 1504784384000,
        duration: 3500000,
        logs: [
            {
                timestamp: 1504784384000,
                fields: [{
                    key: 'event',
                    value: 'sr'
                },
                    {
                        key: 'event',
                        value: 'cs'
                    }
                ]
            }
        ],
        tags: [
            {
                key: 'success',
                value: 'true'
            }
        ]
    },
    {
        traceId: 'x00245a5-g0c4-4c37-55a7-da83def7127a',
        spanId: 'a80921e5-e0c4-4c37-91a7-da79def7597a',
        parentSpanId: 'a40165e5-e0c4-4c51-11x7-bb79def7597a',
        serviceName: 'test-service',
        operationName: 'test',
        startTime: 1504785384000,
        duration: 2000000,
        logs: [
            {
                timestamp: 1504784384000,
                fields: [{
                    key: 'event',
                    value: 'sr'
                },
                    {
                        key: 'event',
                        value: 'cs'
                    }
                ]
            }
        ],
        tags: [
            {
                key: 'success',
                value: 'false'
            }
        ]
    },
    {
        traceId: 'x00245a5-g0c4-4c37-55a7-da83def7127a',
        spanId: 'a55965e5-e0c4-4a37-91a7-da79def7597a',
        parentSpanId: 'a40165e5-e0c4-4c51-11x7-bb79def7597a',
        serviceName: 'test-service',
        operationName: 'test',
        startTime: 1504785384000,
        duration: 2000000,
        logs: [
            {
                timestamp: 1504784384000,
                fields: [{
                    key: 'event',
                    value: 'sr'
                },
                    {
                        key: 'event',
                        value: 'cs'
                    }
                ]
            }
        ],
        tags: [
            {
                key: 'success',
                value: 'false'
            }
        ]
    },
    {
        traceId: 'x00245a5-g0c4-4c37-55a7-da83def7127a',
        spanId: 'wb651a1b-146x-4c37-91a7-6r61v513r1v11',
        parentSpanId: 'a40165e5-e0c4-4c51-11x7-bb79def7597a',
        serviceName: 'test-service',
        operationName: 'test',
        startTime: 1504785384000,
        duration: 2000000,
        logs: [
            {
                timestamp: 1504784384000,
                fields: [{
                    key: 'event',
                    value: 'sr'
                },
                    {
                        key: 'event',
                        value: 'cs'
                    }
                ]
            }
        ],
        tags: [
        ]
    }
];

function createStubStore(results, promise) {
    const store = new SpansSearchStore();
    sinon.stub(store, 'fetchSpans', () => {
        store.results = results;
        store.promiseState = promise;
    });

    return store;
}

describe('SpansSearchStore', () => {
    let server = null;
    const store = new SpansSearchStore();

    beforeEach(() => {
        server = new MockAdapter(axios);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches individual spans from api with traceIds passed in', (done) => {
        server.onGet('/api/traces/raw?traceIds=%5B%2212345%22%5D').reply(200, stubTrace);
        store.fetchSpans(['12345']);

        when(
            () => store.results.length > 0,
            () => {
                expect(store.results).to.have.length(4);
                done();
            });
    });
});

describe('SpansView', () => {
    it('should call create a table with rows for each span in the listed traces', () => {
        const store = createStubStore(stubTrace, fulfilledPromise);
        const wrapper = mount(<SpansView traceIds={['x00245a5-g0c4-4c37-55a7-da83def7127a']} store={store} />);

        expect(store.fetchSpans.callCount).to.equal(1);
        expect(wrapper.find('TableRow').length).to.equal(4);
    });

    it('should show error when promise is rejected', () => {
        const store = createStubStore(stubTrace, rejectedPromise);
        const wrapper = mount(<SpansView traceIds={['x00245a5-g0c4-4c37-55a7-da83def7127a']} store={store} />);

        expect(wrapper.find('Error').length).to.equal(1);
    });

    it('should show loading when promise is pending', () => {
        const store = createStubStore(stubTrace, pendingPromise);
        const wrapper = mount(<SpansView traceIds={['x00245a5-g0c4-4c37-55a7-da83def7127a']} store={store} />);

        expect(wrapper.find('.loading').length).to.equal(1);
    });

    it('clicking on a row should expand the tags for a trace with tags', () => {
        const store = createStubStore(stubTrace, fulfilledPromise);
        const wrapper = mount(<SpansView traceIds={['x00245a5-g0c4-4c37-55a7-da83def7127a']} store={store} />);
        const firstRow = wrapper.find('TableRow').first();
        firstRow.simulate('click');

        expect(firstRow.instance().props.isSelected).to.equal(true);
    });
});
