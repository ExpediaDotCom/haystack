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

import React, {Component} from 'react';
import PropTypes from 'prop-types';

import {buildGraphLayout, computeGraphSize, computeGraphPosition} from './dataLayout';

import DragGroup from './dragGroup';
import Nodes from './nodes';
import Lines from './lines';
import Labels from './labels';
import Tooltip from './tooltip';

import './serviceInsightsGraph.less';

export default class ServiceInsightsGraph extends Component {
    static propTypes = {
        graphData: PropTypes.object
    };

    static defaultProps = {
        graphData: null
    };

    state = {
        tipPos: {x: 0, y: 0},
        tipVisible: false,
        tipType: '',
        tipData: null
    };

    handleNodeHover = (event, x, y, data) => {
        this.setState({
            tipPos: {x, y},
            tipVisible: true,
            tipType: 'node',
            tipData: data
        });
    };

    handleLineHover = (event, x, y, data) => {
        this.setState({
            tipPos: {x, y},
            tipVisible: true,
            tipType: 'line',
            tipData: data
        });
    };

    handleLeave = () => {
        this.setState({tipVisible: false});
    };

    render() {
        const {
            summary: {tracesConsidered}
        } = this.props.graphData;
        const {nodes, edges} = buildGraphLayout(this.props.graphData);
        const graphSize = computeGraphSize(nodes);
        this.graphPos = computeGraphPosition(graphSize);

        const paddingBtm = 140;
        const paddingRight = 200;
        const minHeight = 360;

        return (
            <div className="service-insights-graph" style={{height: `${graphSize.height + paddingBtm}px`}}>
                <svg
                    className="service-insights-graph_svg"
                    width={graphSize.width + paddingRight}
                    height={Math.max(graphSize.height + paddingBtm, minHeight)}
                >
                    <DragGroup offsetX={this.graphPos.x} offsetY={this.graphPos.y}>
                        <Lines edges={edges} onHover={this.handleLineHover} onLeave={this.handleLeave} tracesConsidered={tracesConsidered} />
                        <Nodes nodes={nodes} onHover={this.handleNodeHover} onLeave={this.handleLeave} />
                        <Labels nodes={nodes} />
                    </DragGroup>
                </svg>

                <Tooltip
                    x={this.state.tipPos.x}
                    y={this.state.tipPos.y}
                    visible={this.state.tipVisible}
                    type={this.state.tipType}
                    data={this.state.tipData}
                />
            </div>
        );
    }
}
