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
import {Line} from 'react-chartjs-2';
import PropTypes from 'prop-types';
import _ from 'lodash';
import options from './options';
import MissingTrendGraph from './missingTrend';

// eslint-disable-next-line no-unused-vars
import * as zoom from 'chartjs-plugin-zoom';

const backgroundColor = [['rgba(75, 192, 192, 0.2)']];
const borderColor = [['rgba(75, 192, 192, 1)']];
const successChartOptions = _.cloneDeep(options);

successChartOptions.scales.yAxes = [
    {
        display: true,
        ticks: {
            max: 100,
            callback(value) {
                const fixedValue = value.toFixed(3);
                return `${' '.repeat(8 - fixedValue.toString().length)}${fixedValue}`;
            }
        }
    }
];

const SuccessGraph = ({setShowResetZoom, successCount, failureCount, xAxesTicks, setXAxesTicks}) => {
    const successTimestamps = successCount.map((point) => point.timestamp);
    const failureTimestamps = failureCount.map((point) => point.timestamp);
    const timestamps = _.uniq([...successTimestamps, ...failureTimestamps]);

    const data = _.compact(
        timestamps.map((timestamp) => {
            const successItem = _.find(successCount, (x) => x.timestamp === timestamp);
            const successVal = successItem && successItem.value && successItem.value !== null ? successItem.value : 0;

            const failureItem = _.find(failureCount, (x) => x.timestamp === timestamp);
            const failureVal = failureItem && failureItem.value && failureItem.value !== null ? failureItem.value : 0;

            if (successVal + failureVal) {
                return {
                    x: new Date(timestamp),
                    y: (100 - (failureVal / (successVal + failureVal)) * 100).toFixed(3)
                };
            }
            return null;
        })
    );

    if (!data.length) {
        return <MissingTrendGraph title="Success %" />;
    }

    successChartOptions.scales.xAxes = [
        {
            type: 'time',
            ticks: {
                min: xAxesTicks.min,
                max: xAxesTicks.max,
                autoSkip: true,
                autoSkipPadding: 25,
                maxRotation: 0
            }
        }
    ];

    successChartOptions.zoom.onZoom = ({chart}) => {
        setXAxesTicks({
            min: new Date(chart.options.scales.xAxes[0].ticks.min),
            max: new Date(chart.options.scales.xAxes[0].ticks.max)
        });
        setShowResetZoom(true);
    };

    const chartData = {
        datasets: [
            {
                label: 'Success %',
                data,
                backgroundColor,
                borderColor,
                borderWidth: 1,
                pointRadius: 1,
                pointHoverRadius: 3
            }
        ],
        fill: 'end'
    };

    return (
        <div className="col-md-12">
            <h5 className="text-center">Success %</h5>
            <div className="chart-container">
                <Line data={chartData} options={successChartOptions} type="line" />
            </div>
        </div>
    );
};

SuccessGraph.propTypes = {
    setShowResetZoom: PropTypes.bool.isRequired,
    successCount: PropTypes.object.isRequired,
    failureCount: PropTypes.object.isRequired,
    xAxesTicks: PropTypes.object.isRequired,
    setXAxesTicks: PropTypes.object.isRequired
};

export default SuccessGraph;
