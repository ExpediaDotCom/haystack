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

import { expect } from 'chai';
import axios from 'axios';
import { when } from 'mobx';
import MockAdapter from 'axios-mock-adapter';


import {ServiceStore} from '../../../src/stores/serviceStore';

describe('ServiceStore', () => {
    let server = null;
    const store = new ServiceStore();

    beforeEach(() => {
        server = new MockAdapter(axios);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches services off the api', (done) => {
        server.onGet('/api/services').reply(200, '["test-service"]');

        store.fetchServices();

        when(
            () => store.services.length > 0,
            () => {
                expect(store.services).to.have.length(1);
                done();
            });

        store.fetchServices();
        expect(store.services).to.have.length(0);
    });
});
