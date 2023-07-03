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
import {Line} from 'react-chartjs-2';
import PropTypes from 'prop-types';
import fetcher from './stores/alertTrendFetcher';

export default class AlertsTableSparkline extends React.Component {
    static propTypes = {
        serviceName: PropTypes.string.isRequired,
        operationName: PropTypes.string.isRequired,
        granularity: PropTypes.number.isRequired,
        from: PropTypes.number.isRequired,
        until: PropTypes.number.isRequired,
        trendType: PropTypes.string.isRequired
    };

    componentDidMount() {
        fetcher
            .fetchOperationTrends(this.props.serviceName, this.props.operationName, this.props.granularity, this.props.from, this.props.until)
            .then((result) => {
                this.setState({trends: result});
            });
    }

    render() {
        const trends = this.state && this.state.trends;

        const alertsTableSparklineOptions = {
            tooltips: {enabled: false},
            hover: {mode: null},
            maintainAspectRatio: false,
            scales: {
                yAxes: [{display: false}],
                xAxes: [
                    {
                        type: 'time',
                        time: {
                            min: new Date(this.props.from),
                            max: new Date(this.props.until)
                        },
                        display: false
                    }
                ]
            }
        };

        const metricTrend =
            this.state &&
            this.state.trends[this.props.trendType].map((point) => ({
                x: new Date(point.timestamp),
                y: point.value || 0
            }));

        const chartData = {
            datasets: [
                {
                    data: metricTrend,
                    backgroundColor: [['rgba(54, 162, 235, 0.4']],
                    borderColor: [['rgba(54, 162, 235, 1.2)']],
                    borderWidth: 1,
                    pointRadius: 0,
                    pointHoverRadius: 0
                }
            ]
        };

        if (!trends) {
            return <span className="alert-table_sparkline-loading table__secondary">loading...</span>;
        }

        if (metricTrend.length === 0) {
            return <span className="alert-table_sparkline-loading table__secondary">No trend points</span>;
        }

        if (metricTrend.every((t) => t.y === 0)) {
            chartData.datasets.push({
                data: [{...metricTrend[0], y: 1}],
                backgroundColor: [['rgb(255, 255, 255']],
                borderColor: [['rgb(255, 255, 255']],
                borderWidth: 1,
                pointRadius: 0,
                pointHoverRadius: 0
            });
        }

        return (
            <div className="alert-table_sparkline">
                <Line data={chartData} options={alertsTableSparklineOptions} type="line" height={50} legend={{display: false}} />
            </div>
        );
    }
}
