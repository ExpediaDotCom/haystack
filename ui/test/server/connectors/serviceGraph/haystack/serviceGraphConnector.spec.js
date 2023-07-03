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

const axios = require('axios');
const MockAdapter = require('axios-mock-adapter');
const serviceGraphConnector = require('../../../../../server/connectors/serviceGraph/haystack/serviceGraphConnector');

const stubResponse = {
    edges: [{
        source: {
            name: 'test'
        },
        destination: {
            name: 'test-2'
        },
        stats: {
            count: 5000,
            errorCount: 5000
        }
    }, {
        source: 'test-2',
            destination: 'test-3',
        stats: {
        count: 1000,
            errorCount: 1000
        }
    }]
};

describe('serviceGraphConnector', () => {
    let server;

    before(() => {
        server = new MockAdapter(axios);
        server.onGet('undefined?from=1530828169&to=1530829069').reply(200, stubResponse);
    });

    after(() => {
        server = null;
    });

    it('pulls and formats service graph data', () => serviceGraphConnector.getServiceGraphForTimeLine(1530828169, 1530829069).then(result => expect(result).to.have.length(1)));
});
