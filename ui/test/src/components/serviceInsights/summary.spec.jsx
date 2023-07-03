/*
 * Copyright 2019 Expedia Group
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
import {expect} from 'chai';
import {shallow} from 'enzyme';

import Summary from '../../../../src/components/serviceInsights/summary';

describe('<Summary/>', () => {
    it('should render the expected elements', () => {
        const data = {tracesConsidered: 5, traceLimitReached: false};
        const wrapper = shallow(<Summary data={data} />);

        expect(wrapper.find('.header-summary')).to.have.lengthOf(1);
        expect(wrapper.find('.violation-grid')).to.have.lengthOf(0);
    });

    it('should render violations', () => {
        const data = {
            tracesConsidered: 5,
            hasViolations: true,
            traceLimitReached: false,
            violations: {
                'Uninstrumented services': 2
            }
        };
        const wrapper = shallow(<Summary data={data} />);

        expect(wrapper.find('.violation-grid')).to.have.lengthOf(1);
        expect(wrapper.find('.violation-grid').text()).to.contain('Uninstrumented services (2)');
    });

    it('should render trace limit reached', () => {
        const data = {
            tracesConsidered: 5,
            traceLimitReached: true
        };
        const wrapper = shallow(<Summary data={data} />);

        expect(wrapper.find('.header-summary').text()).to.contain('(limit reached)');
    });
});
