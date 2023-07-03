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

import React, {Component} from 'react';
import PropTypes from 'prop-types';

import {type} from '../../../../universal/enums';

import databaseIcon from './icons/db.svg';
import gatewayIcon from './icons/gateway.svg';
import outboundIcon from './icons/outbound.svg';
import uninstrumentedIcon from './icons/uninstrumented.svg';

const iconMap = {
    [type.database]: databaseIcon,
    [type.gateway]: gatewayIcon,
    [type.outbound]: outboundIcon,
    [type.uninstrumented]: uninstrumentedIcon
};

export default class Nodes extends Component {
    static propTypes = {
        nodes: PropTypes.array.isRequired,
        onHover: PropTypes.func.isRequired,
        onLeave: PropTypes.func.isRequired
    };

    nodeRefs = {};

    // Get node position, including transforms, and call the onHover prop
    handleHover(data) {
        return (event) => {
            const {left, top, width, height} = this.nodeRefs[data.id].getBoundingClientRect();

            this.props.onHover(event, left + width, top + height / 2, data);
        };
    }

    render() {
        const {nodes, onLeave} = this.props;

        return (
            <g className="nodes">
                {nodes.map(({data, x, y}) => {
                    const commonProps = {
                        key: data.id,
                        onMouseOver: this.handleHover(data),
                        onMouseOut: onLeave,
                        transform: `translate(${x}, ${y})`,
                        ref: (el) => (this.nodeRefs[data.id] = el) // eslint-disable-line no-return-assign
                    };

                    const violationClass = data.invalidCycleDetected ? 'violation' : '';

                    // Icon nodes
                    if (iconMap[data.type]) {
                        return (
                            <g className={`graph-icon ${violationClass}`} {...commonProps}>
                                <rect width="20" height="20" x="-10" y="-10" className="node icon-base" />
                                <image width="18" height="18" x="-9" y="-9" xlinkHref={iconMap[data.type]} style={{pointerEvents: 'none'}} />
                            </g>
                        );
                    }

                    // Circle nodes
                    const r = data.type === type.mesh ? 4 : 7;
                    return <circle r={r} className={`node ${data.relationship} ${data.type} ${violationClass}`} {...commonProps} />;
                })}
            </g>
        );
    }
}
