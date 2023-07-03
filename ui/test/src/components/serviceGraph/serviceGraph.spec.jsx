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

import edges from './util/edges';
import ServiceGraph from '../../../../src/components/serviceGraph/serviceGraph';
import ServiceGraphResults from '../../../../src/components/serviceGraph/serviceGraphResults';
import ServiceGraphContainer from '../../../../src/components/serviceGraph/serviceGraphContainer';
import ServiceGraphStore from '../../../../src/components/serviceGraph/stores/serviceGraphStore';

const fulfilledPromise = {
    case: ({fulfilled}) => fulfilled()
};

const rejectedPromise = {
    case: ({rejected}) => rejected()
};

const pendingPromise = {
    case: ({pending}) => pending()
};

const stubHistory = {
    location: {
        search: '/',
        pathname: '/service/some-service/traces'
    }
};

const stubSearch = {
    serviceName: undefined,
    time: {
        preset: '1h'
    }
};

const stubGraph = [
    {
        destination: {
            name: 'service-2'
        },
        source: {
            name: 'service-1',
            tags: {
                DEPLOYMENT: 'aws'
            }
        },
        stats: {
            count: 12,
            errorCount: 2.5
        }
    },
    {
        destination: {
            name: 'service-3'
        },
        source: {
            name: 'service-2'
        },
        stats: {
            count: 16,
            errorCount: 3.5
        }
    }
];


function createServiceGraphStubStore(promiseState) {
    const store = ServiceGraphStore;
    store.promiseState = promiseState;
    store.graphs = [];
    sinon.stub(store, 'fetchServiceGraph', () => {
        store.graphs = [edges, edges];
    });
    return store;
}

describe('<ServiceGraph />', () => {
    it('should render the serviceGraph panel`', () => {
        const wrapper = shallow(<ServiceGraph history={stubHistory}/>);

        expect(wrapper.find('.service-graph-panel')).to.have.length(1);
    });
});

describe('<ServiceGraphContainer />', () => {
    it('should show as loading during a pending graph promise`', () => {
        const stubStore = createServiceGraphStubStore(pendingPromise);
        const wrapper = shallow(<ServiceGraphContainer graphStore={stubStore} history={stubHistory} search={stubSearch}/>);

        expect(wrapper.find('.serviceGraph__loading')).to.have.length(1);
        ServiceGraphStore.fetchServiceGraph.restore();
    });

    it('should show as error after a rejected graph promise`', () => {
        const stubStore = createServiceGraphStubStore(rejectedPromise);
        const wrapper = shallow(<ServiceGraphContainer graphStore={stubStore} history={stubHistory} search={stubSearch}/>);

        expect(wrapper.find('Error')).to.have.length(1);
        ServiceGraphStore.fetchServiceGraph.restore();
    });

    it('should render the serviceGraph container and set up the tabs`', () => {
        const stubStore = createServiceGraphStubStore(fulfilledPromise);
        const wrapper = shallow(<ServiceGraphContainer graphStore={stubStore} history={stubHistory} search={stubSearch}/>);

        expect(wrapper.find('.serviceGraph__tab-link')).to.have.length(2);
        ServiceGraphStore.fetchServiceGraph.restore();
    });

    it('tabs for multiple graphs should be selectable`', () => {
        const stubStore = createServiceGraphStubStore(fulfilledPromise);
        const wrapper = shallow(<ServiceGraphContainer graphStore={stubStore} search={stubSearch}/>);
        wrapper.find('.serviceGraph__tab-link').last().simulate('click');
        expect((wrapper.find('li').last()).hasClass('active')).to.equal(true);
        ServiceGraphStore.fetchServiceGraph.restore();
    });

    it('should not show tabs for roots if service name is specified`', () => {
        const stubStore = createServiceGraphStubStore(fulfilledPromise);
        const serviceName = 'baratheon-service';

        const wrapper = shallow(<ServiceGraphContainer graphStore={stubStore} search={stubSearch} history={stubHistory} serviceName={serviceName}/>);
        expect(wrapper.find('.serviceGraph__tab-link').exists()).to.equal(false);

        ServiceGraphStore.fetchServiceGraph.restore();
    });
});

describe('<ServiceGraphResults />', () => {
    it('should render the ServiceGraphResults panel`', () => {
        const wrapper = shallow(<ServiceGraphResults history={stubHistory} search={stubSearch} serviceGraph={stubGraph}/>);

        expect(wrapper.find('.serviceGraph__panel')).to.have.length(1);
    });
});
