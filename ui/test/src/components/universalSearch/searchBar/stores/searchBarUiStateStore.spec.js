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

const {SearchBarUiStateStore} = require('../../../../../../src/components/universalSearch/searchBar/stores/searchBarUiStateStore.js');

describe('class SearchBarUiStateStore()', () => {
    const mockSearch = {
        tabId: 'traces',
        time: {
            to: 2,
            from: 1
        }
    };
    let MockSearchBarUiStateStore;
    beforeEach(() => {
        MockSearchBarUiStateStore = new SearchBarUiStateStore();
        MockSearchBarUiStateStore.init(mockSearch);
    });
    it('initializes store properly', () => {
        expect(MockSearchBarUiStateStore.getCurrentSearch()).to.deep.equal({
            tabId: 'traces',
            time: {
                from: 1,
                to: 2
            }
        });
    });

    it('setOperatorFromValue() returns operator from KV pair', () => {
        expect(SearchBarUiStateStore.setOperatorFromValue(['<'])).to.equal('<');
        expect(SearchBarUiStateStore.setOperatorFromValue(['>'])).to.equal('>');
        expect(SearchBarUiStateStore.setOperatorFromValue(['='])).to.equal('=');
        expect(SearchBarUiStateStore.setOperatorFromValue([false])).to.equal('=');
    });

    it('checkKeyForPeriods() finds key with periods', () => {
        const result = SearchBarUiStateStore.checkKeyForPeriods('foo', {
            'http.status.code': 'value'
        });
        expect(result).to.equal('foo.http.status.code');
    });

    it('checkValueForPeriods() finds value of key with period', () => {
        const result = SearchBarUiStateStore.checkValueForPeriods({
            '.': 'foo'
        });
        expect(result).to.equal('foo');
    });

    it('createChip() properly transforms data', () => {
        const result = SearchBarUiStateStore.createChip('serviceName', 'foo');
        expect(result).to.deep.equal({
            key: 'serviceName',
            operator: '=',
            value: 'foo'
        });
    });

    it('properly processes chips', () => {
        const chips = [
            {
                key: 'foo',
                operator: '=',
                value: 'bar'
            },
            {
                key: 'durationKey',
                operator: '>',
                value: 'bar'
            }
        ];
        const result = SearchBarUiStateStore.turnChipsIntoSearch(chips);
        expect(result).to.deep.equal({
            foo: 'bar',
            durationKey: '>bar'
        });
    });

    it('getCurrentSearch() returns valid search object', () => {
        const chips = [[
            {
                key: 'foo',
                operator: '=',
                value: 'bar'
            },
            {
                key: 'serviceName',
                operator: '=',
                value: 'mock-ui'
            },
            {
                key: 'operationName',
                operator: '=',
                value: 'read'
            }
        ]];
        MockSearchBarUiStateStore = new SearchBarUiStateStore();
        MockSearchBarUiStateStore.init({
            tabId: 'serviceInsights',
            time: {
                from: 1,
                to: 2
            }
        });
        MockSearchBarUiStateStore.queries = chips;
        const result = MockSearchBarUiStateStore.getCurrentSearch();
        expect(result).to.deep.equal({
            query_1: {
                foo: 'bar',
                serviceName: 'mock-ui',
                operationName: 'read'
            },
            tabId: 'serviceInsights',
            time: {
                from: 1,
                to: 2
            }
        });
        MockSearchBarUiStateStore.setTimeWindow({
            timePreset: 'Last Month'
        });
        const result2 = MockSearchBarUiStateStore.getCurrentSearch();
        expect(result2).to.deep.equal({
            query_1: {
                foo: 'bar',
                serviceName: 'mock-ui',
                operationName: 'read'
            },
            tabId: 'serviceInsights',
            time: {
                preset: 'Last Month'
            }
        });
    });
});
