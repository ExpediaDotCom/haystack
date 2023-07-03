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

import './missingTrend.less';

const MissingTrend = ({title}) =>
    (
        <div className="col-md-12">
            <h5 className="text-center">{title}</h5>
            <div className="text-center">
                <div className="missing-data-point-warn">
                    No data points found
                </div>
            </div>
        </div>
    );

MissingTrend.propTypes = {
    title: PropTypes.string.isRequired
};

export default MissingTrend;
