/*
 * Copyright 2018 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */

import React from 'react';
import {shallow} from 'enzyme';
import {expect} from 'chai';
import sinon from 'sinon';

import ServicePerformance from '../../../src/components/servicePerf/servicePerformance';
import {ServicePerfStore} from '../../../src/components/servicePerf/stores/servicePerfStore';

const servicePerfStubResults = {
    children: [
        {
            serviceName: 'Service 1',
            successPercent: 90,
            failureCount: 26088,
            totalCount: 5394949947
        },
        {
            serviceName: 'Service 2',
            successPercent: 97,
            failureCount: 6001,
            totalCount: 6406933
        },
        {
            serviceName: 'Service 3',
            successPercent: 98,
            failureCount: 21632,
            totalCount: 1000000
        },
        {
            serviceName: 'Service 4',
            successPercent: 96,
            failureCount: 81338,
            totalCount: 84143
        }
    ]
};

const stubHistory = {
    location: {
        search: '/'
    }
};

function createServicePerfStubStore(results) {
    const store = new ServicePerfStore();
    store.servicePerfStats = [];
    sinon.stub(store, 'fetchServicePerf', () => {
        store.servicePerfStats = results;
    });
    return store;
}

describe('<ServicePerformance />', () => {
    it('should render the servicePerformance', () => {
        const servicePerfStore = createServicePerfStubStore(servicePerfStubResults);
        const wrapper = shallow(<ServicePerformance history={stubHistory} servicePerfStore={servicePerfStore} servicePerfStats={servicePerfStore.servicePerfStats}/>);
        expect(wrapper.find('.container')).to.have.length(1);
        expect(wrapper.find('.servicePerformance')).to.have.length(1);
    });
});
