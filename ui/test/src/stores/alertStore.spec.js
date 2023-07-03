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

import {ServiceAlertsStore} from '../../../src/components/alerts/stores/serviceAlertsStore';
import {AlertDetailsStore} from '../../../src/components/alerts/stores/alertDetailsStore';

const stubService = 'stub-service';

const stubAlert = [{}];

const stubDetails = [{}];

const stubPreset = {shortName: '6h', longName: '6 hours', value: 21600000};

const stubSubscriptions = [{}];

describe('ServiceAlertsStore', () => {
    let server = null;
    const store = new ServiceAlertsStore();

    beforeEach(() => {
        server = new MockAdapter(axios);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches active alerts from API', (done) => {
        server.onGet(/\/api\/alerts\/\s*/).reply(200, stubAlert);

        store.fetchServiceAlerts(stubService, '5m', stubPreset);

        when(
            () => store.alerts.length > 0,
            () => {
                expect(store.alerts).to.have.length(1);
                done();
            });
    });
});

describe('AlertDetailsStore', () => {
    let server = null;
    const store = new AlertDetailsStore();

    beforeEach(() => {
        server = new MockAdapter(axios);
    });

    afterEach(() => {
        server = null;
    });

    it('fetches alert details', (done) => {
        server.onGet('/api/alert/svc/op/type/history?from=8600000&interval=5m').reply(200, stubDetails);
        store.fetchAlertHistory('svc', 'op', 'type', 8600000, '5m');

        when(
            () => store.alertHistory.length > 0,
            () => {
                expect(store.alertHistory).to.have.length(1);
                done();
            });
    });

    it('fetches alert subscriptions', (done) => {
        server.onGet('/api/alert/svc/op/type/interval/subscriptions').reply(200, stubSubscriptions);
        store.fetchAlertSubscriptions('svc', 'op', 'type', 'interval');

        when(
            () => store.alertSubscriptions.length > 0,
            () => {
                expect(store.alertSubscriptions).to.have.length(1);
                done();
            });
    });
});
