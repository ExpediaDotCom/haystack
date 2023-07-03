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
import Clipboard from 'react-copy-to-clipboard';
import {Link} from 'react-router-dom';
import timeWindow from '../../../utils/timeWindow';
import metricGranularity from '../utils/metricGranularity';
import linkBuilder from '../../../utils/linkBuilder';

import './trendDetailsToolbar.less';
import TrendTimeRangePicker from '../../common/timeRangePicker';

const refreshInterval = window.haystackUiConfig && window.haystackUiConfig.refreshInterval;
const tracesEnabled = window.haystackUiConfig.subsystems.includes('traces');

export default class TrendDetailsToolbar extends React.Component {
    static propTypes = {
        serviceName: PropTypes.string.isRequired,
        trendsStore: PropTypes.object.isRequired,
        opName: PropTypes.string,
        statsType: PropTypes.string,
        serviceSummary: PropTypes.bool.isRequired,
        interval: PropTypes.oneOfType([PropTypes.oneOf([null]), PropTypes.string]).isRequired
    };

    static defaultProps = {
        opName: null,
        statsType: null
    };

    static getActiveTimeWindow(from, until, isCustomTimeRange) {
        let activeTimeWindow;
        if (isCustomTimeRange) {
            activeTimeWindow = timeWindow.toCustomTimeRange(from, until);
        } else {
            activeTimeWindow = timeWindow.findMatchingPreset(until - from);
            const timeRange = timeWindow.toTimeRange(activeTimeWindow.value);
            activeTimeWindow.from = timeRange.from;
            activeTimeWindow.until = timeRange.until;
        }
        return activeTimeWindow;
    }

    constructor(props) {
        super(props);

        this.setWrapperRef = this.setWrapperRef.bind(this);
        this.hideTimePicker = this.hideTimePicker.bind(this);
        this.showTimePicker = this.showTimePicker.bind(this);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
        this.handlePresetSelection = this.handlePresetSelection.bind(this);
        this.fetchTrends = this.fetchTrends.bind(this);
        this.toggleGranularityDropdown = this.toggleGranularityDropdown.bind(this);
        this.updateGranularity = this.updateGranularity.bind(this);
        this.handleCopy = this.handleCopy.bind(this);
        this.customTimeRangeChangeCallback = this.customTimeRangeChangeCallback.bind(this);
        this.setClipboardText = this.setClipboardText.bind(this);
        this.refreshTrends = this.refreshTrends.bind(this);
        this.enableAutoRefresh = this.enableAutoRefresh.bind(this);
        this.disableAutoRefresh = this.disableAutoRefresh.bind(this);

        const {from, until, isCustomTimeRange} = props.trendsStore.statsQuery;

        const activeWindow = TrendDetailsToolbar.getActiveTimeWindow(from, until, isCustomTimeRange);
        const granularityFromSearch = metricGranularity.options.find((option) => option.longName === this.props.interval);

        this.state = {
            activeWindow,
            activeGranularity: granularityFromSearch || timeWindow.getHigherGranularity(activeWindow.value),
            granularityDropdownOpen: false,
            clipboardText: this.setClipboardText(activeWindow),
            showCustomTimeRangePicker: false,
            autoRefreshEnabled: false,
            autoRefreshTimer: null,
            countdownTimer: null
        };
    }

    componentDidMount() {
        this.fetchTrends(this.state.activeWindow, this.state.activeGranularity);
    }

    componentWillUnmount() {
        this.disableAutoRefresh();
    }

    setWrapperRef(node) {
        this.wrapperRef = node;
    }

    setClipboardText(activeWindow) {
        return linkBuilder.withAbsoluteUrl(
            linkBuilder.universalSearchTrendsLink({
                query_1: {
                    serviceName: this.props.serviceName,
                    operationName: this.props.opName
                },
                interval: this.props.interval,
                time: {
                    from: activeWindow.from || timeWindow.toTimeRange(activeWindow.value).from,
                    to: activeWindow.until || timeWindow.toTimeRange(activeWindow.value).until
                }
            })
        );
    }

