/*
 * Copyright 2018 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the 'License');
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an 'AS IS' BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
/* eslint-disable react/prop-types */

import React from 'react';
import {mount} from 'enzyme';
import {expect} from 'chai';
import sinon from 'sinon';
import {MemoryRouter} from 'react-router';

import OperationResults from '../../../../src/components/trends/operation/operationResults';
import TrendDetails from '../../../../src/components/trends/details/trendDetails';
import {OperationStore} from '../../../../src/components/trends/stores/operationStore';

const stubLocation = {
    search: '?key1=value&key2=value'
};

const stubSearchResults = [
    {
        operationName: 'Op 1',
        totalCount: 18800,
        countPoints: [
            {value: 180, timestamp: 1508431848839},
            {value: 82, timestamp: 1508432748839},
            {value: 950, timestamp: 1508433648839},
            {value: 53, timestamp: 1508434548839}],
        avgSuccessPercent: 69.997,
        successPercentPoints: [
            {value: 180, timestamp: 1508431848839},
            {value: 82, timestamp: 1508432748839},
            {value: 950, timestamp: 1508433648839},
            {value: 53, timestamp: 1508434548839}],
        latestTp99Duration: 14530,
        tp99DurationPoints: [
            {value: 180, timestamp: 1508431848839},
            {value: 82, timestamp: 1508432748839},
            {value: 950, timestamp: 1508433648839},
            {value: 53, timestamp: 1508434548839}]
    },
    {
        operationName: 'Op 2',
        totalCount: 1800,
        countPoints: [
            {value: 380, timestamp: 1508431848839},
            {value: 22, timestamp: 1508432748839},
            {value: 930, timestamp: 1508433648839},
            {value: 23, timestamp: 1508434548839}],
        avgSuccessPercent: 69.997,
        successPercentPoints: [
            {value: 1280, timestamp: 1508431848839},
            {value: 822, timestamp: 1508432748839},
            {value: 9520, timestamp: 1508433648839},
            {value: 253, timestamp: 1508434548839}],
        latestTp99Duration: 14530,
        tp99DurationPoints: [
            {value: 1810, timestamp: 1508431848839},
            {value: 8121, timestamp: 1508432748839},
            {value: 91150, timestamp: 1508433648839},
            {value: 153, timestamp: 1508434548839}]
    },
    {
        operationName: 'Op 3',
        totalCount: 2800,
        countPoints: [
            {value: 1180, timestamp: 1508431848839},
            {value: 1812, timestamp: 1508432748839},
            {value: 9150, timestamp: 1508433648839},
            {value: 533, timestamp: 1508434548839}],
        avgSuccessPercent: 69.997,
        successPercentPoints: [
            {value: 1860, timestamp: 1508431848839},
            {value: 862, timestamp: 1508432748839},
            {value: 9650, timestamp: 1508433648839},
            {value: 563, timestamp: 1508434548839}],
        latestTp99Duration: 14530,
        tp99DurationPoints: [
            {value: 1680, timestamp: 1508431848839},
            {value: 782, timestamp: 1508432748839},
            {value: 97750, timestamp: 1508433648839},
            {value: 573, timestamp: 1508434548839}]
    },
    {
        operationName: 'Op 4',
        totalCount: 2800,
        countPoints: [
            {value: 1180, timestamp: 1508431848839},
            {value: 1812, timestamp: 1508432748839},
            {value: 9150, timestamp: 1508433648839},
            {value: 533, timestamp: 1508434548839}],
        avgSuccessPercent: 69.997,
        successPercentPoints: [
            {value: 1860, timestamp: 1508431848839},
            {value: 862, timestamp: 1508432748839},
            {value: 9650, timestamp: 1508433648839},
            {value: 563, timestamp: 1508434548839}],
        latestTp99Duration: 14530,
        tp99DurationPoints: [
            {value: 1680, timestamp: 1508431848839},
            {value: 782, timestamp: 1508432748839},
            {value: 97750, timestamp: 1508433648839},
            {value: 573, timestamp: 1508434548839}]
    }
];

