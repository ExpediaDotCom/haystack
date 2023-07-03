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

import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';

import traceDetailsStore from '../stores/traceDetailsStore';
import TraceDetails from '../details/traceDetails';
import formatters from '../../../utils/formatters';
import colorMapper from '../../../utils/serviceColorMapper';
import '../../common/resultsTable.less';

function sortByStartTime(a, b, order) {
    if (order === 'desc') {
        return b.startTime - a.startTime;
    }
    return a.startTime - b.startTime;
}
function sortByRootAndTime(a, b, order) {
    if (a.rootOperation === b.rootOperation) {
        return sortByStartTime(a, b, 'desc');
    }
    if (order === 'desc') {
        return a.rootOperation.localeCompare(b.rootOperation);
    }
    return b.rootOperation.localeCompare(a.rootOperation);
}
function sortByRootSuccessAndTime(a, b, order) {
    if (a.rootError === b.rootError) {
        return sortByStartTime(a, b, 'desc');
    }
    if (order === 'desc') {
        return (a.rootError ? 1 : -1);
    }
    return (b.rootError ? 1 : -1);
}
function sortByOperationSuccessAndTime(a, b, order) {
    if (a.operationError === b.operationError) {
        return sortByStartTime(a, b, 'desc');
    }
    if (order === 'desc') {
        return (a.operationError ? 1 : -1);
    }
    return (b.operationError ? 1 : -1);
}
function sortBySpansAndTime(a, b, order) {
    if (a.spanCount === b.spanCount) {
        return sortByStartTime(a, b, 'desc');
    }
    if (order === 'desc') {
        return b.spanCount - a.spanCount;
    }
    return a.spanCount - b.spanCount;
}

function handleServiceList(services) {
    let serviceList = '';
    services.slice(0, 2).map((svc) => {
        serviceList += `<span class="service-spans label ${colorMapper.toBackgroundClass(svc.name)}">${svc.name} x${svc.spanCount}</span> `;
        return serviceList;
    });

    if (services.length > 2) {
        serviceList += '<span>...</span>';
    }
    return serviceList;
}

function timeColumnFormatter(startTime) {
    return `<div class="table__primary">${formatters.toTimeago(startTime)}</div>
                <div class="table__secondary">${formatters.toTimestring(startTime)}</div>`;
}

function rootColumnFormatter(cell, row) {
    return `<div class="table__primary">
                <span class="service-spans label ${colorMapper.toBackgroundClass(row.root.serviceName)}">${row.root.serviceName}</span> 
                ${row.root.operationName}
                </div>
                <div class="table__secondary">${row.root.url}</div>`;
}

function spanColumnFormatter(cell, row) {
    const errorText = row.errorSpanCount ? `<small class="error-span-count label">${row.errorSpanCount} ${row.errorSpanCount > 1 ? 'errors' : 'error'}</small>` : '';
    return `<div class="table__primary">${cell} ${errorText}</div>
                <div>${handleServiceList(row.services)}</div>`;
}

function errorFormatter(cell) {
    if (cell) {
        return (<div className="table__status">
            <img src="/images/error.svg" alt="Error" height="24" width="24" />
        </div>);
    }
    return (<div className="table__status">
        <img src="/images/success.svg" alt="Success" height="24" width="24" />
    </div>);
}

function totalDurationColumnFormatter(duration) {
    return `<div class="table__primary-duration text-right">${formatters.toDurationString(duration)}</div>`;
}

function serviceDurationFormatter(duration, row) {
    return (<>
        <div className="table__primary text-right">{formatters.toDurationString(duration)}</div>
        <div className="table__secondary text-right">{row.serviceDurationPercent}% of total</div>
    </>);
}

function operationDurationFormatter(duration, row) {
    return (<>
        <div className="table__primary text-right">{formatters.toDurationString(duration)}</div>
        <div className="table__secondary text-right">{row.operationDurationPercent}% of total</div>
    </>);
}

function getCaret(direction) {
    if (direction === 'asc') {
        return (
            <span className="order dropup">
                  <span className="caret" style={{margin: '10px 5px'}}/>
              </span>);
    }
    if (direction === 'desc') {
        return (
            <span className="order dropdown">
                  <span className="caret" style={{margin: '10px 5px'}}/>
              </span>);
    }
    return <div/>;
}

const Header = ({name}) => <span className="results-header">{name}</span>;

