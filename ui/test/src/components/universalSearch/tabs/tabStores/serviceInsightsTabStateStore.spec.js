/*
 * Copyright 2019 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the 'License");
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

import {expect} from 'chai';

describe('class ServiceInsightsTabStateStore()', () => {
    const modulePath = '../../../../../../src/components/universalSearch/tabs/tabStores/serviceInsightsTabStateStore';
    let oldHaystackUiConfig = null;
    beforeEach(() => {
        // clear require cache
        delete require.cache[require.resolve(modulePath)];
        oldHaystackUiConfig = window.haystackUiConfig;
    });
    afterEach(() => {
        window.haystackUiConfig = oldHaystackUiConfig;
    });
    it('should initialize `isAvailable` as false by default', () => {
        window.haystackUiConfig = {};
        let store = require(modulePath).default; // eslint-disable-line
        expect(store.isAvailable).to.equal(false);
    });
    it('should initialize `isAvailable` as true if `enableServiceInsights` is enabled and serviceName is passed in', () => {
        window.haystackUiConfig = {
            subsystems: ['serviceInsights']
        };

        let store = require(modulePath).default; // eslint-disable-line
        store.init({}, {
            serviceName: 'mock-ui'
        });
        expect(store.isAvailable).to.equal(true);
    });
    it('should set `isAvailable` as false if `tabId` is set to `serviceInsights` and feature is disabled', () => {
        window.haystackUiConfig = {
            subsystems: []
        };
        let store = require(modulePath).default; // eslint-disable-line
        store.init({
            tabId: 'serviceInsights'
        }, {
            serviceName: 'mock-ui'
        });
        expect(store.isAvailable).to.equal(false);
    });

    it('should set `isAvailable` as true if `tabId` is set to `serviceInsights` and feature is enabled', () => {
        window.haystackUiConfig = {
            subsystems: ['serviceInsights']
        };
        let store = require(modulePath).default; // eslint-disable-line
        store.init({
            tabId: 'serviceInsights'
        }, {});
        expect(store.isAvailable).to.equal(true);
    });

    it('should set `isAvailable` as true if feature is enabled and has valid search', () => {
        window.haystackUiConfig = {
            subsystems: ['serviceInsights']
        };
        let store = require(modulePath).default; // eslint-disable-line
        store.init({}, {
            traceId: '123456789'
        });
        expect(store.isAvailable).to.equal(true);

        store.init({}, {
            serviceName: 'mock-ui'
        });
        expect(store.isAvailable).to.equal(true);
    });

    it('should return a valid serviceInsights store from fetch()', () => {
        window.haystackUiConfig = {
            subsystems: ['serviceInsights']
        };
        let store = require(modulePath).default; // eslint-disable-line
        store.init({}, {});
        expect(JSON.stringify(store.fetch().serviceInsights)).to.equal('{}');
    });
});
