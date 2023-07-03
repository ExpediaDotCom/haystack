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

import React from 'react';
import {shallow} from 'enzyme';
import {expect} from 'chai';

import SearchInsights from '../../../../../src/components/universalSearch/tabs/serviceInsights';

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

const stubStore = {};

describe('<ServiceInsights />', () => {
    it('should render the serviceInsights panel', (done) => {
        const wrapper = shallow(<SearchInsights location={stubLocation} history={stubHistory} store={stubStore} />);
        setTimeout(() => {
            expect(wrapper.find('section')).to.have.length(1);
            expect(!!wrapper.state('ServiceInsightsView')).to.equal(true);
            done();
        }, 50);
    });
});
