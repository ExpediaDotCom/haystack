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
import {observer} from 'mobx-react';
import PropTypes from 'prop-types';
import moment from 'moment';
import {Bar} from 'react-chartjs-2';

import './traceTimeline.less';
import linkBuilder from '../../../utils/linkBuilder';

// eslint-disable-next-line no-unused-vars
import * as zoom from 'chartjs-plugin-zoom';

const TraceTimeline = observer(({history, store}) => {
    const updateTimeFrame = (chart, event) => {
        if (event.length) {
            const results = store.timelineResults;

            // this means we reached lowest granularity, couldn't go further down
            if (results.length === 2) return;

            // eslint-disable-next-line no-underscore-dangle
            const selectedIndex = event[0]._index;
            const startTime = results[selectedIndex].x / 1000;
            const granularityMs = (results[1].x - results[0].x) / 1000;
            const endTime = startTime + granularityMs;
            const newSearch = {
                ...store.searchQuery,
                timePreset: null,
                startTime: null,
                endTime: null,
                time: {
                    from: startTime,
                    to: endTime
                }
            };

            history.push(linkBuilder.universalSearchTracesLink(newSearch));
        }
    };

    const {granularity, timelineResults, apiQuery} = store;
    const labels = [];
    const data = [];
    timelineResults.forEach((item) => {
        labels.push(new Date((item.x + granularity / 2) / 1000)); // small hack to correctly position bar in middle of "from" and "to" times
        data.push(item.y);
    });

    const chartData = {
        labels,
        datasets: [
            {
                label: 'traces count',
                backgroundColor: '#78c5f9',
                borderColor: '#36A2EB',
                borderWidth: 1,
                hoverBackgroundColor: '#b5def7',
                hoverBorderColor: '#36A2EB',
                data,
                barPercentage: 0.9,
                barThickness: 'flex',
                categoryPercentage: 1
            }
        ]
    };

    const options = {
        maintainAspectRatio: false,
        legend: {
            display: false
        },
        zoom: {
            enabled: true,
            mode: 'x',
            drag: {
                borderColor: 'rgba(63,77,113,0.4)',
                borderWidth: 0.3,
                backgroundColor: 'rgba(63,77,113,0.2)'
            },
            onZoom: ({chart}) => {
                const newSearch = {
                    ...store.searchQuery,
                    timePreset: null,
                    startTime: null,
                    endTime: null,
                    time: {
                        from: chart.options.scales.xAxes[0].ticks.min,
                        to: chart.options.scales.xAxes[0].ticks.max
                    }
                };

                history.push(linkBuilder.universalSearchTracesLink(newSearch));
            }
        },
        pan: {
            enabled: false,
            mode: 'x'
        },
        scales: {
            xAxes: [
                {
                    type: 'time',
                    distribution: 'series',
                    time: {
                        unit: 'minute'
                    },
                    ticks: {
                        min: new Date(parseInt(apiQuery.startTime, 10) / 1000),
                        max: new Date(parseInt(apiQuery.endTime, 10) / 1000),
                        autoSkip: true,
                        autoSkipPadding: 25,
                        maxRotation: 0,
                        bounds: 'ticks'
                    }
                }
            ],
            yAxes: [
                {
                    ticks: {
                        beginAtZero: true
                    }
                }
            ]
        },
        tooltips: {
            callbacks: {
                title: (tooltipItem) => {
                    const date = new Date(tooltipItem[0].xLabel).getTime();
                    const from = moment(date - granularity / 1000 / 2);
                    const to = moment(date + granularity / 1000 / 2);

                    return `${from.format('MM/DD/YY hh:mm:ss a')} to ${to.format('MM/DD/YY hh:mm:ss a')}`;
                }
            }
        }
    };

    return (
        <div className="trace-timeline-container">
            <Bar data={chartData} height={150} options={options} getElementsAtEvent={updateTimeFrame} />
        </div>
    );
});

TraceTimeline.propTypes = {
    history: PropTypes.object.isRequired,
    store: PropTypes.object.isRequired
};

export default TraceTimeline;
