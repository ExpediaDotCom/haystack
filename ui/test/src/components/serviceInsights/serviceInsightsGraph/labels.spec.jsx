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

import Labels from '../../../../../src/components/serviceInsights/serviceInsightsGraph/labels';

describe('<Labels/>', () => {
    it('should render the expected elements', () => {
        const wrapper = shallow(<Labels nodes={[{x: 10, y: 20, data: {name: 'foo'}}]} />);

        expect(wrapper.find('.labels')).to.have.lengthOf(1);
        expect(wrapper.find('Label')).to.have.lengthOf(1);
        expect(wrapper.find('Label').prop('text')).to.equal('foo');
        expect(wrapper.find('Label').prop('x')).to.equal(10);
        expect(wrapper.find('Label').prop('y')).to.equal(20);
    });

    it('should not render a label for mesh nodes', () => {
        const wrapper = shallow(<Labels nodes={[{x: 10, y: 20, data: {name: 'foo', type: 'mesh'}}]} />);

        expect(wrapper.find('.labels')).to.have.lengthOf(1);
        expect(wrapper.find('Label')).to.have.lengthOf(0);
    });
});
