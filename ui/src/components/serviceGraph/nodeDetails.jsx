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
import PropTypes from 'prop-types';
import TrafficTable from './trafficTable';

const NodeDetails = ({incomingEdges, outgoingEdges, tags, time}) => {
    const incomingTrafficEdges = incomingEdges.map((e) => ({
        node: e.source.name,
        count: e.stats.count,
        errorPercent: (e.stats.errorCount * 100) / e.stats.count
    }));

    const outgoingTrafficEdges = outgoingEdges.map((e) => ({
        node: e.destination.name,
        count: e.stats.count,
        errorPercent: (e.stats.errorCount * 100) / e.stats.count
    }));

    return (
        <article>
            <div className="row">
                <section className="col-md-4">
                    <div className="service-graph__info">
                        <span className="service-graph__info-header">Tags</span>
                    </div>
                    <table className="service-graph__info-table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            {Object.keys(tags).length ? (
                                Object.keys(tags).map((tagKey) => (
                                    <tr>
                                        <td>{tagKey}</td>
                                        <td>{tags[tagKey]}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td>NA</td>
                                    <td />
                                </tr>
                            )}
                        </tbody>
                    </table>
                </section>
                <TrafficTable trafficEdges={incomingTrafficEdges} trafficType="Incoming" time={time} />
                <TrafficTable trafficEdges={outgoingTrafficEdges} trafficType="Outgoing" time={time} />
            </div>
        </article>
    );
};

NodeDetails.defaultProps = {
    tags: {}
};

NodeDetails.propTypes = {
    incomingEdges: PropTypes.array.isRequired,
    outgoingEdges: PropTypes.array.isRequired,
    tags: PropTypes.object,
    time: PropTypes.object.isRequired
};
export default NodeDetails;
