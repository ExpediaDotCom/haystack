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
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';

import {observer} from 'mobx-react';
import AlertsTableSparkline from './alertsTableSparkline';

import {toQuery} from '../../utils/queryParser';
import AlertDetails from './details/alertDetails';
import alertDetailsStore from './stores/alertDetailsStore';
import formatters from '../../utils/formatters';
import timeWindow from '../../utils/timeWindow';
import './alerts';

@observer
export default class AlertsTable extends React.Component {
    static propTypes = {
        location: PropTypes.object.isRequired,
        serviceName: PropTypes.string.isRequired,
        alerts: PropTypes.array.isRequired,
        defaultPreset: PropTypes.object.isRequired,
        alertType: PropTypes.string.isRequired,
        interval: PropTypes.string.isRequired
    };

    static nameColumnFormatter(cell) {
        return `<div class="table__primary">${cell}</div>`;
    }

    static sortByTimestampAndHealthAndOperationName(a, b, order) {
        if (a.timestamp === b.timestamp) {
            if (order === 'desc') {
                return a.operationName.localeCompare(b.operationName);
            }
            return b.operationName.localeCompare(a.operationName);
        }

        if (order === 'desc') {
            return b.timestamp - a.timestamp;
        }

        return a.timestamp - b.timestamp;
    }

    static statusColumnFormatter(cell) {
        if (cell) {
            return '<span class="label label-failure table__large-label">unhealthy</span>';
        }
        return '<span class="label label-success table__large-label">healthy</span>';
    }

    static timestampColumnFormatter(timestamp) {
        if (timestamp) {
            return `<div class=""><b>${formatters.toTimeago(timestamp * 1000)}</b> at ${formatters.toTimestring(timestamp * 1000)}</div>`;
        }
        return '<div/>';
    }

    static trendColumnFormatter(row, location, activeWindow, alertType, serviceName) {
        const timeRange = timeWindow.toTimeRange(activeWindow.value);
        return (
            <AlertsTableSparkline
                serviceName={serviceName}
                operationName={row.operationName}
                trendType={AlertsTable.toTrendTypeString(alertType)}
                location={location}
                granularity={300000}
                from={timeRange.from}
                until={timeRange.until}
            />
        );
    }

    static Header({name}) {
        return <span className="results-header">{name}</span>;
    }

    static getCaret(direction) {
        if (direction === 'asc') {
            return (
                <span className="order dropup">
                    <span className="caret" style={{margin: '10px 5px'}} />
                </span>
            );
        }
        if (direction === 'desc') {
            return (
                <span className="order dropdown">
                    <span className="caret" style={{margin: '10px 5px'}} />
                </span>
            );
        }
        return <div />;
    }

    static rowClassNameFormat(row) {
        return row.isUnhealthy ? 'tr-no-border alert-details__alert-glow' : 'tr-no-border';
    }

    static toAlertTypeString = (num) => {
        if (num === 'duration') {
            return 'Duration TP99';
        } else if (num === 'failure-span') {
            return 'Failure Count';
        }

        return null;
    };

    static toTrendTypeString = (alertType) => {
        if (alertType === 'duration') {
            return 'tp99Duration';
        } else if (alertType === 'failure-span') {
            return 'failureCount';
        }

        return null;
    };

    constructor(props) {
        super(props);

        const query = toQuery(this.props.location.search);
        const operationName = query.operationName;

        this.state = {
            expanding: [],
            selected: [],
            operationName
        };

        this.handleExpand = this.handleExpand.bind(this);
        this.expandComponent = this.expandComponent.bind(this);
    }

    handleExpand(rowKey, isExpand) {
        if (isExpand) {
            this.setState({
                expanding: [rowKey],
                selected: [rowKey]
            });
        } else {
            this.setState({
                expanding: [],
                selected: []
            });
        }
    }

    expandComponent(row) {
        if (this.state.selected.filter((alertId) => alertId === row.alertId).length > 0) {
            return (
                <AlertDetails
                    serviceName={this.props.serviceName}
                    operationName={row.operationName}
                    type={this.props.alertType}
                    interval={this.props.interval}
                    alertDetailsStore={alertDetailsStore}
                />
            );
        }
        return null;
    }

