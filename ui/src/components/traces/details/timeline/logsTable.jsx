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
import formatters from '../../../../utils/formatters';

const LogEnum = {
    ss: 'Server Send',
    sr: 'Server Receive',
    cs: 'Client Send',
    cr: 'Client Receive'
};

const LogsTable = ({logs, startTime}) => {
    const flattenedLogs =
        logs
        .map(log => log.fields.map(field => ({
            timestamp: log.timestamp,
            key: field.key,
            value: field.value
        })))
        .reduce((x, y) => x.concat(y), [])
        .sort((a, b) => a.timestamp - b.timestamp);

    if (flattenedLogs.length) {
        return (<table className="table table-striped">
            <thead>
            <tr>
                <th>Key</th>
                <th>Value</th>
                <th>Relative</th>
                <th>Timestamp</th>
            </tr>
            </thead>
            <tbody>

            {flattenedLogs.map(log =>
                (<tr>
                    <td>{log.key}</td>
                    <td>{LogEnum[log.value] || log.value}</td>
                    <td>{formatters.toDurationString(log.timestamp - startTime)}</td>
                    <td>{formatters.toTimestringWithMs(log.timestamp)}</td>
                </tr>)
            )}
            </tbody>
        </table>);
    }
    return <h6>No logs associated with span</h6>;
};

LogsTable.propTypes = {
    logs: PropTypes.object.isRequired,
    startTime: PropTypes.number.isRequired
};

export default LogsTable;
