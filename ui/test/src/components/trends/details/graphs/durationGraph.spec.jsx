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
import {Line} from 'react-chartjs-2';
import {observable} from 'mobx';
import DurationGraph from '../../../../../../src/components/trends/details/graphs/durationGraph';

describe('<DurationGraph />', () => {
    it('should not transform value as it is already in ms', () => {
        const from = 1000;
        const until = 2000;

        const xAxesTicks = {
            min: from,
            max: until
        };

        const meanPoints = observable.array([{timestamp: 1530844500000, value: 902}]);
        const tp95Points = observable.array([]);
        const tp99Points = observable.array([]);
        const wrapper = shallow(<DurationGraph meanPoints={meanPoints} tp95Points={tp95Points} tp99Points={tp99Points} xAxesTicks={xAxesTicks} />);

        expect(wrapper.find(Line).props().data.datasets[2].data[0].y).to.equal(902);
    });
});
