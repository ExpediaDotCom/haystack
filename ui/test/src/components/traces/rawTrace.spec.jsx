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

import React from 'react';
import { expect } from 'chai';
import axios from 'axios';
import { when } from 'mobx';
import MockAdapter from 'axios-mock-adapter';
import { mount } from 'enzyme';
import sinon from 'sinon';

import {RawTraceStore} from '../../../../src/components/traces/stores/rawTraceStore';
import RawTraceModal from '../../../../src/components/traces/details/rawTraceModal';


const fulfilledPromise = {
    case: ({fulfilled}) => fulfilled()
};

const stubTrace = [{
    traceId: 'test-stub'
}];

function createStubStore(results, promise) {
    const store = new RawTraceStore();
    sinon.stub(store, 'fetchRawTrace', () => {
        store.rawTrace = results;
        promise ? store.promiseState = promise : null;
    });

    return store;
}

describe('RawTraceStore', () => {
    let server = null;
    const store = new RawTraceStore();

    beforeEach(() => {
        server = new MockAdapter(axios);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches traces from the api with a query', (done) => {
        server.onGet('/api/trace/raw/test-stub').reply(200, stubTrace);

        store.fetchRawTrace('test-stub');

        when(
            () => store.rawTrace.length > 0,
            () => {
                expect(store.rawTrace).to.have.length(1);
                done();
            });
    });
});

describe('RawTrace', () => {
    it('should render a formatted panel with the raw span from the store', () => {
        const store = createStubStore(stubTrace, fulfilledPromise);
        const wrapper = mount(<RawTraceModal isOpen closeModal={() => null} traceId={'test-stub'} rawTraceStore={store} />);

        expect(wrapper.find('Modal')).to.have.length(1);
    });
});
