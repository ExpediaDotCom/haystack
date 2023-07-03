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
import sinon from 'sinon';
import {shallow} from 'enzyme';

import Lines from '../../../../../src/components/serviceInsights/serviceInsightsGraph/lines';

describe('<Lines/>', () => {
    it('should render the expected elements', () => {
        const edges = [{link: {tps: 20}}];
        const wrapper = shallow(<Lines edges={edges} onHover={() => {}} onLeave={() => {}} />);

        expect(wrapper.find('.lines')).to.have.lengthOf(1);
        expect(wrapper.find('.line')).to.have.lengthOf(1);
        expect(wrapper.find('.line.violation')).to.have.lengthOf(0);
    });

    it('should render line with violation class if invalid cycles are detected', () => {
        const edges = [
            {
                link: {tps: 1, invalidCycleDetected: true},
                points: [{x: 5, y: 0}, {x: 1, y: 0}] // backwards line
            }
        ];
        const wrapper = shallow(<Lines edges={edges} onHover={() => {}} onLeave={() => {}} />);

        expect(wrapper.find('.line.violation')).to.have.lengthOf(1);
    });

    it('should render with high opacity on edges with high tps', () => {
        const edges = [{link: {count: 100}}];
        const wrapper = shallow(<Lines edges={edges} onHover={() => {}} onLeave={() => {}} tracesConsidered={100} />);
        expect(wrapper.find('.line').prop('strokeOpacity')).to.greaterThan(0.9);
    });

    it('should render with low opacity on edges with low tps', () => {
        const edges = [{link: {count: 1}}];
        const wrapper = shallow(<Lines edges={edges} onHover={() => {}} onLeave={() => {}} tracesConsidered={100} />);
        expect(wrapper.find('.line').prop('strokeOpacity')).to.equal(0.2);
    });

    it('should render with a default opacity of `0.2` on edges with no tps', () => {
        const edges = [{link: {}}];
        const wrapper = shallow(<Lines edges={edges} onHover={() => {}} onLeave={() => {}} tracesConsidered={100} />);
        expect(wrapper.find('.line').prop('strokeOpacity')).to.equal(0.2);
    });

    it('should render path element with curve data', () => {
        const edges = [
            {
                points: [{x: 0, y: 0}, {x: 5, y: 2}, {x: 10, y: 4}],
                link: {tps: 20}
            }
        ];
        const wrapper = shallow(<Lines edges={edges} onHover={() => {}} onLeave={() => {}} tracesConsidered={100} />);
        expect(
            wrapper
                .find('.line')
                .prop('d')
                .toString()
                .trim()
        ).to.equal('M 0 0 S 3.3333333333333335 1.3333333333333333 5 2 S 8.333333333333334 3.333333333333333 10 4');
    });

    it('should call onHover event with x, y, and link data', () => {
        const hoverStub = sinon.stub();
        const edges = [{link: {tps: 1}}];
        const wrapper = shallow(<Lines edges={edges} onHover={hoverStub} onLeave={() => {}} tracesConsidered={100} />);

        wrapper.find('.line').simulate('mouseover', {clientX: 4, clientY: 5});

        sinon.assert.calledWith(hoverStub, {clientX: 4, clientY: 5}, 4, 5, edges[0].link);
    });

    it('should call onLeave event', () => {
        const leaveStub = sinon.stub();
        const edges = [{link: {tps: 1}}];
        const wrapper = shallow(<Lines edges={edges} onHover={() => {}} onLeave={leaveStub} tracesConsidered={100} />);

        wrapper.find('.line').simulate('mouseout');

        sinon.assert.called(leaveStub);
    });
});
