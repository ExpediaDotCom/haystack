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


import {OperationStore} from '../../../src/stores/operationStore';

describe('OperationStore', () => {
    let server = null;
    const store = new OperationStore();

    beforeEach(() => {
        server = new MockAdapter(axios);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches operations off the api', (done) => {
        server.onGet('/api/operations?serviceName=test-service').reply(200, '["test-service"]');

        store.fetchOperations('test-service');

        when(
            () => store.operations.length > 0,
            () => {
                expect(store.operations).to.have.length(2);
                done();
            });
    });
});
