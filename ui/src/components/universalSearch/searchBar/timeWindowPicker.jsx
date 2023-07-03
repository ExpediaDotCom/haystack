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
import {observer} from 'mobx-react';
import PropTypes from 'prop-types';
import TimeRangePicker from './timeRangePicker';
import formatters from '../../../utils/formatters';

@observer
export default class TimeWindowPicker extends React.Component {
    static propTypes = {
        uiState: PropTypes.object.isRequired
    };

    constructor(props) {
        super(props);

        this.timeRangeChangeCallback = this.timeRangeChangeCallback.bind(this);
        this.setWrapperRef = this.setWrapperRef.bind(this);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
        this.hideTimePicker = this.hideTimePicker.bind(this);
        this.showTimePicker = this.showTimePicker.bind(this);
        this.state = {showTimeRangePicker: false};
    }

    setWrapperRef(node) {
        this.wrapperRef = node;
    }

    hideTimePicker() {
        document.removeEventListener('mousedown', this.handleOutsideClick);
        this.setState({showTimeRangePicker: false});
    }

    showTimePicker() {
        document.addEventListener('mousedown', this.handleOutsideClick);
        this.setState({showTimeRangePicker: true});
    }

    timeRangeChangeCallback(timePreset, startTime, endTime) {
        this.props.uiState.setTimeWindow({timePreset, startTime, endTime});
        this.setState({timeRangePickerToggleText: formatters.toTimeRangeTextFromTimeWindow(timePreset, startTime, endTime)});
        this.hideTimePicker();
    }

    handleOutsideClick(e) {
        if (this.wrapperRef && !this.wrapperRef.contains(e.target)) {
            this.hideTimePicker();
        }
    }

    render() {
        const {
            timePreset,
            startTime,
            endTime
        } = this.props.uiState.timeWindow;

        const timeRangePickerToggleText = formatters.toTimeRangeTextFromTimeWindow(timePreset, startTime, endTime);

        return (
            <div ref={this.setWrapperRef} className="usb-timepicker">
                    <button
                        className="usb-timepicker__button"
                        type="button"
                        onClick={this.state.showTimeRangePicker ? this.hideTimePicker : this.showTimePicker}
                    >
                        {timeRangePickerToggleText}
                    </button>
                { this.state.showTimeRangePicker
                    ? <TimeRangePicker
                        timeRangeChangeCallback={this.timeRangeChangeCallback}
                        range={startTime && endTime ? {from: startTime, to: endTime} : null}
                    />
                    : null }
            </div>
        );
    }
}
