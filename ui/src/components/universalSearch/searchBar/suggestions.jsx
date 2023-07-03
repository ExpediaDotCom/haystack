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

export default class Suggestions extends React.Component {
    static propTypes = {
        suggestionStrings: PropTypes.array.isRequired,
        suggestedOnType: PropTypes.string,
        suggestedOnValue: PropTypes.string,
        suggestionIndex: PropTypes.number,
        handleHover: PropTypes.func.isRequired,
        handleSelection: PropTypes.func.isRequired
    };

    static defaultProps = {
        suggestionIndex: null,
        suggestedOnValue: null,
        suggestedOnType: null
    };

    static formatSuggestion(item, suggestedOnValue) {
        const startIndex = item.toLowerCase().indexOf(suggestedOnValue.toLowerCase());

        return (
            <span>
                <span>{item.substring(0, startIndex)}</span>
                <span className="usb-suggestions__field-highlight ">{item.substring(startIndex, startIndex + suggestedOnValue.length)}</span>
                <span>{item.substring(startIndex + suggestedOnValue.length, item.length)}</span>
            </span>);
    }

    constructor(props) {
        super(props);
        this.state = {
            suggestionStrings: this.props.suggestionStrings,
            suggestedOnType: this.props.suggestedOnType,
            suggestedOnValue: this.props.suggestedOnValue,
            suggestionIndex: this.props.suggestionIndex
        };
    }

    componentWillReceiveProps(next) {
        this.setState({
            suggestionStrings: next.suggestionStrings,
            suggestedOnType: next.suggestedOnType,
            suggestedOnValue: next.suggestedOnValue,
            suggestionIndex: next.suggestionIndex
        });
    }

    render() {
        return (
            <div className="usb-suggestions__fields-wrapper pull-left">
                <div className="usb-suggestions__field-category ">Tag {this.state.suggestedOnType}</div>
                <ul className="usb-suggestions__fields">
                    {this.state.suggestionStrings.map((item, i) => (
                        <li
                            key={item.value}
                            onMouseEnter={() => this.props.handleHover(i)}
                            className={this.state.suggestionIndex === i ? 'usb-suggestions__field usb-suggestions__field--active' : 'usb-suggestions__field'}
                        >
                            <div role="link" tabIndex="0" onClick={this.props.handleSelection}>
                                {Suggestions.formatSuggestion(item.value, this.state.suggestedOnValue)}
                                <span className="usb-suggestions__description">{item.description}</span>
                            </div>
                        </li>)
                    )}
                </ul>
            </div>
        );
    }
}
