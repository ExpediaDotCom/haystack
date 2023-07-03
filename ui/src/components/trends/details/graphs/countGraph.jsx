/* eslint-disable no-return-assign */
/* eslint-disable react/no-this-in-sfc */
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

// eslint-disable-next-line no-unused-vars
import * as zoom from 'chartjs-plugin-zoom';

import formatters from '../../../../utils/formatters';
import options from './options';
import MissingTrendGraph from './missingTrend';

const backgroundColorTotal = [['rgba(54, 162, 235, 0.2']];
const borderColorTotal = [['rgba(54, 162, 235, 1)']];

const backgroundColorSuccess = [['rgba(75, 192, 192, 0.2']];
const borderColorSuccess = [['rgba(75, 192, 192, 1)']];

const backgroundColorFailure = [['rgba(229, 28, 35, 0.2)']];
const borderColorFailure = [['rgba(229, 28, 35, 1)']];

const countChartOptions = _.cloneDeep(options);

countChartOptions.scales.yAxes = [
    {
        display: true,
        ticks: {
            callback(value) {
                const formattedValue = formatters.toNumberString(value);
                if (formattedValue.length < 8) {
                    return `${' '.repeat(8 - formattedValue.length)}${formattedValue}`;
                }
                return formattedValue;
            }
        }
    }
];

const CountGraph = ({setShowResetZoom, countPoints, successPoints, failurePoints, xAxesTicks, setXAxesTicks}) => {
    const totalData = countPoints.map((point) => ({x: new Date(point.timestamp), y: point.value || 0}));
    const successData = successPoints.map((point) => ({x: new Date(point.timestamp), y: point.value || 0}));
    const failureData = failurePoints.map((point) => ({x: new Date(point.timestamp), y: point.value || 0}));

    if (!totalData.length && !successData && !failureData) {
        return <MissingTrendGraph title="Count" />;
    }

    countChartOptions.scales.xAxes = [
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

    countChartOptions.zoom.onZoom = ({chart}) => {
        setXAxesTicks({
            min: new Date(chart.options.scales.xAxes[0].ticks.min),
            max: new Date(chart.options.scales.xAxes[0].ticks.max)
        });
        setShowResetZoom(true);
    };

    const chartData = {
        datasets: [
            {
                label: 'Failure  ',
                data: failureData,
                backgroundColor: backgroundColorFailure,
                borderColor: borderColorFailure,
                borderWidth: 1,
                pointRadius: 1,
                pointHoverRadius: 3
            },
            {
                label: 'Success  ',
                data: successData,
                backgroundColor: backgroundColorSuccess,
                borderColor: borderColorSuccess,
                borderWidth: 1,
                pointRadius: 1,
                pointHoverRadius: 3
            },
            {
                label: 'Total     ',
                data: totalData,
                backgroundColor: backgroundColorTotal,
                borderColor: borderColorTotal,
                borderWidth: 1,
                pointRadius: 1,
                pointHoverRadius: 3
            }
        ]
    };

    return (
        <div className="col-md-12">
            <h5 className="text-center">Count</h5>
            <div className="chart-container">
                <Line data={chartData} options={countChartOptions} type="line" />
            </div>
        </div>
    );
};

CountGraph.propTypes = {
    setShowResetZoom: PropTypes.bool.isRequired,
    countPoints: PropTypes.object.isRequired,
    successPoints: PropTypes.object.isRequired,
    failurePoints: PropTypes.object.isRequired,
    xAxesTicks: PropTypes.object.isRequired,
    setXAxesTicks: PropTypes.object.isRequired
};

export default CountGraph;
