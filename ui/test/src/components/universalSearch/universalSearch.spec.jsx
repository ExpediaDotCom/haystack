/*
 * Copyright 2018 Expedia Group
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

import React from 'react';
import {mount} from 'enzyme';
import {expect} from 'chai';
import sinon from 'sinon';
import {MemoryRouter} from 'react-router-dom';
import MockAdapter from 'axios-mock-adapter';
import axios from 'axios';
import moment from 'moment';

import UniversalSearch from '../../../../src/components/universalSearch/universalSearch';
import Autosuggest from '../../../../src/components/universalSearch/searchBar/autosuggest';
import SearchBar from '../../../../src/components/universalSearch/searchBar/searchBar';

import uiState from '../../../../src/components/universalSearch/searchBar/stores/searchBarUiStateStore';
import {OperationStore} from '../../../../src/stores/operationStore';

const stubLocation = {
    search: ''
};

const stubHistory = {
    location: {
        search: ''
    },
    push: (location) => {
        stubLocation.search = location.search;
    }
};

const stubOptions = {
    error: {values: ['true, false'], isRangeQuery: false},
    serviceName: {values: ['test-a', 'test-b', 'test-c', 'whitespace test'], isRangeQuery: false},
    operationName: {values: [], isRangeQuery: false},
    traceId: {values: [], isRangeQuery: false}
};

// STUBS FOR BACKEND SERVICE RESPONSES
const stubSearchableKeys = {
    traceId: {isRangeQuery: false},
    spanId: {isRangeQuery: false},
    serviceName: {isRangeQuery: false},
    operationName: {isRangeQuery: false},
    error: {isRangeQuery: false}
};

const stubServices = [
    'root-service',
    'lannister-service',
    'stark-service',
    'tyrell-service',
    'targaryen-service',
    'baratheon-service',
    'dragon-service',
    'westeros-service'
];

const stubOperations = [
    'mormont-1',
    'seaworth-1',
    'bolton-1',
    'baelish-1',
    'snow-1',
    'tully-1',
    'dondarrion-1',
    'grayjoy-1',
    'clegane-1',
    'drogo-1',
    'tarley-1'
];
// Two traces are neccessary to prevent triggering the trace details api.
const stubTraces = [
    {
        traceId: '15b83d5f-64e1-4f69-b038-aaa23rfn23r',
        root: {
            url: 'test-1',
            serviceName: 'test-1',
            operationName: 'test-1'
        },
        services: [
            {
                name: 'abc-service',
                duration: 89548,
                spanCount: 12
            },
            {
                name: 'def-service',
                duration: 89548,
                spanCount: 29
            },
            {
                name: 'ghi-service',
                duration: 89548,
                spanCount: 31
            }
        ],
        error: true,
        startTime: 1499975993,
        duration: 89548
    },
    {
        traceId: '23g89z5f-64e1-4f69-b038-c123rc1c1r1',
        root: {
            url: 'test-2',
            serviceName: 'test-2',
            operationName: 'test-2'
        },
        services: [
            {
                name: 'abc-service',
                duration: 1000000,
                spanCount: 11
            },
            {
                name: 'def-service',
                duration: 89548,
                spanCount: 12
            },
            {
                name: 'ghi-service',
                duration: 89548,
                spanCount: 12
            }
        ],
        error: false,
        startTime: 1499985993,
        duration: 17765
    }
];

const stubShortQuery = [[{key: 'serviceName', value: 'test', operator: '='}]];

const stubWhitespaceQuery = [[{key: 'serviceName', value: 'whitespace test', operator: '='}]];

const stubLongQuery = [[{key: 'serviceName', value: 'test', operator: '='}, {key: 'error', value: 'true', operator: '='}]];

function createOperationStubStore() {
    const store = new OperationStore();
    store.operations = ['test-operation-a', 'test operation b'];
    sinon.stub(store, 'fetchOperations', (val, callback) => {
        callback();
    });
    return store;
}

function createStubUiStateStore(queries = [], timeWindow = {}, pendingQuery = []) {
    const store = uiState;
    store.queries = queries;
    store.timeWindow = timeWindow;
    store.pendingQuery = pendingQuery;

    return store;
}
//
// const updatedStubLocation = {
//     search: '?query_1.serviceName=test-service&time.preset=1h'
// };

describe('<UniversalSearch />', () => {
    let server;

    before(() => {
        server = new MockAdapter(axios);
        server.onGet('/api/traces/searchableKeys').reply(200, stubSearchableKeys);
        server.onGet('/api/alerts/root-service/unhealthyCount').reply(200, 0);
        server.onGet('/api/services').reply(200, stubServices);
        server.onGet('/api/operations?serviceName=root-service').reply(200, stubOperations);
        server.onGet(/^\/api\/traces\?/g).reply(200, stubTraces);
        server.onGet(/^\/api\/traces\/timeline\?/g).reply(200, []);
    });

    after(() => {
        server = null;
    });

    it('should render the universalSearch panel', () => {
        const wrapper = mount(
            <MemoryRouter>
                <UniversalSearch.WrappedComponent location={stubLocation} history={stubHistory} />
            </MemoryRouter>
        );
        expect(wrapper.find('.universal-search-panel')).to.have.length(1);
    });

    // todo: fix issue with useEffect hook breaking this test
    // it('should update the state and rerender upon new location prop', () => {
    //     const wrapper = shallow(<UniversalSearch.WrappedComponent location={stubLocation} history={stubHistory}/>);
    //
    //     wrapper.setProps({location: updatedStubLocation});
    //
    //     expect(wrapper.state().search.query_1.serviceName).to.equal('test-service');
    // });

    it('should render traces trends and alerts with a serviceName query', () => {
        const stubServiceNameLocation = {search: '?query_1.serviceName=root-service&time.preset=1h'};
        const wrapper = mount(
            <MemoryRouter>
                <UniversalSearch.WrappedComponent location={stubServiceNameLocation} history={stubHistory} />
            </MemoryRouter>
        );

        expect(wrapper.find('.universal-search-bar-tabs__nav-text').length).to.equal(3);
    });

    it('should render only traces with anything other than serviceName or operationName', () => {
        const stubErrorLocation = {search: '?query_1.error=true&time.preset=1h'};
        const wrapper = mount(
            <MemoryRouter>
                <UniversalSearch.WrappedComponent location={stubErrorLocation} history={stubHistory} />
            </MemoryRouter>
        );

        expect(wrapper.find('.universal-search-bar-tabs__nav-text').length).to.equal(1);
    });

    it('should update chips and time preset when location changes', () => {
        const stubQueryOne = {search: '?query_1.serviceName=root-service&time.preset=1h'};
        const stubQueryTwo = {search: '?query_1.serviceName=new-root-service&query_2.error=true&time.preset=4h'};
        const wrapper = mount(
            <MemoryRouter>
                <UniversalSearch.WrappedComponent location={stubQueryOne} history={stubHistory} />
            </MemoryRouter>
        );

        expect(wrapper.find('QueryBank').instance().props.uiState.queries.length).to.equal(1);
        wrapper.setProps({
            children: React.cloneElement(wrapper.props().children, {location: stubQueryTwo})
        });

        // Ensure that chips were updated with the location change
        expect(wrapper.find('QueryBank').instance().props.uiState.queries.length).to.equal(2);
    });

    it('should allow whitelisted keys with a period in them', () => {
        const stubQuery = {search: '?query_1.test.key=root-service'};
        const wrapper = mount(
            <MemoryRouter>
                <UniversalSearch.WrappedComponent location={stubQuery} history={stubHistory} />
            </MemoryRouter>
        );

        expect(wrapper.find('QueryBank').instance().props.uiState.queries.length).to.equal(1);
    });

    it('should add new searches to the history cookie', () => {
        global.window.document.cookie = 'searchhistory=[]'; // reset search history cookie
        const stubQueryOne = {search: '?query_1.serviceName=root-service&time.preset=1h'};
        const stubQueryTwo = {search: '?query_1.serviceName=new-root-service&query_1.error=true&time.preset=4h'};
        const wrapper = mount(
            <MemoryRouter>
                <UniversalSearch.WrappedComponent location={stubQueryOne} history={stubHistory} />
            </MemoryRouter>
        );

        expect(wrapper.find('QueryBank').instance().props.uiState.searchHistory.length).to.equal(0);
        wrapper.setProps({
            children: React.cloneElement(wrapper.props().children, {location: stubQueryTwo})
        });

        expect(wrapper.find('QueryBank').instance().props.uiState.searchHistory.length).to.equal(1);
    });
});

describe('uiState', () => {
    it('should add a custom time frame to search', () => {
        const UiState = createStubUiStateStore([], {startTime: 10, endTime: 10});
        const search = UiState.getCurrentSearch();

        expect(search.time.from).to.equal(10);
    });

    it('should add a preset time frame to search', () => {
        const UiState = createStubUiStateStore([], {timePreset: '1h'});
        const search = UiState.getCurrentSearch();

        expect(search.time.preset).to.equal('1h');
    });
});

describe('<SearchBar />', () => {
    before(() => {
        global.window.document.cookie = 'searchhistory=[]'; // reset search history cookie
    });

    it('should show history on the search bar', () => {
        const stubSearchOne = {serviceName: 'test-1'};
        const stubSearchTwo = {serviceName: 'test-2'};
        const wrapper = mount(<SearchBar search={stubSearchOne} handleSearch={() => {}} />);
        wrapper.setProps({search: stubSearchTwo});

        expect(wrapper.find('.usb-suggestions__guide-history-list')).to.have.length(1);
    });

    it('should show not show duplicate history or tabId', () => {
        const stubSearchOne = {serviceName: 'test-1'};
        const stubSearchTwo = {serviceName: 'test-2'};
        const stubSearchThree = {serviceName: 'test-1', tabId: 'trends'};
        const wrapper = mount(<SearchBar search={stubSearchOne} handleSearch={() => {}} />);

        wrapper.setProps({search: stubSearchTwo});
        expect(wrapper.find('.usb-suggestions__guide-history-list')).to.have.length(1);

        wrapper.setProps({search: stubSearchOne});
        expect(wrapper.find('.usb-suggestions__guide-history-list')).to.have.length(1);

        wrapper.setProps({search: stubSearchThree});
        expect(wrapper.find('.usb-suggestions__guide-history-list')).to.have.length(1);
    });
});

describe('<Autosuggest />', () => {
    it('should render the autosuggest panel', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        expect(wrapper.find('.usb-wrapper')).to.have.length(1);
    });

    it('should show time window upon selecting the timepicker', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        expect(wrapper.find('.timerange-picker').length).to.equal(0);

        wrapper.find('.usb-timepicker__button').simulate('click');

        expect(wrapper.find('.timerange-picker').length).to.equal(1);
    });

    it('should update the picker upon selecting a preset', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        wrapper.find('.usb-timepicker__button').simulate('click');

        wrapper
            .find('.timerange-picker__preset')
            .first()
            .simulate('click');
        expect(wrapper.find('TimeWindowPicker').prop('uiState').timeWindow.timePreset).to.equal('5m');
    });

    it('should update the picker upon manually selecting a time', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        expect(wrapper.find('TimeWindowPicker').prop('uiState').timeWindow.startTime).to.equal(undefined);

        wrapper.find('.usb-timepicker__button').simulate('click');
        wrapper
            .find('.datetimerange-picker')
            .first()
            .simulate('click');
        wrapper
            .find('.rdtDay')
            .first()
            .simulate('click');
        wrapper.find('.custom-timerange-apply').simulate('click');

        expect(wrapper.find('TimeWindowPicker').prop('uiState').timeWindow.startTime).to.be.a('number');
    });

    it('should retain custom time when re-opening the picker after previously selecting a custom time', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        wrapper.find('.usb-timepicker__button').simulate('click');
        const customTime = '04/18/1988 8:00 AM';
        const timeRangePicker = wrapper.find('TimeRangePicker');
        const timeWindowPicker = wrapper.find('TimeWindowPicker');

        // Change date, submit, and re-open time picker to check if date retains
        timeWindowPicker.instance().showTimePicker();
        timeRangePicker.instance().handleChangeStartDate(moment(customTime, 'MM-DD-YYYY H:m'));
        timeRangePicker.instance().handleCustomTimeRange();
        timeWindowPicker.instance().showTimePicker();

        const uiStartTime = wrapper
            .find('.datetimerange-picker')
            .find('.form-control')
            .first()
            .instance().value;
        expect(uiStartTime).to.equal(customTime);
    });

    it('should populate suggestions when input is focused', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        expect(wrapper.find('.usb-suggestions__field').length).to.equal(0);
        const input = wrapper.find('.usb-searchbar__input');
        input.prop('onFocus')({target: {value: ''}});
        wrapper.update();
        expect(wrapper.find('.usb-suggestions__field').length).to.equal(4);
    });

    it('update suggestion string index on suggestion mouseover', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        const input = wrapper.find('.usb-searchbar__input');
        input.prop('onChange')({target: {value: 'e'}});
        wrapper.update();

        const firstSuggestion = wrapper.find('.usb-suggestions__field').last();
        firstSuggestion.simulate('mouseEnter');
        expect(wrapper.find('Suggestions').prop('suggestionIndex')).to.equal(3);
    });

    it('update suggestion string index on suggestion mouseover', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        const input = wrapper.find('.usb-searchbar__input');
        input.prop('onChange')({target: {value: 'e'}});
        wrapper.update();

        const firstSuggestion = wrapper.find('.usb-suggestions__field').first();
        firstSuggestion.simulate('mouseEnter');
        expect(wrapper.find('Suggestions').prop('suggestionIndex')).to.equal(0);
    });

    it('suggestions should disappear when escape is pressed', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        const input = wrapper.find('.usb-searchbar__input');
        input.prop('onFocus')({target: {value: ''}});

        input.prop('onKeyDown')({keyCode: 27, preventDefault: () => {}});
        expect(wrapper.find('Suggestions').prop('suggestionStrings').length).to.equal(0);
    });

    it('should change suggestion when input is changed', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        expect(wrapper.find('Suggestions').prop('suggestionStrings').length).to.equal(0);
        const input = wrapper.find('.usb-searchbar__input');
        input.prop('onChange')({target: {value: 'err'}});
        wrapper.update();
        expect(wrapper.find('Suggestions').prop('suggestionStrings').length).to.equal(1);
    });

    it('should change be able to suggest when encapsulating value in quotations', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        expect(wrapper.find('Suggestions').prop('suggestionStrings').length).to.equal(0);
        const input = wrapper.find('.usb-searchbar__input');
        input.prop('onChange')({target: {value: 'error="tr'}});
        wrapper.update();
        expect(wrapper.find('Suggestions').prop('suggestionStrings').length).to.equal(1);
    });

    it('should change state with down and up arrow and upon pressing enter', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        expect(wrapper.find('Suggestions').prop('suggestionIndex')).to.equal(null);
        let input = wrapper.find('.usb-searchbar__input');
        input.prop('onFocus')({target: {value: ''}});

        input = wrapper.update().find('.usb-searchbar__input');
        input.prop('onKeyDown')({keyCode: 40, preventDefault: () => {}});
        wrapper.update();
        expect(wrapper.find('Suggestions').prop('suggestionIndex')).to.equal(0);
        expect(wrapper.find('Suggestions').prop('suggestedOnType')).to.equal('Keys');

        input = wrapper.update().find('.usb-searchbar__input');
        input.prop('onKeyDown')({keyCode: 38, preventDefault: () => {}});
        wrapper.update();
        expect(wrapper.find('Suggestions').prop('suggestionIndex')).to.equal(3);
        expect(wrapper.find('Suggestions').prop('suggestedOnType')).to.equal('Keys');

        input = wrapper.update().find('.usb-searchbar__input');
        input.prop('onKeyDown')({keyCode: 13, preventDefault: () => {}});
        wrapper.update();
        expect(wrapper.find('Suggestions').prop('suggestionIndex')).to.equal(null);
        expect(wrapper.find('Suggestions').prop('suggestedOnType')).to.equal('Values');
    });

    it('should be able to modify a single key/value query', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore(stubShortQuery)}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        expect(wrapper.find('QueryBank').prop('uiState').queries.length).to.equal(1);
        wrapper.find('.usb-chip__key').simulate('click');

        expect(wrapper.find('QueryBank').prop('uiState').queries.length).to.equal(0);
    });

    it('should be able to modify a multiple key/value query', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore(stubLongQuery)}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        wrapper
            .find('.usb-chip__key')
            .first()
            .simulate('click');

        expect(wrapper.find('QueryBank').prop('uiState').pendingQuery.length).to.equal(2);
    });

    it('should be able to properly modify a query with whitespace', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore(stubWhitespaceQuery)}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        wrapper
            .find('.usb-chip__key')
            .first()
            .simulate('click');
        expect(wrapper.find('QueryBank').prop('uiState').pendingQuery[0].value).to.equal('whitespace test');
    });

    it('should be able to delete an existing chip', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore(stubShortQuery)}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        expect(wrapper.find('QueryBank').prop('uiState').queries.length).to.equal(1);
        wrapper.find('.usb-chip__delete').simulate('click');
        expect(wrapper.find('QueryBank').prop('uiState').queries.length).to.equal(0);
    });

    it('should be able to add multiple key/values to one query', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        const input = wrapper.find('.usb-searchbar__input');
        input.instance().value = 'serviceName=test';
        input.prop('onKeyDown')({keyCode: 13, preventDefault: () => {}});
        input.instance().value = 'error=true';
        input.prop('onKeyDown')({keyCode: 13, preventDefault: () => {}});

        const pendingQuery = wrapper.find('QueryBank').prop('uiState').pendingQuery;
        expect(pendingQuery.length).to.equal(2);
    });

    it('should be able to add a chip by pressing space bar', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        const input = wrapper.find('.usb-searchbar__input');
        input.instance().value = 'serviceName=test';
        input.prop('onKeyDown')({keyCode: 32, preventDefault: () => {}});
        const pendingQuery = wrapper.find('QueryBank').prop('uiState').pendingQuery;

        expect(pendingQuery.length).to.equal(1);
    });

    it('should be able to add a chip with whitespace', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );
        const input = wrapper.find('.usb-searchbar__input');
        input.instance().value = 'serviceName="whitespace test"';
        input.prop('onKeyDown')({keyCode: 32, preventDefault: () => {}});
        const pendingQuery = wrapper.find('QueryBank').prop('uiState').pendingQuery;

        expect(pendingQuery.length).to.equal(1);
    });

    it('should be able to handle range operators', () => {
        const customOption = {duration: {values: [], isRangeQuery: true}};
        const wrapper = mount(
            <Autosuggest
                options={customOption}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        const input = wrapper.find('.usb-searchbar__input');
        input.instance().value = 'duration>10000';

        input.prop('onKeyDown')({keyCode: 32, preventDefault: () => {}});
        const pendingQuery = wrapper.find('QueryBank').prop('uiState').pendingQuery;

        expect(pendingQuery.length).to.equal(1);
        expect(pendingQuery[0].key).to.equal('duration');
        expect(pendingQuery[0].operator).to.equal('>');
    });

    it('should fail submission with an invalid KV pair', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        const input = wrapper.find('.usb-searchbar__input');
        input.instance().value = 'serviceName=';

        input.prop('onKeyDown')({keyCode: 32, preventDefault: () => {}});
        const pendingQuery = wrapper.find('QueryBank').prop('uiState').pendingQuery;

        expect(pendingQuery.length).to.equal(0);
    });

    it('should fail submission with a non-whitelisted key', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        const input = wrapper.find('.usb-searchbar__input');
        input.instance().value = 'failure=asdf';

        input.prop('onKeyDown')({keyCode: 32, preventDefault: () => {}});
        const pendingQuery = wrapper.find('QueryBank').prop('uiState').pendingQuery;

        expect(pendingQuery.length).to.equal(0);
    });

    it('should be able to paste a query', () => {
        const wrapper = mount(
            <Autosuggest
                options={stubOptions}
                uiState={createStubUiStateStore()}
                search={() => {}}
                services={stubServices}
                operationStore={createOperationStubStore()}
            />
        );

        wrapper.find('input').simulate('paste', {clipboardData: {getData: () => 'serviceName=asdf error=true'}});
        const pendingQuery = wrapper.find('QueryBank').prop('uiState').pendingQuery;

        expect(pendingQuery.length).to.equal(2);
    });
});
