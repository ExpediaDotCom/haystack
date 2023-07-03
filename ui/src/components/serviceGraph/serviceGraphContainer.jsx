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
import {observer} from 'mobx-react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import Loading from '../common/loading';
import ServiceGraphResults from './serviceGraphResults';
import Error from '../common/error';
import Graph from './util/graph';
import timeWindow from '../../utils/timeWindow';

@observer
export default class ServiceGraphContainer extends React.Component {
    static propTypes = {
        search: PropTypes.object.isRequired,
        graphStore: PropTypes.object.isRequired,
        history: PropTypes.object.isRequired,
        serviceName: PropTypes.string
    };

    static defaultProps = {
        serviceName: undefined
    };

    static timePresetOptions = window.haystackUiConfig.tracesTimePresetOptions;

    /**
     *
     * @param graph
     * @returns {*}
     * Find the root node for the graph tab. For this we need to find all the nodes with no incoming edges. From that
     * list we will pick the node with the largest outgoing traffic.ser
     */
    static findRootNode(graph) {
        const dest = _.map(graph, (edge) => edge.destination.name);
        let rootNodes = [];
        _.forEach(graph, (edge) => {
            if (!_.includes(dest, edge.source.name)) {
                rootNodes.push(edge.source.name);
            }
        });
        // No node found with outgoing connections only. Find the node with the most outgoing traffic as a root node.
        if (_.isEmpty(rootNodes)) {
            rootNodes = dest;
        }

        const uniqRoots = _.uniq(rootNodes);
        const sortedRoots = _.sortBy(uniqRoots, (node) => {
            const outgoingEdges = _.filter(graph, (edge) => edge.source.name === node);
            return _.reduce(outgoingEdges, (result, val) => val.stats.count + result, 0);
        });
        return _.last(sortedRoots);
    }

    constructor(props) {
        super(props);

        this.state = {
            tabSelected: 1
        };
        this.toggleTab = this.toggleTab.bind(this);
    }

    componentDidMount() {
        this.fetchServiceGraphFromStore();
    }

    toggleTab(tabIndex) {
        this.setState({tabSelected: tabIndex});
        this.fetchServiceGraphFromStore();
    }

    fetchServiceGraphFromStore() {
        const time = this.props.search.time;
        const isCustomTimeRange = !!(time && time.from && time.to);
        let activeWindow;

        if (isCustomTimeRange) {
            activeWindow = timeWindow.toCustomTimeRange(time.from, time.to);
        } else {
            activeWindow =
                time && time.preset
                    ? ServiceGraphContainer.timePresetOptions.find((preset) => preset.shortName === time.preset)
                    : timeWindow.defaultPreset;
        }

        const activeWindowTimeRange = timeWindow.toTimeRange(activeWindow.value);
        const filterQuery = {
            from: activeWindowTimeRange.from,
            to: activeWindowTimeRange.until
        };
        this.props.graphStore.fetchServiceGraph(filterQuery);
    }

    render() {
        return (
            <section>
                <div className="clearfix" id="service-graph">
                    {!this.props.serviceName && this.props.graphStore.graphs && (
                        <div className="serviceGraph__tabs pull-right">
                            <ul className="nav nav-tabs">
                                {this.props.graphStore.graphs.map((graph, index) => (
                                    <li className={this.state.tabSelected === index + 1 ? 'active ' : ''} key={index.toString()}>
                                        <a role="button" className="serviceGraph__tab-link" tabIndex="-1" onClick={() => this.toggleTab(index + 1)}>
                                            {ServiceGraphContainer.findRootNode(graph)}
                                        </a>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>
                <div>
                    {this.props.graphStore.promiseState &&
                        this.props.graphStore.promiseState.case({
                            pending: () => <Loading className="serviceGraph__loading" />,
                            rejected: () => <Error />,
                            fulfilled: () =>
                                this.props.graphStore.graphs && this.props.graphStore.graphs.length ? (
                                    <FilteredServiceGraphResults
                                        graphs={this.props.graphStore.graphs}
                                        serviceName={this.props.serviceName}
                                        tabSelected={this.state.tabSelected}
                                        history={this.props.history}
                                        search={this.props.search}
                                    />
                                ) : (
                                    <Error errorMessage="No edges found" />
                                )
                        })}
                </div>
            </section>
        );
    }
}

function FilteredServiceGraphResults(props) {
    if (!props.serviceName) {
        return <ServiceGraphResults serviceGraph={props.graphs[props.tabSelected - 1]} history={props.history} search={props.search} />;
    }
    const result = _.find(props.graphs, (g) => _.includes(Graph.buildGraph(g).allNodes(), props.serviceName));
    if (typeof result !== 'undefined') {
        const filtered = _.filter(result, (edge) => edge.source.name === props.serviceName || edge.destination.name === props.serviceName);
        return <ServiceGraphResults serviceGraph={filtered} history={props.history} search={props.search} />;
    }
    return <Error errorMessage="Service Graph data not found for the given time frame." />;
}

FilteredServiceGraphResults.propTypes = {
    graphs: PropTypes.object.isRequired,
    serviceName: PropTypes.string,
    tabSelected: PropTypes.number,
    history: PropTypes.object.isRequired,
    search: PropTypes.object.isRequired
};

FilteredServiceGraphResults.defaultProps = {
    serviceName: undefined,
    tabSelected: 0
};
