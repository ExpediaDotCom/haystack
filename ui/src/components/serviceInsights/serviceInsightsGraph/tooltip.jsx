/*
 * Copyright 2019 Expedia Group
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

import React, {Fragment} from 'react';

import './tooltip.less';

function lineToolTip(x, y, data) {
    const hasIssues = data.invalidCycleDetected;

    return (
        <div className="graph-tooltip">
            <div className="graph-tooltip_decoration line" style={{left: x, top: y}}>
                <div className="tip-grid">
                    <div className="tip-grid_label">Trace Count:</div>
                    <div className="tip-grid_value">{data.tps}</div>

                    {hasIssues && (
                        <Fragment>
                            <div className="tip-grid_label">Issues:</div>
                            <div className="tip-grid_value" data-issue>
                                {data.invalidCycleDetected && <div>{'⚠️Invalid cycle detected'}</div>}
                            </div>
                        </Fragment>
                    )}
                </div>
            </div>
        </div>
    );
}

function nodeToolTip(x, y, data) {
    const hasIssues = data.type === 'uninstrumented';
    const isDebug = new URLSearchParams(window.location.search).get('debug') === 'true';

    return (
        <div className="graph-tooltip">
            <div className="graph-tooltip_decoration" style={{left: x, top: y}}>
                <div className="graph-tooltip_title">{data.name}</div>

                <div className="tip-grid">
                    <div className="tip-grid_label">Type:</div>
                    <div className="tip-grid_value">{data.type}</div>
                    <div className="tip-grid_label">Trace Count:</div>
                    <div className="tip-grid_value">{data.count}</div>
                    <div className="tip-grid_label">Avg Duration:</div>
                    <div className="tip-grid_value">{data.avgDuration}</div>
                    <div className="tip-grid_label">Operations:</div>
                    <div className="tip-grid_value">
                        {Object.keys(data.operations).map((key) => (
                            <div key={key}>{`${key}: ${data.operations[key]}`}</div>
                        ))}
                    </div>
                    {hasIssues && (
                        <Fragment>
                            <div className="tip-grid_label">Issues:</div>
                            <div className="tip-grid_value">
                                {data.type === 'uninstrumented' && <div>{'⚠️Service not instrumented with open tracing'}</div>}
                            </div>
                        </Fragment>
                    )}
                </div>

                {isDebug && (
                    <Fragment>
                        <div className="graph-tooltip_title debug">Debug Info:</div>
                        <div className="tip-grid debug">
                            <div className="tip-grid_label">id:</div>
                            <div className="tip-grid_value">{data.id}</div>
                            <div className="tip-grid_label">relationship:</div>
                            <div className="tip-grid_value">{data.relationship}</div>
                        </div>
                    </Fragment>
                )}
            </div>
        </div>
    );
}

export default function Tooltip({x, y, visible, type, data}) {
    if (visible) {
        if (type === 'line') {
            return lineToolTip(x, y, data);
        }
        return nodeToolTip(x, y, data);
    }
    return null;
}