const stubOperationResults = {
    count: [
        {value: 710, timestamp: 1508432128583},
        {value: 880, timestamp: 1508432188583},
        {value: 674, timestamp: 1508432248583},
        {value: 331, timestamp: 1508432308583},
        {value: 809, timestamp: 1508432368583}
    ],
    successCount: [
        {value: 312, timestamp: 1508432128583},
        {value: 90, timestamp: 1508432188583},
        {value: 68, timestamp: 1508432248583},
        {value: 46, timestamp: 1508432308583},
        {value: 425, timestamp: 1508432368583}
    ],
    failureCount: [
        {value: 505, timestamp: 1508432128583},
        {value: 745, timestamp: 1508432188583},
        {value: 286, timestamp: 1508432248583},
        {value: 46, timestamp: 1508432308583},
        {value: 164, timestamp: 1508432368583}
    ],
    meanDuration: [
        {value: 606, timestamp: 1508432128583},
        {value: 321, timestamp: 1508432188583},
        {value: 807, timestamp: 1508432248583},
        {value: 540, timestamp: 1508432308583},
        {value: 83, timestamp: 1508432368583}
    ],
    tp95Duration: [
        {value: 104, timestamp: 1508432128583},
        {value: 742, timestamp: 1508432188583},
        {value: 178, timestamp: 1508432248583},
        {value: 860, timestamp: 1508432308583},
        {value: 751, timestamp: 1508432368583}
    ],
    tp99Duration: [
        {value: 924, timestamp: 1508432128583},
        {value: 611, timestamp: 1508432188583},
        {value: 945, timestamp: 1508432248583},
        {value: 272, timestamp: 1508432308583},
        {value: 468, timestamp: 1508432368583}
    ]
};

const stubQuery = {
    from: 1508441383079,
    until: 1508444983079,
    granularity: 60000
};


const fulfilledPromise = {
    case: ({fulfilled}) => fulfilled()
};

const rejectedPromise = {
    case: ({rejected}) => rejected()
};

const pendingPromise = {
    case: ({pending}) => pending()
};

const stubService = 'test-service';
const stubOperation = 'test-operation-1';


function TrendsStubComponent({operationStore, location, serviceName}) {
    return (<section className="trends-panel">
        <OperationResults
            operationStore={operationStore}
            serviceName={serviceName}
            location={location}
            interval={null}
        />
    </section>);
}

function TrendsDetailsStubComponent({store, location, serviceName, opName}) {
    return (<TrendDetails store={store} location={location} serviceName={serviceName} opName={opName} interval={null} />);
}

function createOperationStubStore(statsResults, trendsResults, promise, statsQuery = {}, trendsQuery = {}) {
    const store = new OperationStore();
    store.statsQuery = statsQuery;
    store.trendsQuery = trendsQuery;

    sinon.stub(store, 'fetchStats', () => {
        store.statsResults = statsResults;
        store.statsPromiseState = promise;
    });

    sinon.stub(store, 'fetchTrends', () => {
        store.trendsResults = trendsResults;
        store.trendsPromiseState = promise;
    });

    store.fetchTrends();
    store.fetchStats();

    return store;
}

