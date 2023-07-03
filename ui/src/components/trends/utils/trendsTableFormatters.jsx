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
import '../operation/operationResultsTable.less';

import Sparklines from './trendSparklines';

export default class TrendsTableFormatters {
    static Header({name}) {
        return <span className="results-header">{name}</span>;
    }

    static getCaret(direction) {
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

    static columnFormatter(operation) {
        return `<div class="table__left">${operation}</div>`;
    }

    static countColumnFormatter(cell, row) {
        return <Sparklines.CountSparkline total={cell} points={row.countPoints.map(d => d.value)} />;
    }

    static durationColumnFormatter(cell, row) {
        return <Sparklines.DurationSparkline latest={cell} points={row.tp99DurationPoints.map(d => d.value)} />;
    }

    static successPercentFormatter(cell, row) {
        return <Sparklines.SuccessPercentSparkline average={cell} points={row.successPercentPoints.map(d => d.value)} />;
    }

    static sortByName(a, b, order) {
        if (order === 'desc') {
            return a.operationName.toLowerCase().localeCompare(b.operationName.toLowerCase());
        }
        return b.operationName.toLowerCase().localeCompare(a.operationName.toLowerCase());
    }

    static sortByTotalCount(a, b, order) {
        if (order === 'desc') {
            return b.totalCount - a.totalCount;
        }
        return a.totalCount - b.totalCount;
    }

    static sortByAvgPercentage(a, b, order) {
        if (order === 'desc') {
            return b.avgSuccessPercent - a.avgSuccessPercent;
        }
        return a.avgSuccessPercent - b.avgSuccessPercent;
    }

    static getCountDefaultFilter(gtCount, ltCount) {
        let countDefaultFilter = { comparator: '>' };
        if (gtCount) {
            countDefaultFilter.number = gtCount;
        } else if (ltCount) {
            countDefaultFilter = {number: ltCount, comparator: '<'};
        }
        return countDefaultFilter;
    }

    static getPercentDefaultFilter(gtSuccessPercent, ltSuccessPercent) {
        let percentDefaultFilter = { comparator: '<' };
        if (ltSuccessPercent) {
            percentDefaultFilter.number = ltSuccessPercent;
        } else if (gtSuccessPercent) {
            percentDefaultFilter = {number: gtSuccessPercent, comparator: '>'};
        }
        return percentDefaultFilter;
    }

    static getTp99DefaultFilter(gtTP99Count, ltTP99Count) {
        let tp99DefaultFilter = { comparator: '>' };
        if (gtTP99Count) {
            tp99DefaultFilter.number = gtTP99Count;
        } else if (ltTP99Count) {
            tp99DefaultFilter = {number: ltTP99Count, comparator: '<'};
        }
        return tp99DefaultFilter;
    }
}

