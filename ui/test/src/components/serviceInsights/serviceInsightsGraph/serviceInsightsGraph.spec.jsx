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
import sinon from 'sinon';

import * as dataLayout from '../../../../../src/components/serviceInsights/serviceInsightsGraph/dataLayout';

import ServiceInsightsGraph from '../../../../../src/components/serviceInsights/serviceInsightsGraph/serviceInsightsGraph';

describe('<ServiceInsightsGraph/>', () => {
    it('should render the expected elements', () => {
        const graphData = {nodes: [], links: [], summary: {tracesConsidered: 0, traceLimitReached: false}};
        const wrapper = shallow(<ServiceInsightsGraph graphData={graphData} />);

        expect(wrapper.find('.service-insights-graph')).to.have.lengthOf(1);
        expect(wrapper.find('.service-insights-graph_svg')).to.have.lengthOf(1);
        expect(wrapper.find('DragGroup')).to.have.lengthOf(1);
        expect(wrapper.find('Lines')).to.have.lengthOf(1);
        expect(wrapper.find('Nodes')).to.have.lengthOf(1);
        expect(wrapper.find('Labels')).to.have.lengthOf(1);
        expect(wrapper.find('Tooltip')).to.have.lengthOf(1);
    });

    it("should set the svg's width and height", () => {
        dataLayout.computeGraphSize = sinon.stub().returns({width: 640, height: 480});
        const graphData = {nodes: [], links: [], summary: {tracesConsidered: 0, traceLimitReached: false}};
        const wrapper = shallow(<ServiceInsightsGraph graphData={graphData} />);

        expect(wrapper.find('.service-insights-graph_svg').prop('width')).to.equal(840);
        expect(wrapper.find('.service-insights-graph_svg').prop('height')).to.equal(620);
    });

    it("should set the svg's height to no less than minimum", () => {
        dataLayout.computeGraphSize = sinon.stub().returns({width: 640, height: 200});
        const graphData = {nodes: [], links: [], summary: {tracesConsidered: 0, traceLimitReached: false}};
        const wrapper = shallow(<ServiceInsightsGraph graphData={graphData} />);

        expect(wrapper.find('.service-insights-graph_svg').prop('height')).to.equal(360);
    });

    it("should set the DragGroup's offsets based on graphPosition values", () => {
        dataLayout.computeGraphPosition = sinon.stub().returns({x: 100, y: 80});
        const graphData = {nodes: [], links: [], summary: {tracesConsidered: 0, traceLimitReached: false}};
        const wrapper = shallow(<ServiceInsightsGraph graphData={graphData} />);

        expect(wrapper.find('DragGroup').prop('offsetX')).to.equal(100);
        expect(wrapper.find('DragGroup').prop('offsetY')).to.equal(80);
    });

    it("should set the Tooltip's props based on state values", () => {
        const graphData = {nodes: [], links: [], summary: {tracesConsidered: 0, traceLimitReached: false}};
        const wrapper = shallow(<ServiceInsightsGraph graphData={graphData} />);

        wrapper.setState({
            tipPos: {x: 5, y: 6},
            tipVisible: true,
            tipType: 'node',
            tipData: {foo: 'bar'}
        });

        expect(wrapper.find('Tooltip').prop('x')).to.equal(5);
        expect(wrapper.find('Tooltip').prop('y')).to.equal(6);
        expect(wrapper.find('Tooltip').prop('visible')).to.equal(true);
        expect(wrapper.find('Tooltip').prop('type')).to.equal('node');
        expect(wrapper.find('Tooltip').prop('data')).to.deep.equal({foo: 'bar'});
    });

    it('should set state values when onNodeHover is called', () => {
        const graphData = {nodes: [], links: [], summary: {tracesConsidered: 0, traceLimitReached: false}};
        const instance = shallow(<ServiceInsightsGraph graphData={graphData} />).instance();

        instance.handleNodeHover({}, 7, 8, {foo: 'bar'});

        expect(instance.state.tipPos).to.deep.equal({x: 7, y: 8});
        expect(instance.state.tipVisible).to.equal(true);
        expect(instance.state.tipType).to.equal('node');
        expect(instance.state.tipData).to.deep.equal({foo: 'bar'});
    });

    it('should set state values when handleLineHover is called', () => {
        const graphData = {nodes: [], links: [], summary: {tracesConsidered: 0, traceLimitReached: false}};
        const instance = shallow(<ServiceInsightsGraph graphData={graphData} />).instance();

        instance.handleLineHover({}, 9, 10, {bar: 'baz'});

        expect(instance.state.tipPos).to.deep.equal({x: 9, y: 10});
        expect(instance.state.tipVisible).to.equal(true);
        expect(instance.state.tipType).to.equal('line');
        expect(instance.state.tipData).to.deep.equal({bar: 'baz'});
    });

    it('should set tipVisible to false when handleLeave is called', () => {
        const graphData = {nodes: [], links: [], summary: {tracesConsidered: 0, traceLimitReached: false}};
        const instance = shallow(<ServiceInsightsGraph graphData={graphData} />).instance();

        instance.state.tipVisible = true;
        instance.handleLeave();

        expect(instance.state.tipVisible).to.equal(false);
    });
});
