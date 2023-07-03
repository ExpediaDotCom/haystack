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

import React, {Component} from 'react';
import {observer} from 'mobx-react';
import {makeWidthFlexible, Treemap} from 'react-vis';
import PropTypes from 'prop-types';
import Select from 'react-select';
import Error from '../common/error';
import Loading from '../common/loading';
import './servicePerformance.less';

const colorPercentRanges = [0, 50, 75, 90, 95, 98, 99, 99.9, 100];

const colors = ['#8D2827', '#B34D4B', '#EC8B83', '#F6C6B5', '#f5ffd3', '#D1E6CC', '#A5CEB9', '#76B6A3', '#4B9D93'];

const colorScale = colors.map((color, index) => `${color} ${colorPercentRanges[index]}%`).join(',');

@observer
export default class ServicePerformance extends Component {
    static propTypes = {
        history: PropTypes.object.isRequired,
        servicePerfStore: PropTypes.object.isRequired,
        servicePerfStats: PropTypes.object.isRequired
    };

    static mapToColor(percent) {
        return colors[colorPercentRanges.indexOf(colorPercentRanges.find((x) => x >= percent))];
    }

    static createTreemapData(servicePerfStats) {
        const serviceData = servicePerfStats.map((servicePerf) => ({
            ...servicePerf,
            title: (
                <div>
                    <div className="rv-treemap__leaf__title">{servicePerf.serviceName}</div>
                </div>
            ),
            totalCountValue: Math.log(servicePerf.totalCount || 1),
            failureCountValue: Math.log(servicePerf.failureCount || 1),
            successPercentColor: servicePerf.successPercent === null ? '#eee' : ServicePerformance.mapToColor(servicePerf.successPercent)
        }));

        return {children: serviceData, successPercentColor: '#fff'};
    }

    constructor(props) {
        super(props);
        const treemapData = ServicePerformance.createTreemapData(props.servicePerfStats);
        this.state = {
            area: 'totalCount',
            color: 'successPercent',
            hoveredNode: false,
            treemapData,
            initData: treemapData
        };
        this.handleAreaParamChange = this.handleAreaParamChange.bind(this);
        this.handleColorParamChange = this.handleColorParamChange.bind(this);
        this.handleNodeClick = this.handleNodeClick.bind(this);
        this.handleTreemapDataFilter = this.handleTreemapDataFilter.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        const treemapData = ServicePerformance.createTreemapData(nextProps.servicePerfStats);
        this.setState({
            treemapData,
            initData: treemapData
        });
    }

    handleAreaParamChange(event) {
        this.setState({area: event.value});
    }

    handleColorParamChange(event) {
        this.setState({color: event.value});
    }

    handleNodeClick() {
        if (this.state.hoveredNode) {
            this.props.history.push(`/search?serviceName=${encodeURIComponent(this.state.hoveredNode)}&tabId=trends`);
        }
    }

    handleTreemapDataFilter(event) {
        const initData = this.state.initData.children.slice(0, this.state.treemapData.length);
        const filteredData = initData.filter((serviceData) => serviceData.serviceName.toLowerCase().includes(event.target.value.toLowerCase()));
        if (filteredData.length !== 0) {
            this.setState({
                treemapData: {children: filteredData, successPercentColor: '#fff'}
            });
        } else {
            this.setState({
                treemapData: {title: 'No Matching Service Found', totalCountValue: 1, successPercentColor: '#fff'}
            });
        }
    }

    render() {
        const areaOptions = [{value: 'failureCount', label: 'Failure Count'}, {value: 'totalCount', label: 'Request Count'}];

        const colorOptions = [{value: 'successPercent', label: 'Success Percent'}];

        const ResponsiveTreemap = makeWidthFlexible(Treemap);

        return (
            <section className="container servicePerformance">
                <div className="servicePerformance__header row clearfix">
                    <div className="servicePerformance__header-title col-md-4">Service Performance</div>
                    <div className="servicePerformance__search col-md-4">
                        <input
                            type="text"
                            className="servicePerformance__searchBox"
                            placeholder={'Filter Services...'}
                            onChange={this.handleTreemapDataFilter}
                        />
                        <span className="ti-search" />
                    </div>
                    <div className="clearfix col-md-4">
                        <div className="servicePerformance__param-selector pull-right">
                            <div className="text-right">Color </div>
                            <Select options={colorOptions} onChange={this.handleColorParamChange} value={this.state.color} clearable={false} />
                        </div>
                        <div className="servicePerformance__param-selector pull-right">
                            <div className="text-right">Area </div>
                            <Select options={areaOptions} onChange={this.handleAreaParamChange} clearable={false} value={this.state.area} />
                        </div>
                    </div>
                </div>
                {this.props.servicePerfStore.promiseState &&
                    this.props.servicePerfStore.promiseState.case({
                        empty: () => <Loading />,
                        pending: () => <Loading />,
                        rejected: () => <Error />,
                        fulfilled: () =>
                            this.props.servicePerfStore.promiseState ? (
                                <ResponsiveTreemap
                                    animation
                                    height={600}
                                    data={this.state.treemapData}
                                    renderMode={'DOM'}
                                    padding={3}
                                    margin={5}
                                    mode={'circlePack'}
                                    colorType={'literal'}
                                    getSize={(d) => d[`${this.state.area}Value`]}
                                    getColor={(d) => d[`${this.state.color}Color`]}
                                    sortFunction={(a, b) => b.value - a.value}
                                    onLeafMouseOver={(nodeData) =>
                                        nodeData.data.serviceName && this.setState({hoveredNode: nodeData.data.serviceName})
                                    }
                                    onLeafMouseOut={() => this.setState({hoveredNode: false})}
                                    onLeafClick={this.handleNodeClick}
                                />
                            ) : (
                                <Error errorMessage="There was a problem displaying the service performance graph. Please try again later." />
                            )
                    })}
                <section>
                    <div className="pull-right clearfix">
                        <div className="pull-left">0%</div>
                        <div
                            className="pull-left"
                            style={{
                                background: `linear-gradient(to right, ${colorScale})`,
                                height: '10px',
                                width: '200px',
                                margin: '6px'
                            }}
                        />
                        <div className="pull-left">100%</div>
                    </div>
                </section>
            </section>
        );
    }
}