const TraceResultsTable = ({query, results}) => {
    const [expanding, setExpanding] = useState([]);
    const [selected, setSelected] = useState([]);


    const handleExpand = (rowKey, isExpand) => {
        if (isExpand) {
            setExpanding([rowKey]);
            setSelected([rowKey]);
        } else {
            setExpanding([]);
            setSelected([]);
        }
    };

    useEffect(() => {
        if (results.length === 1) {
            handleExpand(results[0].traceId, true);
        }
    }, [results]);


    const expandComponent = (row) => {
        if (selected.filter(id => id === row.traceId).length > 0) {
            return <TraceDetails traceId={row.traceId} serviceName={query.serviceName} traceDetailsStore={traceDetailsStore} />;
        }
        return null;
    };

    const selectRowProp = {
        clickToSelect: true,
        clickToExpand: true,
        className: 'selected-row',
        mode: 'checkbox',
        hideSelectColumn: true,
        selected
    };

    const options = {
        page: 1,  // which page you want to show as default
        pageStartIndex: 1, // where to start counting the pages
        prePage: 'Prev', // Previous page button text
        nextPage: 'Next', // Next page button text
        firstPage: 'First', // First page button text
        lastPage: 'Last', // Last page button text
        hideSizePerPage: true, // Hide page size bar
        defaultSortName: query.sortBy || 'startTime',  // default sort column name
        defaultSortOrder: 'desc',  // default sort order
        expanding,
        onExpand: handleExpand,
        expandBodyClass: 'expand-row-body'
    };

    const tableHeaderStyle = { border: 'none' };
    const tableHeaderRightAlignedStyle = { border: 'none', textAlign: 'right' };

    return (
        <BootstrapTable
            data={results}
            tableStyle={{ border: 'none' }}
            trClassName="tr-no-border"
            options={options}
            expandableRow={() => true}
            expandComponent={expandComponent}
            selectRow={selectRowProp}
        >
            <TableHeaderColumn
                dataField="traceId"
                hidden
                isKey
            >TraceId</TableHeaderColumn>
            <TableHeaderColumn
                dataField="startTime"
                dataFormat={timeColumnFormatter}
                width="15"
                dataSort
                caretRender={getCaret}
                thStyle={tableHeaderStyle}
                headerText={'Start time of the first span in local timezone'}
            ><Header name="Start Time"/></TableHeaderColumn>
            <TableHeaderColumn
                dataField="rootOperation"
                dataFormat={rootColumnFormatter}
                width="25"
                dataSort
                sortFunc={sortByRootAndTime}
                caretRender={getCaret}
                thStyle={tableHeaderStyle}
                headerText={'Operation name of the root span'}
            ><Header name="Root"/></TableHeaderColumn>
            <TableHeaderColumn
                dataField="rootError"
                width="10"
                dataFormat={errorFormatter}
                dataSort
                sortFunc={sortByRootSuccessAndTime}
                caretRender={getCaret}
                thStyle={tableHeaderStyle}
                headerText={'Success of the root service'}
            ><Header name="Root Success"/></TableHeaderColumn>
            <TableHeaderColumn
                dataField="spanCount"
                dataFormat={spanColumnFormatter}
                width="25"
                dataSort
                sortFunc={sortBySpansAndTime}
                caretRender={getCaret}
                thStyle={tableHeaderStyle}
                headerText={'Total number of spans across all services in the trace'}
            ><Header name="Span Count"/></TableHeaderColumn>
            {
                (query.operationName && query.operationName !== 'all')
                && <TableHeaderColumn
                    dataField="operationError"
                    dataFormat={errorFormatter}
                    width="10"
                    dataSort
                    caretRender={getCaret}
                    sortFunc={sortByOperationSuccessAndTime}
                    thStyle={tableHeaderRightAlignedStyle}
                    headerText={'Success of the searched operation'}
                ><Header name="Op Success"/></TableHeaderColumn>
            }
            {
                (query.operationName && query.operationName !== 'all')
                && <TableHeaderColumn
                    dataField="operationDuration"
                    dataFormat={operationDurationFormatter}
                    width="10"
                    dataSort
                    caretRender={getCaret}
                    thStyle={tableHeaderRightAlignedStyle}
                    headerText={'Total busy time in timeline for the queried operation'}
                ><Header name="Op Duration"/></TableHeaderColumn>
            }
            {
                query.serviceName
                    ? <TableHeaderColumn
                        dataField="serviceDuration"
                        dataFormat={serviceDurationFormatter}
                        width="10"
                        dataSort
                        caretRender={getCaret}
                        thStyle={tableHeaderRightAlignedStyle}
                        headerText={'Total busy time in timeline for the queried service'}
                    ><Header name="Svc Duration"/></TableHeaderColumn>
                    : null
            }
            <TableHeaderColumn
                dataField="duration"
                dataFormat={totalDurationColumnFormatter}
                width="10"
                dataSort
                caretRender={getCaret}
                thStyle={tableHeaderRightAlignedStyle}
                headerText={'Duration of the span. It is the difference between the start time of earliest operation and the end time of last operation in the trace'}
            ><Header name="Total Duration"/></TableHeaderColumn>
        </BootstrapTable>
    );
};

Header.propTypes = {
    name: PropTypes.string.isRequired
};

TraceResultsTable.propTypes = {
    query: PropTypes.object.isRequired,
    results: PropTypes.array.isRequired
};

export default TraceResultsTable;
