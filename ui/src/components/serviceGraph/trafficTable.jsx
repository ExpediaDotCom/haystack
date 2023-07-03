/*
 * Copyright 2017 Expedia, Inc.
 *
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */

import React from 'react';
import PropTypes from 'prop-types';
import formatters from '../../utils/formatters';

export default class TrafficTable extends React.Component {
    static propTypes = {
        trafficType: PropTypes.string.isRequired,
        trafficEdges: PropTypes.array.isRequired,
        time: PropTypes.object.isRequired
    };

    static MAX_ROWS = 7;

    static getErrorLevel(errorPercentage) {
        const ERROR_LEVEL = 10;
        const WARN_LEVEL = 1;
        if (errorPercentage > ERROR_LEVEL) {
            return 'danger';
        } else if (errorPercentage > WARN_LEVEL) {
            return 'warning';
        }
        return 'normal';
    }

    constructor(props) {
        super(props);
        this.state = {isExpanded: false};
        this.toggleShowMore = this.toggleShowMore.bind(this);
    }

    toggleShowMore() {
        this.setState({isExpanded: !this.state.isExpanded});
    }

    render() {
        const {trafficEdges, trafficType, time} = this.props;
        const {isExpanded} = this.state;

        let trafficEdgesList = trafficEdges.sort((a, b) => b.count - a.count);

        let ShowMoreLess = () => null;
        if (trafficEdges.length > TrafficTable.MAX_ROWS) {
            if (isExpanded) {
                ShowMoreLess = () => (
                    <tr>
                        <td>
                            <a role="button" tabIndex={-1} onClick={() => this.toggleShowMore()}>
                                Show Less
                            </a>
                        </td>
                        <td />
                        <td />
                    </tr>
                );
            } else {
                trafficEdgesList = trafficEdgesList.slice(0, TrafficTable.MAX_ROWS);
                ShowMoreLess = () => (
                    <tr>
                        <td>
                            <a role="button" tabIndex={-1} onClick={() => this.toggleShowMore()}>
                                Show all {trafficEdges.length} Services
                            </a>
                        </td>
                        <td />
                        <td />
                    </tr>
                );
            }
        }

        const timeWindowText = time
            ? `(${formatters.toTimeRangeTextFromTimeWindow(time.preset, time.from, time.to)} average)`
            : '(last 1 hour average)';

        return (
            <section className="col-md-4">
                <div className="service-graph__info">
                    <span className="service-graph__info-header">{trafficType} Traffic</span>{' '}
                    <span className="service-graph__info-sub">{timeWindowText}</span>
                </div>
                <table className="service-graph__info-table">
                    <thead>
                        <tr>
                            <th>Service</th>
                            <th className="text-right">Rq/sec</th>
                            <th className="text-right">Error %</th>
                        </tr>
                    </thead>
                    <tbody>
                        {trafficEdgesList && trafficEdgesList.length ? (
                            trafficEdgesList.map((edge) => (
                                <tr key={edge.node}>
                                    <td>{edge.node}</td>
                                    <td className="text-right">{edge.count.toFixed(2)}</td>
                                    <td className={`text-right service-graph__info-error-${TrafficTable.getErrorLevel(edge.errorPercent)}`}>
                                        {edge.errorPercent.toFixed(2)}%
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td>NA</td>
                                <td />
                                <td />
                            </tr>
                        )}
                        <ShowMoreLess />
                    </tbody>
                </table>
            </section>
        );
    }
}
