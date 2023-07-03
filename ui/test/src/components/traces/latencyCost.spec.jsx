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

import LatencyCostTab from '../../../../src/components/traces/details/latency/latencyCostTab';
import LatencyCostStore from '../../../../src/components/traces/stores/latencyCostStore';

const stubLatencyCost = [{
    from: {
        serviceName: 'service-a',
        infrastructureProvider: 'aws',
        infrastructureLocation: 'us-west-2'
    },
    to: {
        serviceName: 'service-b',
        infrastructureProvider: 'aws',
        infrastructureLocation: 'us-west-2'
    },
    networkDelta: 65
}, {
    from: {
        serviceName: 'service-b',
        infrastructureProvider: 'aws',
        infrastructureLocation: 'us-west-2'
    },
    to: {
        serviceName: 'service-c',
        infrastructureProvider: '',
        infrastructureLocation: ''
    },
    networkDelta: null
}];

const stubLatencyCostTrends = [{
        from: {
            serviceName: 'service-a',
            infrastructureProvider: 'aws',
            infrastructureLocation: 'us-west-2'
        },
        to: {
            serviceName: 'service-b',
            infrastructureProvider: 'aws',
            infrastructureLocation: 'us-west-2'
        },
        meanNetworkDelta: 65,
        medianNetworkDelta: 65
    }, {
        from: {
            serviceName: 'service-b',
            infrastructureProvider: 'aws',
            infrastructureLocation: 'us-west-2'
        },
        to: {
            serviceName: 'service-c',
            infrastructureProvider: '',
            infrastructureLocation: ''
        },
        meanNetworkDelta: 65,
        medianNetworkDelta: 65
}];

describe('LatencyCostStore', () => {
    let server = null;
    const store = LatencyCostStore;

    beforeEach(() => {
        server = new MockAdapter(axios);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches latency cost from api', (done) => {
        server.onGet('/api/trace/traceId/latencyCost').reply(200, ['success']);
        store.fetchLatencyCost('traceId');

        when(
            () => store.latencyCost.length > 0,
            () => {
                expect(store.latencyCost).to.have.length(1);
                done();
            });
    });
});

describe('LatencyCostTab', () => {
    it('should render the latency cost tab', () => {
        const wrapper = mount(<LatencyCostTab latencyCostTrends={stubLatencyCostTrends} latencyCost={stubLatencyCost}/>);

        expect(wrapper.find('.latency-summary')).to.have.length(1);
    });
});