    hideTimePicker() {
        document.removeEventListener('mousedown', this.handleOutsideClick);
        this.setState({showCustomTimeRangePicker: false});
    }

    showTimePicker() {
        document.addEventListener('mousedown', this.handleOutsideClick);
        this.setState({showCustomTimeRangePicker: true});
    }

    handleOutsideClick(e) {
        if (this.wrapperRef && !this.wrapperRef.contains(e.target)) {
            this.hideTimePicker();
        }
    }

    handleCopy() {
        this.setState({showCopied: true});
        setTimeout(() => this.setState({showCopied: false}), 2000);
    }

    toggleGranularityDropdown() {
        this.setState({granularityDropdownOpen: !this.state.granularityDropdownOpen});
    }

    updateGranularity(granularity) {
        this.setState({granularityDropdownOpen: false, activeGranularity: granularity});
        this.fetchTrends(this.state.activeWindow, granularity);
    }

    fetchTrends(window, granularity) {
        const query = {
            granularity: granularity.value,
            from: window.from,
            until: window.until
        };

        if (this.props.opName) {
            this.props.trendsStore.fetchTrends(encodeURIComponent(this.props.serviceName), encodeURIComponent(this.props.opName), query);
        } else {
            this.props.trendsStore.fetchTrends(encodeURIComponent(this.props.serviceName), encodeURIComponent(this.props.statsType), query);
        }
    }

    handlePresetSelection(preset) {
        const updatedGranularity = timeWindow.getLowerGranularity(preset.value);
        const timeRange = timeWindow.toTimeRange(preset.value);

        const presetWindow = preset;
        presetWindow.from = timeRange.from;
        presetWindow.until = timeRange.until;
        this.hideTimePicker();
        this.setState({
            clipboardText: this.setClipboardText(preset),
            activeWindow: presetWindow,
            activeGranularity: updatedGranularity
        });

        this.fetchTrends(preset, updatedGranularity);
    }

    customTimeRangeChangeCallback(updatedWindow) {
        const activeWindow = timeWindow.toCustomTimeRange(updatedWindow.from, updatedWindow.to);

        const updatedGranularity = timeWindow.getLowerGranularity(activeWindow.value);
        this.hideTimePicker();
        this.setState({
            clipboardText: this.setClipboardText(activeWindow),
            activeWindow,
            activeGranularity: updatedGranularity
        });

        this.fetchTrends(activeWindow, updatedGranularity);
    }

    refreshTrends() {
        this.fetchTrends(this.state.activeWindow, this.state.activeGranularity);
    }

    enableAutoRefresh() {
        this.setState({
            autoRefreshEnabled: true
        });
        this.setState({
            autoRefreshTimer: new Date(),
            countdownTimer: new Date()
        });
        this.autoRefreshTimerRef = setInterval(() => {
            this.setState({autoRefreshTimer: new Date()});
            this.refreshTrends();
        }, refreshInterval);
        this.countdownTimerRef = setInterval(() => this.setState({countdownTimer: new Date()}), 1000);
    }

    disableAutoRefresh() {
        this.setState({
            autoRefreshEnabled: false
        });
        clearInterval(this.autoRefreshTimerRef);
        clearInterval(this.countdownTimerRef);
        this.setState({
            autoRefreshTimer: null,
            countdownTimer: null
        });
    }

