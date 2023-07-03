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

import Label from './label';

function Labels({nodes}) {
    return (
        <g className="labels">
            {nodes.map((node) => {
                if (node.data.type === 'mesh') {
                    return null;
                }
                return <Label text={node.data.name} x={node.x} y={node.y} key={`label-${node.data.id}`} />;
            })}
        </g>
    );
}

Labels.propTypes = {
    nodes: PropTypes.array.isRequired
};

export default Labels;
