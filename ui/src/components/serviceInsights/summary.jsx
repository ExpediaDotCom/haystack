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

import React from 'react';
import PropTypes from 'prop-types';

function Summary({data}) {
    return (
        <div className="header-summary">
            <div>
                Traces considered: {data.tracesConsidered} {data.traceLimitReached ? '(limit reached)' : ''}
            </div>
            {data.hasViolations && (
                <div className="violation-grid">
                    <div className="violation-title">Violations found: </div>
                    <div>
                        {Object.keys(data.violations).map((key) => (
                            <div key={key}>{`${key} (${data.violations[key]})`}</div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}

Summary.propTypes = {
    data: PropTypes.object.isRequired
};

export default Summary;
