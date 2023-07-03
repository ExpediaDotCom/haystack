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

/* eslint-disable react/prop-types, no-unused-expressions */

import { expect } from 'chai';
import { when } from 'mobx';
import MockAdapter from 'axios-mock-adapter';
import axios from 'axios';

import {ServiceGraphStore} from '../../../src/components/serviceGraph/stores/serviceGraphStore';

const stubFilter = {from: '1', to: '2'};

const stubGraph = [
    {
        destination: {
            name: 'service-2'
        },
        source: {
            name: 'service-1',
            tags: {
                DEPLOYMENT: 'aws'
            }
        },
        stats: {
            count: 12,
            errorCount: 2.5
        }
    },
    {
        destination: {
            name: 'service-3'
        },
        source: {
            name: 'service-2'
        },
        stats: {
            count: 16,
            errorCount: 3.5
        }
    }
];

describe('ServiceGraphStore', () => {
    let server = null;
    const store = new ServiceGraphStore();

    beforeEach(() => {
        server = new MockAdapter(axios);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches service graph from the api', (done) => {
        server.onGet('/api/serviceGraph?from=1&to=2').reply(200, stubGraph);

        store.fetchServiceGraph(stubFilter);

        when(
            () => store.graphs.length > 0,
            () => {
                expect(store.graphs).to.have.length(2);
                done();
            });
    });
});
