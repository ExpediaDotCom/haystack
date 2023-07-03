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
import { shallow } from 'enzyme';
import { expect } from 'chai';
import NoMatch from '../../../../src/components/common/noMatch';

describe('<NoMatch />', () => {
    it('should render the NoMatch panel`', () => {
        const wrapper = shallow(<NoMatch location={{pathname: '/traces/b1e7ef78-8cae-47cb-afaf-9febcf03fd72'}}/>);
        expect(wrapper.find('.nomatch-panel')).to.have.length(1);
    });
});
