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
import {observer} from 'mobx-react';

import AlertDetailsToolbar from './alertDetailsToolbar';
import AlertSubscriptions from './alertSubscriptions';
import AlertHistory from './alertHistory';
import Loading from '../../common/loading';
import Error from '../../common/error';

@observer
export default class AlertDetails extends React.Component {
    static propTypes = {
        serviceName: PropTypes.string.isRequired,
        operationName: PropTypes.string.isRequired,
        type: PropTypes.string.isRequired,
        interval: PropTypes.string.isRequired,
        alertDetailsStore: PropTypes.object.isRequired
    };

    static historyWindow = 86400000;

    componentDidMount() {
        this.props.alertDetailsStore.fetchAlertSubscriptions(this.props.serviceName, this.props.operationName, this.props.type, this.props.interval);
        this.props.alertDetailsStore.fetchAlertHistory(this.props.serviceName, this.props.operationName, this.props.type, Date.now() - AlertDetails.historyWindow, this.props.interval);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.type !== this.props.type) {
            this.props.alertDetailsStore.fetchAlertSubscriptions(nextProps.serviceName, nextProps.operationName, nextProps.type, nextProps.interval);
            this.props.alertDetailsStore.fetchAlertHistory(nextProps.serviceName, nextProps.operationName, nextProps.type, Date.now() - AlertDetails.historyWindow, nextProps.interval);
        }
    }

    render() {
        const enableAlertSubscriptions = window.haystackUiConfig.enableAlertSubscriptions;

        return (
            <section className="table-row-details">
                <div className="alert-details-container">
                    <div className="clearfix alert-details-container_header">
                        <AlertDetailsToolbar serviceName={this.props.serviceName} operationName={this.props.operationName} interval={this.props.interval} />
                    </div>
                    <div className="row">
                        {
                            this.props.alertDetailsStore.historyPromiseState && this.props.alertDetailsStore.historyPromiseState.case({
                                pending: () => <Loading />,
                                rejected: () => <Error />,
                                fulfilled: () => (<AlertHistory
                                    operationName={this.props.operationName}
                                    serviceName={this.props.serviceName}
                                    type={this.props.type}
                                    alertDetailsStore={this.props.alertDetailsStore}
                                    historyWindow={AlertDetails.historyWindow}
                                    interval={this.props.interval}
                                />)
                            })
                        }
                        {
                            enableAlertSubscriptions && this.props.alertDetailsStore.subscriptionsPromiseState && this.props.alertDetailsStore.subscriptionsPromiseState.case({
                                pending: () => <Loading />,
                                rejected: () => <Error />,
                                fulfilled: () => (<AlertSubscriptions
                                    operationName={this.props.operationName}
                                    serviceName={this.props.serviceName}
                                    type={this.props.type}
                                    alertDetailsStore={this.props.alertDetailsStore}
                                    interval={this.props.interval}
                                />)
                            })
                        }
                    </div>
                </div>
        </section>
        );
    }
}
