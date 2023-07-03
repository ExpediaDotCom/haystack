/*
 * Copyright 2018 Expedia Group
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
import {observer} from 'mobx-react';

import Loading from '../common/loading';
import AlertTabs from './alertTabs';
import AlertsToolbar from './alertsToolbar';
import Error from '../common/error';

@observer
export default class Alerts extends React.Component {
    static propTypes = {
        location: PropTypes.object.isRequired,
        alertsStore: PropTypes.object.isRequired,
        history: PropTypes.object.isRequired,
        defaultPreset: PropTypes.object.isRequired,
        serviceName: PropTypes.string.isRequired,
        interval: PropTypes.string.isRequired
    };

    render() {
        return (
            <section className="alert-results">
                <AlertsToolbar
                    defaultPreset={this.props.defaultPreset}
                    history={this.props.history}
                    alertsStore={this.props.alertsStore}
                    location={this.props.location}
                    serviceName={this.props.serviceName}
                    interval={this.props.interval}
                />
                {this.props.alertsStore.promiseState &&
                    this.props.alertsStore.promiseState.case({
                        pending: () => <Loading />,
                        rejected: () => <Error />,
                        fulfilled: () =>
                            this.props.alertsStore.alerts && this.props.alertsStore.alerts.length ? (
                                <AlertTabs
                                    defaultPreset={this.props.defaultPreset}
                                    history={this.props.history}
                                    alertsStore={this.props.alertsStore}
                                    location={this.props.location}
                                    serviceName={this.props.serviceName}
                                    interval={this.props.interval}
                                />
                            ) : (
                                <Error errorMessage="There was a problem displaying alerts. Please try again later." />
                            )
                    })}
            </section>
        );
    }
}
