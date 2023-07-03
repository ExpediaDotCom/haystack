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
import {observer} from 'mobx-react';
import Loading from '../common/loading';
import Error from '../common/error';
import PropTypes from 'prop-types';
import Summary from './summary';
import ServiceInsightsGraph from './serviceInsightsGraph/serviceInsightsGraph';
import './serviceInsights.less';

@observer
export default class ServiceInsights extends Component {
    static propTypes = {
        history: PropTypes.object.isRequired,
        search: PropTypes.object.isRequired,
        store: PropTypes.object.isRequired
    };

    // relationship to the central node is only applicable when a service is specified
    relationshipIsApplicable = () => !!this.props.search.serviceName;

    handleSelectViewFilter = (ev) => {
        const params = new URLSearchParams(this.props.history.location.search);
        params.set('relationship', ev.target.value);
        this.props.history.push(decodeURIComponent(`${this.props.history.location.pathname}?${params}`));
    };

    render() {
        const {store} = this.props;

        return (
            <section className="container serviceInsights">
                {!this.props.store.hasValidSearch && (
                    <p className="select-service-msg">
                        Please search for a serviceName in the global search bar to render a service insight (such as serviceName=example-service).
                    </p>
                )}

                {this.relationshipIsApplicable() && (
                    <div className="service-insights__filter">
                        <span>View:</span>
                        <select value={this.props.search.relationship} onChange={this.handleSelectViewFilter}>
                            <option value="downstream,upstream">Only Downstream &amp; Upstream Dependencies</option>
                            <option value="downstream">Only Downstream Dependencies</option>
                            <option value="upstream">Only Upstream Dependencies</option>
                            <option value="all">All Dependencies</option>
                        </select>
                    </div>
                )}

                {this.props.store.hasValidSearch &&
                    store.promiseState &&
                    store.promiseState.case({
                        pending: () => <Loading className="service-insights__loading" />,
                        rejected: () => <Error />,
                        fulfilled: () => {
                            const data = store.serviceInsights;

                            if (data && data.nodes && data.nodes.length) {
                                return (
                                    <div className="row">
                                        <Summary data={data.summary} />
                                        <ServiceInsightsGraph graphData={data} />
                                    </div>
                                );
                            }

                            return <Error errorMessage="No trace data found" />;
                        }
                    })}
            </section>
        );
    }
}
