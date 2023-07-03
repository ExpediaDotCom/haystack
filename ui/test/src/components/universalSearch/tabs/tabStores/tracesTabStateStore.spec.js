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

describe('<Traces />', () => {
    const modulePath = '../../../../../../src/components/universalSearch/tabs/tabStores/tracesTabStateStore.js';
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

    it('should initialize `isAvailable` as true when accessing traces', () => {
        window.haystackUiConfig = {};
        let store = require(modulePath).default; // eslint-disable-line
        store.init({
            tabId: 'traces'
        });
        expect(store.isAvailable).to.equal(true);
    });
});
