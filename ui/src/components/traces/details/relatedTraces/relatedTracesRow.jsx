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
import {PropTypes as MobxPropTypes} from 'mobx-react';

import colorMapper from '../../../../utils/serviceColorMapper';
import linkBuilder from '../../../../utils/linkBuilder';
import formatters from '../../../../utils/formatters';

const RelatedTracesRow = ({traceId, serviceName, operationName, spanCount, startTime, rootError, services, duration}) => {
    const openTrendDetailInNewTab = () => {
        const search = {traceId};
        const traceUrl = linkBuilder.withAbsoluteUrl(linkBuilder.universalSearchTracesLink(search));

        const tab = window.open(traceUrl, '_blank');
        tab.focus();
    };

    // Formatters copied from trace ResultsTable.jsx and then refactored into jsx.
    // START TIME
    const timeColumnFormatter = () => (<div>
            <div className="table__primary">{formatters.toTimeago(startTime)}</div>
            <div className="table__secondary">{formatters.toTimestring(startTime)}</div>
        </div>);

    // ROOT SUCCESS
    const errorFormatter = (cell) => {
        const status = cell ? 'error' : 'success';
        return (<div className="table__status">
            <img src={`/images/${status}.svg`} alt={status} height="24" width="24" />
        </div>);
    };

    // TOTAL DURATION
    const totalDurationColumnFormatter = () => <div className="table__primary-duration text-right">{formatters.toDurationString(duration)}</div>;

    // SPAN COUNT
    const handleServiceList = () => {
        const serviceList = services.slice(0, 2).map(svc =>
            <span key={svc.name} className={'service-spans label ' + colorMapper.toBackgroundClass(svc.name)}>{svc.name +' x' + svc.spanCount}</span> // eslint-disable-line
        );

        if (services.length > 2) {
            serviceList.push(<span key="extra">...</span>);
        }
        return serviceList;
    };

    const spanColumnFormatter = () => (<div>
            <div className="table__primary">{spanCount}</div>
            <div>{handleServiceList()}</div>
        </div>);

    return (
        <tr onClick={openTrendDetailInNewTab}>
            <td className="trace-trend-table_cell">
                {timeColumnFormatter()}
            </td>
            <td className="trace-trend-table_cell">
                <div className={`service-spans label label-default ${colorMapper.toBackgroundClass(serviceName)}`}>{serviceName}</div>
                <div className="trace-trend-table_op-name">{operationName}</div>
            </td>
            <td className="trace-trend-table_cell">
                {errorFormatter(rootError)}
            </td>
            <td className="trace-trend-table_cell">
                {spanColumnFormatter()}
            </td>
            <td className="trace-trend-table_cell">
                {totalDurationColumnFormatter()}
            </td>
        </tr>
    );
};

RelatedTracesRow.propTypes = {
    traceId: PropTypes.string.isRequired,
    serviceName: PropTypes.string.isRequired,
    operationName: PropTypes.string.isRequired,
    spanCount: PropTypes.number.isRequired,
    startTime: PropTypes.number.isRequired,
    rootError: PropTypes.bool.isRequired,
    services: MobxPropTypes.observableArray,
    duration: PropTypes.number.isRequired
};

RelatedTracesRow.defaultProps = {
    services: []
};

export default RelatedTracesRow;
