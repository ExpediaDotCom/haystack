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

import React, {useState, useEffect} from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';

import colorMapper from '../../../../utils/serviceColorMapper';
import TrendSparklines from '../../../trends/utils/trendSparklines';
import fetcher from '../../stores/traceTrendFetcher';
import linkBuilder from '../../../../utils/linkBuilder';

// TODO dedupe code and push it to server side
function toSuccessPercentPoints(successCount, failureCount) {
    const successTimestamps = successCount.map(point => point.timestamp);
    const failureTimestamps = failureCount.map(point => point.timestamp);
    const timestamps = _.uniq([...successTimestamps, ...failureTimestamps]);

    return _.compact(timestamps.map((timestamp) => {
        const successItem = _.find(successCount, x => (x.timestamp === timestamp));
        const successVal = (successItem && successItem.value) ? successItem.value : 0;

        const failureItem = _.find(failureCount, x => (x.timestamp === timestamp));
        const failureVal = (failureItem && failureItem.value) ? failureItem.value : 0;

        if (successVal + failureVal) {
            return (100 - ((failureVal / (successVal + failureVal)) * 100));
        }

        return null;
    }));
}

// TODO dedupe code and push it to server side
function toAvgSuccessPercent(successPoints, failurePoints) {
    const successCount = successPoints.reduce(((accumulator, dataPoint) => (accumulator + dataPoint.value)), 0);
    const failureCount = failurePoints.reduce(((accumulator, dataPoint) => (accumulator + dataPoint.value)), 0);

    if (successCount + failureCount === 0) {
        return null;
    }

    return 100 - ((failureCount / (successCount + failureCount)) * 100);
}


function openTrendDetailInNewTab(serviceName, operationName, from, until) {
    const url = linkBuilder.universalSearchTrendsLink({
        query_1: {
            serviceName,
            operationName
        },
        time: {
            from,
            to: until
        }}
    );

    const tab = window.open(url, '_blank');
    tab.focus();
}

const TrendRow = ({serviceName, operationName, from, until, granularity}) => {
    const [trends, setTrends] = useState(null);

    useEffect(() => {
        fetcher.fetchOperationTrends(serviceName, operationName, granularity, from, until)
            .then((result) => {
                setTrends(result);
            });
    }, []);

    const totalCount = trends && trends.count && _.sum(trends.count.map(a => a.value));
    const totalPoints = trends && trends.count && trends.count.map(p => p.value);

    const latestDuration = trends && trends.tp99Duration && trends.tp99Duration.length && trends.tp99Duration[trends.tp99Duration.length - 1].value;
    const durationPoints = trends && trends.tp99Duration && trends.tp99Duration.length && trends.tp99Duration.map(p => p.value);

    const successPercentAvg = trends && toAvgSuccessPercent(trends.successCount, trends.failureCount);
    const successPercentPoints = trends && toSuccessPercentPoints(trends.successCount, trends.failureCount);

    return (
        <tr onClick={() => openTrendDetailInNewTab(serviceName, operationName, from, until)}>
            <td className="trace-trend-table_cell">
                <div className={`service-spans label label-default ${colorMapper.toBackgroundClass(serviceName)}`}>{serviceName}</div>
                <div className="trace-trend-table_op-name">{operationName}</div>
            </td>
            <td className="trace-trend-table_cell">
                {trends && <TrendSparklines.CountSparkline total={totalCount} points={totalPoints} />}
            </td>
            <td className="trace-trend-table_cell">
                {(durationPoints && durationPoints.length) ? <TrendSparklines.DurationSparkline latest={latestDuration} points={durationPoints} /> : null}
            </td>
            <td className="trace-trend-table_cell">
                {trends && <TrendSparklines.SuccessPercentSparkline average={successPercentAvg} points={successPercentPoints} />}
            </td>
        </tr>
    );
};

TrendRow.propTypes = {
    serviceName: PropTypes.string.isRequired,
    operationName: PropTypes.string.isRequired,
    from: PropTypes.number.isRequired,
    until: PropTypes.number.isRequired,
    granularity: PropTypes.number.isRequired
};

export default TrendRow;