    render() {
        const operationFilter = this.state.operationName
            ? {type: 'RegexFilter', defaultValue: decodeURIComponent(this.state.operationName), delay: 0, placeholder: 'FilterOperation (Regex)...'}
            : {type: 'RegexFilter', delay: 0, placeholder: 'FilterOperation (Regex)...'};

        const results = this.props.alerts.map((result, index) => ({...result, alertId: index, timestamp: result.timestamp || 0}));

        const selectRowProp = {
            clickToSelect: true,
            clickToExpand: true,
            className: 'selected-row',
            mode: 'checkbox',
            hideSelectColumn: true,
            selected: this.state.selected
        };

        const options = {
            page: 1, // which page you want to show as default
            sizePerPage: 15, // which size per page you want to locate as default
            pageStartIndex: 1, // where to start counting the pages
            paginationSize: 3, // the pagination bar size.
            prePage: 'Prev', // Previous page button text
            nextPage: 'Next', // Next page button text
            firstPage: 'First', // First page button text
            lastPage: 'Last', // Last page button text
            paginationShowsTotal: (start, to, total) => (
                <p>
                    Showing alerts {start} to {to} out of {total}
                </p>
            ),
            hideSizePerPage: true, // Hide page size bar
            defaultSortName: 'timestamp',
            defaultSortOrder: 'desc', // default sort order
            expanding: this.state.expanding,
            onExpand: this.handleExpand,
            expandBodyClass: 'expand-row-body'
        };

        const tableHeaderStyle = {border: 'none'};

        const query = toQuery(this.props.location.search);
        const activeWindow = query.preset ? timeWindow.presets.find((presetItem) => presetItem.shortName === query.preset) : this.props.defaultPreset;

        return (
            <div>
                <BootstrapTable
                    className="alerts-table"
                    data={results}
                    tableStyle={{border: 'none'}}
                    trClassName={AlertsTable.rowClassNameFormat}
                    options={options}
                    pagination
                    expandableRow={() => true}
                    expandComponent={this.expandComponent}
                    selectRow={selectRowProp}
                >
                    <TableHeaderColumn dataField="alertId" hidden isKey>
                        AlertId
                    </TableHeaderColumn>
                    <TableHeaderColumn
                        caretRender={AlertsTable.getCaret}
                        dataFormat={AlertsTable.nameColumnFormatter}
                        dataField="operationName"
                        width="30"
                        dataSort
                        filter={operationFilter}
                        thStyle={tableHeaderStyle}
                        headerText={'Operation Name'}
                    >
                        <AlertsTable.Header name="Operation" />
                    </TableHeaderColumn>
                    <TableHeaderColumn
                        caretRender={AlertsTable.getCaret}
                        dataField="isUnhealthy"
                        width="8"
                        dataFormat={AlertsTable.statusColumnFormatter}
                        dataSort
                        thStyle={tableHeaderStyle}
                        headerText={'Status'}
                    >
                        <AlertsTable.Header name="Status" />
                    </TableHeaderColumn>
                    <TableHeaderColumn
                        caretRender={AlertsTable.getCaret}
                        dataFormat={AlertsTable.timestampColumnFormatter}
                        dataField="timestamp"
                        width="20"
                        dataSort
                        sortFunc={AlertsTable.sortByTimestampAndHealthAndOperationName}
                        thStyle={tableHeaderStyle}
                        headerText={'Alert Timestamp'}
                    >
                        <AlertsTable.Header name="Status Changed" />
                    </TableHeaderColumn>
                    <TableHeaderColumn
                        caretRender={AlertsTable.getCaret}
                        dataField="trend"
                        width="50"
                        dataFormat={(cell, row) =>
                            AlertsTable.trendColumnFormatter(row, this.props.location, activeWindow, this.props.alertType, this.props.serviceName)
                        }
                        dataSort
                        thStyle={tableHeaderStyle}
                        headerText={'trend'}
                    >
                        <AlertsTable.Header name={`Trend (last ${activeWindow.longName})`} />
                    </TableHeaderColumn>
                </BootstrapTable>
            </div>
        );
    }
}
