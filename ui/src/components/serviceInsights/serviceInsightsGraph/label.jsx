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

export default class Label extends Component {
    static propTypes = {
        text: PropTypes.string.isRequired,
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired
    };

    state = {
        ellipsisify: true
    };

    getText() {
        if (this.state.ellipsisify) {
            return this.ellipsis(this.props.text);
        }
        return this.props.text;
    }

    ellipsis = (name) => {
        const limit = 26;
        return name.length > limit ? `${name.slice(0, limit - 3)}...` : name;
    };

    handleMouseOver = () => {
        this.setState({ellipsisify: false});
    };

    handleMouseOut = () => {
        this.setState({ellipsisify: true});
    };

    render() {
        const {x, y} = this.props;
        const text = this.getText();
        return (
            <g
                className="service-name"
                onMouseOver={this.handleMouseOver}
                onMouseOut={this.handleMouseOut}
                onFocus={this.handleMouseOver}
                onBlur={this.handleMouseOut}
                transform={`translate(${x}, ${y})`}
            >
                <text className="service-name-outline" x="11" y="4">
                    {text}
                </text>
                <text className="service-name-main" x="11" y="4">
                    {text}
                </text>
            </g>
        );
    }
}
