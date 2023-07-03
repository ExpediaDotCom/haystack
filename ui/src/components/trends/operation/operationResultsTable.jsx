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
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import trendTableFormatters from '../utils/trendsTableFormatters';

import TrendDetails from './../details/trendDetails';

import './operationResultsTable.less';

export default class OperationResultsTable extends React.Component {
    static propTypes = {
        serviceName: PropTypes.string.isRequired,
        operationStore: PropTypes.object.isRequired,
        interval: PropTypes.oneOfType([PropTypes.oneOf([null]), PropTypes.string]).isRequired
    };

    static Header({name}) {
        return <span className="results-header">{name}</span>;
    }

    constructor(props) {
        super(props);
        this.state = {
            expanding: [],
            selected: []
        };
        this.handleExpand = this.handleExpand.bind(this);
        this.expandComponent = this.expandComponent.bind(this);
        this.afterColumnFilter = this.afterColumnFilter.bind(this);
    }

    componentDidMount() {
        const opName = this.props.operationStore.statsQuery.filters && this.props.operationStore.statsQuery.filters.operationName && this.props.operationStore.statsQuery.filters.operationName.value;
        if (opName) {
            const matchingFilters = this.props.operationStore.statsResults.filter(stat => stat.operationName.includes(opName));
            if (matchingFilters.length === 1) this.handleExpand(matchingFilters[0].operationName, true);
        }
    }

    afterColumnFilter(filters) {
        this.props.operationStore.statsQuery.filters = filters;
    }

    handleExpand(rowKey, isExpand) {
        if (isExpand) {
            this.setState(
                {
                    expanding: [rowKey],
                    selected: [rowKey]
                }
            );
        } else {
            this.setState(
                {
                    expanding: [],
                    selected: []
                }
            );
        }
    }

    expandComponent(row) {
        if (this.state.selected.filter(id => id === row.operationName).length > 0) {
            return (<TrendDetails
                        serviceSummary={false}
                        store={this.props.operationStore}
                        serviceName={this.props.serviceName}
                        opName={row.operationName}
                        interval={this.props.interval}
            />);
        }
        return null;
    }

    render() {
        const tableHeaderRightAlignedStyle = {border: 'none', textAlign: 'right'};
        const tableHeaderStyle = {border: 'none'};

        const options = {
            page: 1,  // which page you want to show as default
            sizePerPage: 20,  // which size per page you want to locate as default
            pageStartIndex: 1, // where to start counting the pages
            paginationSize: 3,  // the pagination bar size.
            prePage: 'Prev', // Previous page button text
            nextPage: 'Next', // Next page button text
            firstPage: 'First', // First page button text
            lastPage: 'Last', // Last page button text
            paginationShowsTotal: (start, to, total) =>
                (<p>Showing { (to - start) + 1 } out of { total } total {total === 1 ? 'operation' : 'operations'}</p>),
            hideSizePerPage: true, // Hide page size bar
            defaultSortName: 'totalCount',  // default sort column name
            defaultSortOrder: 'desc',  // default sort order
            expanding: this.state.expanding,
            onExpand: this.handleExpand,
            expandBodyClass: 'expand-row-body',
            noDataText: 'No such operation found, try clearing filters',
            afterColumnFilter: this.afterColumnFilter
        };

        const selectRowProp = {
            clickToSelect: true,
            clickToExpand: true,
            className: 'selected-row',
            mode: 'checkbox',
            hideSelectColumn: true,
            selected: this.state.selected
        };

        const {
            operationName,
            gtCount,
            ltCount,
            gtSuccessPercent,
            ltSuccessPercent,
            gtTP99Count,
            ltTP99Count
        } = this.props.operationStore.statsQuery.filters || {};

        const operationNameFilter = operationName
            ? {type: 'RegexFilter', defaultValue: `${operationName}`, delay: 0, placeholder: 'FilterOperation (Regex)...'}
            : {type: 'RegexFilter', delay: 0, placeholder: 'Filter Operations (Regex)...'};

        const numberFilterFormatter = {
            type: 'NumberFilter',
            delay: 0,
            numberComparators: ['>', '<'],
            defaultValue: { comparator: '>' }
        };

        const countDefaultFilter = trendTableFormatters.getCountDefaultFilter(gtCount, ltCount);
        const percentDefaultFilter = trendTableFormatters.getPercentDefaultFilter(gtSuccessPercent, ltSuccessPercent);
        const tp99DefaultFilter = trendTableFormatters.getTp99DefaultFilter(gtTP99Count, ltTP99Count);

        return (
            <BootstrapTable
                className="trends-panel"
                data={this.props.operationStore.statsResults}
                tableStyle={{border: 'none'}}
                trClassName="tr-no-border"
                expandableRow={() => true}
                expandComponent={this.expandComponent}
                selectRow={selectRowProp}
                pagination
                options={options}
            >

                <TableHeaderColumn
                    isKey
                    dataFormat={trendTableFormatters.columnFormatter}
                    dataField="operationName"
                    width="50"
                    dataSort
                    sortFunc={trendTableFormatters.sortByName}
                    caretRender={trendTableFormatters.getCaret}
                    thStyle={tableHeaderStyle}
                    filter={operationNameFilter}
                    headerText={'All operations for the service'}
                ><OperationResultsTable.Header name="Operation"/></TableHeaderColumn>
                <TableHeaderColumn
                    dataField="totalCount"
                    dataFormat={trendTableFormatters.countColumnFormatter}
                    width="15"
                    dataSort
                    sortFunc={trendTableFormatters.sortByTotalCount}
                    filter={{
                        ...numberFilterFormatter,
                        defaultValue: countDefaultFilter,
                        placeholder: 'Total Count...'
                    }}
                    caretRender={trendTableFormatters.getCaret}
                    thStyle={tableHeaderRightAlignedStyle}
                    headerText={'Total invocation count of the operation for summary duration'}
                ><OperationResultsTable.Header name="Count"/></TableHeaderColumn>
                <TableHeaderColumn
                    dataField="latestTp99Duration"
                    dataFormat={trendTableFormatters.durationColumnFormatter}
                    width="15"
                    dataSort
                    filter={{
                        ...numberFilterFormatter,
                        defaultValue: tp99DefaultFilter,
                        placeholder: 'Last TP99 in ms...'
                    }}
                    caretRender={trendTableFormatters.getCaret}
                    thStyle={tableHeaderRightAlignedStyle}
                    headerText={'TP99 duration for the operation. Sorting is based on duration of the last data point, which is marked as a dot'}
                ><OperationResultsTable.Header name="Duration TP99"/></TableHeaderColumn>
                <TableHeaderColumn
                    dataField="avgSuccessPercent"
                    dataFormat={trendTableFormatters.successPercentFormatter}
                    width="15"
                    dataSort
                    sortFunc={trendTableFormatters.sortByAvgPercentage}
                    filter={{
                        ...numberFilterFormatter,
                        defaultValue: percentDefaultFilter,
                        placeholder: 'Avg Success %...'
                    }}
                    caretRender={trendTableFormatters.getCaret}
                    thStyle={tableHeaderRightAlignedStyle}
                    headerText={'Success % for the operation'}
                ><OperationResultsTable.Header name="Success %"/></TableHeaderColumn>
            </BootstrapTable>
        );
    }
}
