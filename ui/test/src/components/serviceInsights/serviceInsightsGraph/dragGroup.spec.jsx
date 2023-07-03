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

import DragGroup from '../../../../../src/components/serviceInsights/serviceInsightsGraph/dragGroup';

describe('<DragGroup/>', () => {
    it('should render the expected elements', () => {
        const wrapper = shallow(
            <DragGroup offsetX={0} offsetY={0}>
                <div className="child" />
            </DragGroup>
        );

        expect(wrapper.find('.drag-group')).to.have.lengthOf(1);
        expect(wrapper.find('.drag-target')).to.have.lengthOf(1);
        expect(wrapper.find('.drag-group > g')).to.have.lengthOf(1);
        expect(wrapper.contains(<div className="child" />)).to.equal(true);
    });

    it('should render with a transformation when offset is supplied', () => {
        const wrapper = shallow(
            <DragGroup offsetX={10} offsetY={20}>
                <div />
            </DragGroup>
        );
        expect(wrapper.find('.drag-group > g').prop('transform')).to.equal('translate(10, 20)');
    });

    it('should set initial drag state on mousedown', () => {
        const wrapper = shallow(
            <DragGroup offsetX={0} offsetY={0}>
                <div />
            </DragGroup>
        );
        wrapper.find('.drag-target').simulate('mousedown', {clientX: 5, clientY: 6});

        expect(wrapper.state('isDragging')).to.equal(true);
        expect(wrapper.state('origin')).to.deep.equal({x: 5, y: 6});
    });

    it('should set initial drag state on mousedown', () => {
        const wrapper = shallow(
            <DragGroup offsetX={0} offsetY={0}>
                <div />
            </DragGroup>
        );
        wrapper.find('.drag-target').simulate('mousedown', {clientX: 5, clientY: 6});

        expect(wrapper.state('isDragging')).to.equal(true);
        expect(wrapper.state('origin')).to.deep.equal({x: 5, y: 6});
    });

    it('should update transformation on mousemove', () => {
        const wrapper = shallow(
            <DragGroup offsetX={0} offsetY={0}>
                <div />
            </DragGroup>
        );
        wrapper.instance().state.isDragging = true;
        wrapper.instance().state.origin = {x: 1, y: 1};
        wrapper.instance().state.last = {x: 100, y: 100};

        wrapper.find('.drag-target').simulate('mousemove', {clientX: 5, clientY: 6});

        expect(wrapper.state('coords')).to.deep.equal({x: 104, y: 105});
        expect(wrapper.find('.drag-group > g').prop('transform')).to.equal('translate(104, 105)');
    });

    it('should end dragging on mouseup', () => {
        const wrapper = shallow(
            <DragGroup offsetX={0} offsetY={0}>
                <div />
            </DragGroup>
        );
        wrapper.instance().state.coords = {x: 8, y: 9};

        wrapper.find('.drag-target').simulate('mouseup');

        expect(wrapper.state('isDragging')).to.equal(false);
        expect(wrapper.state('last')).to.deep.equal({x: 8, y: 9});

        // ensure mouse movement doesn't update `coords` state when finished dragging
        wrapper.find('.drag-target').simulate('mousemove', {clientX: 12, clientY: 14});
        expect(wrapper.state('coords')).to.deep.equal({x: 8, y: 9});
    });
});