    render() {
        const countDownMiliSec =
            this.state.countdownTimer &&
            this.state.autoRefreshTimer &&
            refreshInterval - (this.state.countdownTimer.getTime() - this.state.autoRefreshTimer.getTime());

        const tracesLink = linkBuilder.universalSearchTracesLink({
            query_1: {
                serviceName: this.props.serviceName,
                operationName: this.props.opName
            },
            time: {
                preset: this.state.activeWindow.shortName
            }
        });

        const PresetOption = ({preset}) => (
            <button
                className={preset === this.state.activeWindow ? 'btn btn-primary' : 'btn btn-default'}
                key={preset.value}
                onClick={() => this.handlePresetSelection(preset)}
            >
                {preset.shortName}
            </button>
        );

        return (
            <div className="trend-details-toolbar clearfix">
                <div className="pull-left trend-details-toolbar__time-range">
                    <div>Time Range</div>
                    <div ref={this.setWrapperRef}>
                        <div className="btn-group btn-group-sm">
                            {timeWindow.presets.map((preset) => (
                                <PresetOption preset={preset} key={preset.shortName} />
                            ))}
                            <button
                                className={this.state.activeWindow.isCustomTimeRange ? 'custom-btn btn btn-primary' : 'custom-btn btn btn-default'}
                                type="button"
                                onClick={this.state.showCustomTimeRangePicker ? this.hideTimePicker : this.showTimePicker}
                            >
                                {this.state.activeWindow.isCustomTimeRange ? this.state.activeWindow.longName : 'custom'}
                            </button>
                        </div>
                        {this.state.showCustomTimeRangePicker ? (
                            <TrendTimeRangePicker
                                customTimeRangeChangeCallback={this.customTimeRangeChangeCallback}
                                from={parseInt(this.state.activeWindow.from, 10)}
                                to={parseInt(this.state.activeWindow.until, 10)}
                            />
                        ) : null}
                    </div>
                </div>
                <div className="pull-left">
                    <div className="">Metric Granularity</div>
                    <div className={this.state.granularityDropdownOpen ? 'dropdown open' : 'dropdown'}>
                        <button className="btn btn-sm btn-default dropdown-toggle" onClick={() => this.toggleGranularityDropdown()}>
                            <span>{this.state.activeGranularity.shortName}</span>
                            <span className="caret" />
                        </button>
                        <ul className="dropdown-menu" aria-labelledby="dropdownMenu1">
                            <li>
                                {metricGranularity.options.map((option) => (
                                    <a
                                        className="granularity-button"
                                        tabIndex={-1}
                                        key={option.shortName}
                                        role="button"
                                        onClick={() => this.updateGranularity(option)}
                                    >
                                        {option.shortName}
                                    </a>
                                ))}
                            </li>
                        </ul>
                    </div>
                </div>
                <div className="pull-left autorefresh-group">
                    <div>Auto Refresh {this.state.autoRefreshEnabled ? `in ${Math.round(countDownMiliSec / 1000)}s` : ''}</div>
                    <div className="btn-group btn-group-sm">
                        <button
                            className={`btn btn-sm btn-${this.state.autoRefreshEnabled ? 'primary' : 'default'}`}
                            onClick={this.state.autoRefreshEnabled ? null : this.enableAutoRefresh}
                        >
                            On
                        </button>
                        <button
                            className={`btn btn-sm btn-${this.state.autoRefreshEnabled ? 'default' : 'primary'}`}
                            onClick={this.state.autoRefreshEnabled ? this.disableAutoRefresh : null}
                        >
                            Off
                        </button>
                    </div>
                </div>
                <div className="pull-right btn-group btn-group-sm">
                    {this.state.showCopied && (
                        <span className="tooltip fade left in" role="tooltip">
                            <span className="tooltip-arrow" />
                            <span className="tooltip-inner">Link Copied!</span>
                        </span>
                    )}
                    {this.props.serviceSummary === false && tracesEnabled && (
                        <Link role="button" className="btn btn-sm btn-default" to={tracesLink}>
                            <span className="ti-align-left" /> See Traces
                        </Link>
                    )}
                    <a role="button" className="btn btn-sm btn-default" target="_blank" href={this.state.clipboardText}>
                        <span className="ti-new-window" /> Open in new tab
                    </a>

                    <Clipboard text={this.state.clipboardText} onCopy={this.handleCopy}>
                        <a role="button" className="btn btn-sm btn-primary">
                            <span className="ti-link" /> Share Trend
                        </a>
                    </Clipboard>
                </div>
            </div>
        );
    }
}
