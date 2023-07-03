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

const subsystems = (window.haystackUiConfig && window.haystackUiConfig.subsystems) || [];
const tracesEnabled = subsystems.includes('traces');
const trendsEnabled = subsystems.includes('trends');
const alertsEnabled = subsystems.includes('alerts');
const serviceGraphEnabled = subsystems.includes('serviceGraph');
const servicePerformanceEnabled = window.haystackUiConfig.enableServicePerformance;
const serviceInsightsEnabled = subsystems.includes('serviceInsights');

const Guide = ({searchHistory}) => {
    const historyList = searchHistory.map((searchObject) => (
        <li key={searchObject}><code><a href={`/search?${searchObject}`}>{searchObject.split('&').join(', ')}</a></code></li>
    ));
    const history = historyList.length ?
        (<section>
            <div><b>History</b></div>
            <ul className="usb-suggestions__guide-history-list">
                {historyList}
            </ul>
        </section>)
        : null;

    return (
        <div className="usb-suggestions__guide-wrapper pull-left">
            <section>
                <div><b>How to search</b></div>
                <div>Create a query by specifying key/value pairs in <code>key=value</code> format to search for traces containing tags. One query can have multiple key/value pairs, eg:</div>
                <ul>
                    <li>Traces having traceId xxxx :<code>traceId=xxxx</code></li>
                    <li>Traces containing a span with test-svc service: <code>serviceName=test-svc</code></li>
                    <li>Traces containing an error span with test-svc service: <code>serviceName=test-svc</code> <code>error=true</code></li>
                </ul>
            </section>
            <section>
                <div><b>How tabs show up</b></div>
                <div>
                        <li>If no tag searched:
                            <span className="usb-suggestions__guide-highlight">
                                {serviceGraphEnabled && <><span className="ti-vector usb-suggestions__guide-tab"/><span> Service Graph </span></>}
                                {servicePerformanceEnabled && <><span className="ti-pie-chart usb-suggestions__guide-tab"/><span> Service Performance </span></>}
                            </span>
                        </li>
                        <li>If only serviceName (and/or operationName) searched:
                            <br />
                            <span className="usb-suggestions__guide-highlight">
                                {tracesEnabled && <><span className="ti-align-left usb-suggestions__guide-tab"/><span> Traces </span></>}
                                {trendsEnabled && <><span className="ti-stats-up usb-suggestions__guide-tab"/><span> Trends </span></>}
                                {alertsEnabled && <><span className="ti-bell usb-suggestions__guide-tab"/><span> Alerts </span></>}
                                {serviceGraphEnabled && <><span className="ti-vector usb-suggestions__guide-tab"/><span> Service Graph </span></>}
                                {serviceInsightsEnabled && <><span className="ti-pie-chart usb-suggestions__guide-tab"/><span> Service Insights </span></>}
                            </span>
                        </li>
                        {   tracesEnabled &&
                            <li>Any other combination of tags searched:
                                <span className="usb-suggestions__guide-highlight">
                                <span className="ti-align-left usb-suggestions__guide-tab"/><span> Traces </span>
                            </span>
                            </li>
                        }
                </div>
            </section>
            {history}
        </div>
    );
};

Guide.propTypes = {
    searchHistory: PropTypes.object.isRequired
};

export default Guide;
