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
import {shallow, mount} from 'enzyme';

import Nodes from '../../../../../src/components/serviceInsights/serviceInsightsGraph/nodes';

describe('<Nodes/>', () => {
    it('should render a default service node', () => {
        const nodes = [{x: 10, y: 20, data: {id: 'foo'}}];
        const wrapper = shallow(<Nodes nodes={nodes} onHover={() => {}} onLeave={() => {}} />);

        expect(wrapper.find('.nodes')).to.have.lengthOf(1);
        expect(wrapper.find('.node')).to.have.lengthOf(1);
        expect(wrapper.find('circle')).to.have.lengthOf(1);
        expect(wrapper.find('circle').prop('r')).to.equal(7);
    });

    it('should render a mesh node', () => {
        const nodes = [{x: 10, y: 20, data: {type: 'mesh'}}];
        const wrapper = shallow(<Nodes nodes={nodes} onHover={() => {}} onLeave={() => {}} />);

        expect(wrapper.find('.nodes')).to.have.lengthOf(1);
        expect(wrapper.find('.node')).to.have.lengthOf(1);
        expect(wrapper.find('circle')).to.have.lengthOf(1);
        expect(wrapper.find('circle').prop('r')).to.equal(4);
    });

    it('should render node transformation', () => {
        const nodes = [{x: 10, y: 20, data: {}}];
        const wrapper = shallow(<Nodes nodes={nodes} onHover={() => {}} onLeave={() => {}} />);

        expect(wrapper.find('.node').prop('transform')).to.equal('translate(10, 20)');
    });

    it('should render node with violation class if invalid cycles are detected', () => {
        const nodes = [{x: 10, y: 20, data: {invalidCycleDetected: true}}];
        const wrapper = shallow(<Nodes nodes={nodes} onHover={() => {}} onLeave={() => {}} />);

        expect(wrapper.find('.violation')).to.have.lengthOf(1);
    });

    it('should render node with central class if its the central node', () => {
        const nodes = [{x: 10, y: 20, data: {relationship: 'central'}}];
        const wrapper = shallow(<Nodes nodes={nodes} onHover={() => {}} onLeave={() => {}} />);

        expect(wrapper.find('.central')).to.have.lengthOf(1);
    });

    it('should render a graph-icon for specific node types', () => {
        const nodes = [
            {x: 10, y: 20, data: {type: 'gateway'}},
            {x: 10, y: 20, data: {type: 'database'}},
            {x: 10, y: 20, data: {type: 'uninstrumented'}},
            {x: 10, y: 20, data: {type: 'outbound'}}
        ];
        const wrapper = shallow(<Nodes nodes={nodes} onHover={() => {}} onLeave={() => {}} />);
        expect(wrapper.find('.graph-icon')).to.have.lengthOf(4);
        expect(wrapper.find('.node.icon-base')).to.have.lengthOf(4);
        expect(wrapper.find('image')).to.have.lengthOf(4);
        expect(wrapper.find('circle')).to.have.lengthOf(0);
    });

    it('should build map of node element references based on `id` property', () => {
        const nodes = [{x: 10, y: 20, data: {id: 'foo'}}];
        const wrapper = mount(<Nodes nodes={nodes} onHover={() => {}} onLeave={() => {}} />);
        const instance = wrapper.instance();
        expect(Object.keys(instance.nodeRefs)[0]).to.equal('foo');
    });

    it('should call onHover event with x, y, and node data', () => {
        const hoverStub = sinon.stub();
        const nodes = [{x: 10, y: 20, data: {id: '123'}}];
        const wrapper = shallow(<Nodes nodes={nodes} onHover={hoverStub} onLeave={() => {}} />);

        wrapper.instance().nodeRefs['123'] = {
            getBoundingClientRect: () => ({left: 1, top: 2, width: 640, height: 480})
        };

        wrapper.find('.node').simulate('mouseover', {});

        sinon.assert.calledWith(hoverStub, {}, 641, 242, {id: '123'});
    });

    it('should call onLeave event', () => {
        const leaveStub = sinon.stub();
        const nodes = [{x: 10, y: 20, data: {id: '123'}}];
        const wrapper = shallow(<Nodes nodes={nodes} onHover={() => {}} onLeave={leaveStub} />);

        wrapper.find('.node').simulate('mouseout');

        sinon.assert.called(leaveStub);
    });
});
