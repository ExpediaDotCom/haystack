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

import Label from '../../../../../src/components/serviceInsights/serviceInsightsGraph/label';

describe('<Label/>', () => {
    it('should render the expected elements', () => {
        const wrapper = shallow(<Label text="foo" x={0} y={0} />);

        expect(wrapper.find('.service-name')).to.have.lengthOf(1);
        expect(wrapper.find('.service-name-outline')).to.have.lengthOf(1);
        expect(wrapper.find('.service-name-main')).to.have.lengthOf(1);
    });

    it('should render with a transformation when x, y values are supplied', () => {
        const wrapper = shallow(<Label text="foo" x={10} y={20} />);

        expect(wrapper.find('.service-name').prop('transform')).to.equal('translate(10, 20)');
    });

    it('should render the full text on short strings', () => {
        const wrapper = shallow(<Label text="short-string" x={0} y={0} />);

        expect(wrapper.find('.service-name-outline').text()).to.equal('short-string');
        expect(wrapper.find('.service-name-main').text()).to.equal('short-string');
    });

    it('should render ellipsified text on long strings', () => {
        const wrapper = shallow(<Label text="example-long-string-for-testing" x={0} y={0} />);

        expect(wrapper.find('.service-name-outline').text()).to.equal('example-long-string-for...');
        expect(wrapper.find('.service-name-main').text()).to.equal('example-long-string-for...');
    });

    it('should render un-ellipsified text on mouseover', () => {
        const wrapper = shallow(<Label text="example-long-string-for-testing" x={0} y={0} />);

        wrapper.find('.service-name').simulate('mouseover');

        expect(wrapper.find('.service-name-outline').text()).to.equal('example-long-string-for-testing');
        expect(wrapper.find('.service-name-main').text()).to.equal('example-long-string-for-testing');
    });

    it('should render ellipsified text on mouseout', () => {
        const wrapper = shallow(<Label text="example-long-string-for-testing" x={0} y={0} />);

        wrapper.find('.service-name').simulate('mouseover');

        expect(wrapper.find('.service-name-outline').text()).to.equal('example-long-string-for-testing');
        expect(wrapper.find('.service-name-main').text()).to.equal('example-long-string-for-testing');

        wrapper.find('.service-name').simulate('mouseout');

        expect(wrapper.find('.service-name-outline').text()).to.equal('example-long-string-for...');
        expect(wrapper.find('.service-name-main').text()).to.equal('example-long-string-for...');
    });
});
