/* eslint-disable react/prefer-stateless-function */
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

import React, {useState} from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';

import timeWindow from '../../../../utils/timeWindow';
import ServiceOperationTrendRow from './serviceOperationTrendRow';
import {observer} from 'mobx-react';

const TrendsTab = observer(({timelineSpans}) => {
    const [from, setFrom] = useState(Date.now() - (60 * 60 * 1000));
    const [until, setUntil] = useState(Date.now());
    const [selectedIndex, setSelectedIndex] = useState(timeWindow.presets.indexOf(timeWindow.defaultPreset));

    const handleTimeChange = (event) => {
        const newSelectedIndex = event.target.value;
        const selectedWindow = timeWindow.presets[newSelectedIndex];
        const selectedTimeRange = timeWindow.toTimeRange(selectedWindow.value);

        setFrom(selectedTimeRange.from);
        setUntil(selectedTimeRange.until);
        setSelectedIndex(newSelectedIndex);
    };

    const selectedPreset = timeWindow.presets[selectedIndex];
    const granularity = timeWindow.getLowerGranularity(selectedPreset.value).value;

    const serviceOperationList = _.uniqWith(timelineSpans.map(span => ({
            serviceName: span.serviceName,
            operationName: span.operationName
        })),
        _.isEqual);

    return (
        <article>
            <div className="text-right">
                <span>Trace trends for </span>
                <select className="time-range-selector" value={selectedIndex} onChange={handleTimeChange}>
                    {timeWindow.presets.map((window, index) => (
                        <option
                            key={window.longName}
                            value={index}
                        >{window.isCustomTimeRange ? '' : 'last'} {window.longName}</option>))}
                </select>
            </div>
            <table className="trace-trend-table">
                <thead className="trace-trend-table_header">
                <tr>
                    <th width="60" className="trace-trend-table_cell">Operation</th>
                    <th width="20" className="trace-trend-table_cell text-right">Count</th>
                    <th width="20" className="trace-trend-table_cell text-right">Duration</th>
                    <th width="20" className="trace-trend-table_cell text-right">Success %</th>
                </tr>
                </thead>
                <tbody>
                {
                    serviceOperationList.map(serviceOp => (
                        <ServiceOperationTrendRow
                            key={Math.random()}
                            serviceName={serviceOp.serviceName}
                            operationName={serviceOp.operationName}
                            granularity={granularity}
                            from={from}
                            until={until}
                        />
                    ))
                }
                </tbody>
            </table>
        </article>
    );
});

TrendsTab.propTypes = {
    timelineSpans: PropTypes.array.isRequired
};

export default TrendsTab;
