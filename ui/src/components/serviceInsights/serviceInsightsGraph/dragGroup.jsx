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

class DragGroup extends Component {
    static propTypes = {
        children: PropTypes.node.isRequired,
        offsetX: PropTypes.number.isRequired,
        offsetY: PropTypes.number.isRequired
    };

    state = {
        isDragging: false,
        origin: {x: 0, y: 0},
        coords: {x: this.props.offsetX, y: this.props.offsetY},
        last: {x: this.props.offsetX, y: this.props.offsetY}
    };

    dragStart = (event) => {
        this.setState({
            isDragging: true,
            origin: {x: event.clientX, y: event.clientY}
        });
    };

    dragMove = (event) => {
        const {isDragging, origin, last} = this.state;

        if (isDragging) {
            this.setState({
                coords: {
                    x: last.x + event.clientX - origin.x,
                    y: last.y + event.clientY - origin.y
                }
            });
        }
    };

    dragEnd = () => {
        this.setState({
            isDragging: false,
            last: this.state.coords
        });
    };

    render() {
        return (
            <g className="drag-group">
                <rect className="drag-target" onMouseDown={this.dragStart} onMouseMove={this.dragMove} onMouseUp={this.dragEnd} width="100%" />
                <g transform={`translate(${this.state.coords.x}, ${this.state.coords.y})`}>{this.props.children}</g>
            </g>
        );
    }
}

export default DragGroup;