describe('<Trends />', () => {
    it('should trigger Service/Operation fetchStats on mount', () => {
        const operationStore = createOperationStubStore([]);
        const wrapper = mount(<TrendsStubComponent operationStore={operationStore} location={stubLocation} serviceName={stubService}/>);

        expect(wrapper.find('.trends-panel')).to.have.length(1);
        expect(operationStore.fetchStats.calledOnce);
    });

    it('should render results after getting search results', () => {
        const operationStore = createOperationStubStore(stubSearchResults, stubOperationResults, fulfilledPromise, stubQuery, stubQuery);
        const wrapper = mount(<TrendsStubComponent operationStore={operationStore} location={stubLocation} serviceName={stubService}/>);

        expect(operationStore.fetchStats.callCount).to.equal(1);
        expect(wrapper.find('.react-bs-table-container')).to.have.length(1);
        expect(wrapper.find('tr.tr-no-border')).to.have.length(4);
    });

    it('should render error if promise is rejected', () => {
        const operationStore = createOperationStubStore(stubSearchResults, stubOperationResults, rejectedPromise, stubQuery, stubQuery);
        const wrapper = mount(<TrendsStubComponent operationStore={operationStore} location={stubLocation} serviceName={stubService}/>);

        expect(wrapper.find('.error-message_text')).to.have.length(1);
        expect(wrapper.find('tr.tr-no-border')).to.have.length(0);
    });

    it('should render loading if promise is pending', () => {
        const operationStore = createOperationStubStore(stubSearchResults, stubOperationResults, pendingPromise, stubQuery, stubQuery);
        const wrapper = mount(<TrendsStubComponent operationStore={operationStore} location={stubLocation} serviceName={stubService}/>);

        expect(wrapper.find('.loading')).to.have.length(1);
        expect(wrapper.find('.error-message_text')).to.have.length(0);
        expect(wrapper.find('tr.tr-no-border')).to.have.length(0);
    });

    it('should render sparklines on each row of search results', () => {
        const operationStore = createOperationStubStore(stubSearchResults, stubOperationResults, fulfilledPromise, stubQuery, stubQuery);
        const wrapper = mount(<TrendsStubComponent operationStore={operationStore} location={stubLocation} serviceName={stubService}/>);

        expect(wrapper.find('.sparkline-container')).to.have.length(12);
    });

    it('should call operation fetchStats upon expanding a trend', () => {
        const operationStore = createOperationStubStore(stubSearchResults, stubOperationResults, fulfilledPromise, stubQuery, stubQuery);
        // eslint-disable-next-line
        const wrapper = mount(<MemoryRouter>
            <TrendsDetailsStubComponent store={operationStore} location={stubLocation} serviceName={stubService} opName={stubOperation} />
        </MemoryRouter>); // MemoryRouter used to keep Link component from reading or writing to address-bar
        expect(operationStore.fetchStats.calledOnce);
    });

    xit('should show the charts in a trend expanded view', () => {
        const operationStore = createOperationStubStore(stubSearchResults, stubOperationResults, fulfilledPromise, stubQuery, stubQuery);
        const wrapper = mount(<MemoryRouter>
            <TrendsDetailsStubComponent store={operationStore} location={stubLocation} serviceName={stubService} opName={stubOperation} />
        </MemoryRouter>);
        expect(wrapper.find('.chart-container')).to.have.length(3);
        expect(wrapper.find('#trend-row-container')).to.have.length(1);
    });

    xit('should reload the graphs upon selecting a separate time', () => {
        const operationStore = createOperationStubStore(stubSearchResults, stubOperationResults, fulfilledPromise, stubQuery, stubQuery);
        const wrapper = mount(<MemoryRouter>
            <TrendsDetailsStubComponent store={operationStore} location={stubLocation} serviceName={stubService} opName={stubOperation} />
        </MemoryRouter>);
        wrapper.find('.btn-default').first().simulate('click');
        expect(operationStore.fetchTrends.callCount).to.equal(2);
        expect(wrapper.find('.chart-container')).to.have.length(3);
        expect(wrapper.find('#trend-row-container')).to.have.length(1);
    });

    it('trend custom time picker should be responsive and change the date parameter', () => {
        const operationStore = createOperationStubStore(stubSearchResults, stubOperationResults, fulfilledPromise, stubQuery, stubQuery);

        const wrapper = mount(<MemoryRouter>
            <TrendsDetailsStubComponent store={operationStore} location={stubLocation} serviceName={stubService} opName={stubOperation} />
        </MemoryRouter>);

        // Clicking modal
        expect(wrapper.find('.custom-timerange-picker')).to.have.length(0);
        wrapper.find('.custom-btn').simulate('click');
        expect(wrapper.find('.custom-timerange-picker')).to.have.length(1);
        wrapper.find('.custom-btn').simulate('click');
        expect(wrapper.find('.custom-timerange-picker')).to.have.length(0);

        // Custom time picker
        wrapper.find('.custom-btn').simulate('click');
        wrapper.find('div.custom-timerange-picker__datetime-from').simulate('click');
        wrapper.find('.rdtOld').first().simulate('click');
        wrapper.find('div.custom-timerange-picker').simulate('click');
        wrapper.find('.btn-apply').simulate('click');
        expect(wrapper.find('.timerange-picker')).to.have.length(0);
    });

    it('trend custom time picker should be responsive and change the date parameter', () => {
        const operationStore = createOperationStubStore(stubSearchResults, stubOperationResults, fulfilledPromise, stubQuery, stubQuery);
        const wrapper = mount(<MemoryRouter>
            <TrendsDetailsStubComponent store={operationStore} location={stubLocation} serviceName={stubService} opName={stubOperation} />
        </MemoryRouter>);

        // Clicking granularity button
        wrapper.find('.dropdown-toggle').simulate('click');
        expect(wrapper.find('.granularity-button')).to.have.length(4);
        wrapper.find('.granularity-button').last().simulate('click');
    });
});
