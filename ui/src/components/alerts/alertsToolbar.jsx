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

import timeWindow from '../../utils/timeWindow';
import granularityMetrics from '../trends/utils/metricGranularity';
import {toQuery, toQueryUrlString} from '../../utils/queryParser';
import './alerts';

const refreshInterval = window.haystackUiConfig && window.haystackUiConfig.refreshInterval;

@observer
export default class AlertsToolbar extends React.Component {
    static propTypes = {
        location: PropTypes.object.isRequired,
        serviceName: PropTypes.string.isRequired,
        alertsStore: PropTypes.object.isRequired,
        history: PropTypes.object.isRequired,
        defaultPreset: PropTypes.object.isRequired,
        interval: PropTypes.string.isRequired
    };

    constructor(props) {
        super(props);

        const query = toQuery(this.props.location.search);
        const activeWindow = query.preset ? timeWindow.presets.find((presetItem) => presetItem.shortName === query.preset) : this.props.defaultPreset;
        this.state = {
            interval: this.props.interval,
            options: timeWindow.presets,
            activeWindow,
            autoRefreshTimer: new Date(),
            countdownTimer: new Date(),
            autoRefresh: false
        };

        this.getUnhealthyAlerts = this.getUnhealthyAlerts.bind(this);
        this.handleTimeChange = this.handleTimeChange.bind(this);
        this.startRefresh = this.startRefresh.bind(this);
        this.stopRefresh = this.stopRefresh.bind(this);
        this.toggleAutoRefresh = this.toggleAutoRefresh.bind(this);
        this.handleIntervalChange = this.handleIntervalChange.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        const query = toQuery(nextProps.location.search);
        const activeWindow = query.preset ? timeWindow.presets.find((presetItem) => presetItem.shortName === query.preset) : nextProps.defaultPreset;
        this.setState({activeWindow, autoRefresh: false, interval: nextProps.interval || 'FiveMinute'});
        this.stopRefresh();
    }

    componentWillUnmount() {
        this.stopRefresh();
    }

    getUnhealthyAlerts() {
        let unhealthyAlerts = 0;
        this.props.alertsStore.alerts.forEach((alert) => {
            if (alert.isUnhealthy) {
                unhealthyAlerts += 1;
            }
        });
        return unhealthyAlerts;
    }

    startRefresh() {
        this.setState({
            autoRefreshTimer: new Date(),
            countdownTimer: new Date()
        });
        this.autoRefreshTimerRef = setInterval(() => {
            this.setState({autoRefreshTimer: new Date()});
            this.props.alertsStore.fetchServiceAlerts(this.props.serviceName, this.props.interval, this.state.activeWindow);
        }, refreshInterval);
        this.countdownTimerRef = setInterval(() => this.setState({countdownTimer: new Date()}), 1000);
    }

    stopRefresh() {
        clearInterval(this.autoRefreshTimerRef);
        clearInterval(this.countdownTimerRef);
        this.setState({
            autoRefreshTimer: null,
            countdownTimer: null
        });
    }

    toggleAutoRefresh() {
        this.setState({autoRefresh: !this.state.autoRefresh});
        if (this.state.autoRefresh) {
            this.stopRefresh();
        } else {
            this.startRefresh();
        }
    }

    handleTimeChange(event) {
        const selectedIndex = event.target.value;
        const selectedWindow = this.state.options[selectedIndex];

        this.props.alertsStore.fetchServiceAlerts(this.props.serviceName, this.props.interval, selectedWindow);
        const query = {
            preset: this.state.options[selectedIndex].shortName
        };
        const queryUrl = `?${toQueryUrlString(query)}`;
        this.props.history.push(queryUrl);
        this.setState({activeWindow: selectedWindow});
    }

    handleIntervalChange(event) {
        const query = toQuery(this.props.location.search);
        query.interval = event.target.value;

        const queryUrl = toQueryUrlString(query);
        if (queryUrl !== this.props.location.search) {
            this.props.history.push({
                search: queryUrl
            });
        }
    }

    render() {
        const countDownMiliSec =
            this.state.countdownTimer &&
            this.state.autoRefreshTimer &&
            refreshInterval - (this.state.countdownTimer.getTime() - this.state.autoRefreshTimer.getTime());

        return (
            <header className="alerts-toolbar">
                <div className="pull-right text-right">
                    <div>
                        <div className="box-inline">
                            <span>Metric Interval</span>
                            <select value={this.state.interval} className="form-control alert-interval" onChange={this.handleIntervalChange}>
                                {granularityMetrics.options.map((granularity) => (
                                    <option key={granularity.longName} value={granularity.longName}>
                                        {granularity.shortName}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="box-inline">
                            <span>Auto Refresh {this.state.autoRefresh ? `in ${Math.round(countDownMiliSec / 1000)}s` : ''} </span>
                            <span className="btn-group btn-group-sm">
                                <button
                                    className={`btn btn-sm btn-${this.state.autoRefresh ? 'primary' : 'default'}`}
                                    onClick={this.state.autoRefresh ? null : this.toggleAutoRefresh}
                                >
                                    On
                                </button>
                                <button
                                    className={`btn btn-sm btn-${this.state.autoRefresh ? 'default' : 'primary'}`}
                                    onClick={this.state.autoRefresh ? this.toggleAutoRefresh : null}
                                >
                                    Off
                                </button>
                            </span>
                        </div>
                    </div>
                </div>
            </header>
        );
    }
}
