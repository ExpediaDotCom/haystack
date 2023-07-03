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

import { expect } from 'chai';
import axios from 'axios';
import { when } from 'mobx';
import MockAdapter from 'axios-mock-adapter';

import { TracesSearchStore } from '../../../src/components/traces/stores/tracesSearchStore';
import { TraceDetailsStore } from '../../../src/components/traces/stores/traceDetailsStore';

const stubTrace = [{
    traceId: 'test-stub',
    spanCount: 34,
    services: [
        {
            name: 'test-stub',
            spanCount: 16
        },
        {
            name: 'test-stub',
            spanCount: 18
        }
    ],
    root: {
        url: 'test-stub',
        serviceName: 'test-stub',
        operationName: 'test-stub',
        duration: 3404000,
        error: false
    },
    queriedService: {
        duration: 23000,
        durationPercent: 99,
        error: false
    },
    queriedOperation: {
        duration: 1,
        durationPercent: 0,
        error: false
    },
    startTime: 1510686424051000,
    duration: 240000,
    error: false
}];

const stubTraceDetails = [{
    traceId: 'traceid',
    spanId: 'root-spanid',
    serviceName: 'stark-service',
    operationName: 'snow-1',
    startTime: 1504784384000,
    duration: 3525000,
    logs: [],
    tags: [{
        key: 'url',
        value: 'http://trace.io/blah'
    }, {
        key: 'url2',
        value: 'some:data'
    }]
}, {
    traceId: 'traceid',
    parentSpanId: 'root-spanid',
    spanId: 'westeros-1',
    serviceName: 'westeros-service',
    operationName: 'mormont-1',
    startTime: 1504784634000,
    duration: 1505000,
    logs: [],
    tags: []
}, {
    traceId: 'traceid',
    parentSpanId: 'westeros-1',
    spanId: 'tyrell-1',
    serviceName: 'tyrell-service',
    operationName: 'tully-1',
    startTime: 1504784754000,
    duration: 605000,
    logs: [],
    tags: []
}];

const stubRawTraceData = [{
    traceId: '380965e5-e0c4-4c37-91a7-da79def7597b',
    spanCount: 12,
    services: [{
        name: 'stark-service',
        spanCount: 1
    }, {
        name: 'tyrell-service',
        spanCount: 29
    }],
    root: {
        url: '/stark/endpoint',
        serviceName: 'stark-service',
        operationName: 'snow-1',
        duration: 3404000,
        error: false
    },
    queriedService: {
        duration: 31000,
        durationPercent: 64,
        error: true
    },
    queriedOperation: {
        duration: 1,
        durationPercent: 0,
        error: false
    },
    startTime: 1532623718819000,
    duration: 390000
}];

describe('TracesSearchStore', () => {
    let server = null;
    const store = new TracesSearchStore();

    beforeEach(() => {
        server = new MockAdapter(axios);
        // Mocking an empty response for the timeline API.
        // TODO: Change to actual tests.
        server.onGet(/^\/api\/traces\/timeline\?/g).reply(200, []);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches traces and timeline from the api with a query with a timerange', (done) => {
        server.onGet('/api/traces?spanLevelFilters=%5B%7B%22serviceName%22%3A%22test-service%22%2C%22operationName%22%3A%22all%22%7D%5D&startTime=10000000&endTime=900000000').reply(200, stubTrace);

        store.fetchTraceResults('spanLevelFilters=%5B%7B%22serviceName%22%3A%22test-service%22%2C%22operationName%22%3A%22all%22%7D%5D&startTime=10000000&endTime=900000000');

        when(
            () => store.searchResults.length > 0,
            () => {
                expect(store.searchResults).to.have.length(1);
                done();
            });
    });

    it('fetches traces from the api with an query consisting of an operationName of all', (done) => {
        server.onGet('/api/traces?spanLevelFilters=%5B%7B%22serviceName%22%3A%22test-service%22%2C%22operationName%22%3A%22all%22%7D%5D&startTime=1000&endTime=1000').reply(200, stubTrace);
        const spanLevelFilters = JSON.stringify([{serviceName: 'test-service', operationName: 'all'}]);
        store.fetchSearchResults({ spanLevelFilters, startTime: 1, endTime: 1 });

        when(
            () => store.searchResults.length > 0,
            () => {
                expect(store.searchResults).to.have.length(1);
                done();
            });
    });

    it('fetches traces from the api and decodes the components before submitting them', (done) => {
        server.onGet('/api/traces?spanLevelFilters=%5B%7B%22serviceName%22%3A%22test-service%22%2C%22operationName%22%3A%22test-operation%22%7D%5D&startTime=1000&endTime=1000').reply(200, stubTrace);
        const spanLevelFilters = JSON.stringify([{serviceName: 'test-service', operationName: 'test-operation'}]);
        store.fetchSearchResults({ spanLevelFilters, startTime: 1, endTime: 1 });

        when(
            () => store.searchResults.length > 0,
            () => {
                expect(store.searchResults).to.have.length(1);
                done();
            });
    });
});

describe('TracesDetailsStore', () => {
    let server = null;
    const store = new TraceDetailsStore();

    beforeEach(() => {
        server = new MockAdapter(axios);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches the trace\'s details from the api', (done) => {
        server.onGet('/api/trace/traceid').reply(200, stubTraceDetails);

        store.fetchTraceDetails('traceid');

        when(
            () => store.spans.length > 0,
            () => {
                expect(store.spans).to.have.length(3);
                done();
            }
        );
    });

    it('generates the correct tag dictionary from trace details spans', () => {
        store.spans = stubTraceDetails;

        const expectedTags = {
            url: 'http://trace.io/blah',
            url2: 'some:data'
        };

        expect(store.tags).to.deep.equal(expectedTags);
    });

    it('generates timelinespans from spans', () => {
        store.spans = stubTraceDetails;
        const timelineSpans = store.timelineSpans;

        expect(timelineSpans).to.be.an('array');
        expect(timelineSpans).to.have.length(3);
        expect(timelineSpans[0]).to.contain.keys('traceId', 'spanId', 'serviceName', 'operationName', 'startTime', 'duration', 'logs', 'tags', 'children', 'startTimePercent', 'depth', 'expandable', 'display', 'expanded');
    });

    it('formats related traces api response into correct format', (done) => {
        server.onGet('/api/traces?url2=some%3Adata&startTime=1000&endTime=1000').reply(200, stubRawTraceData);

        const searchQuery = {
            serviceName: '',
            url2: 'some:data',
            startTime: 1,
            endTime: 1
        };

        store.fetchRelatedTraces(searchQuery);

        when(
            () => store.relatedTraces.length > 0,
            () => {
                expect(store.relatedTraces).to.have.length(1);
                const trace = store.relatedTraces[0];
                expect(trace).to.contain.keys('traceId', 'serviceName', 'operationName', 'spanCount', 'startTime', 'rootError', 'services', 'duration');
                done();
            }
        );
    });

    it('expects haystack exception to be thrown when query returns no data', () => {
        server.onGet('/api/traces?url2=some%3Adata&startTime=1000&endTime=1000').reply(404);

        const searchQuery = {
            serviceName: '',
            url2: 'some:data',
            startTime: 1,
            endTime: 1
        };

        try {
            store.fetchRelatedTraces(searchQuery);
        } catch (error) {
            expect(error).to.eql('HaystackApiException');
        }
    });
});
