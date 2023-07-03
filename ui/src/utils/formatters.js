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

import timeago from 'timeago.js';
import moment from 'moment';
import { toPresetDisplayText } from '../components/traces/utils/presets';

const formatters = {};

formatters.toTimestring = startTime => moment(Math.floor(startTime / 1000)).format('HH:mm:ss, DD MMM YY');

formatters.toShortTimestring = startTime => moment(Math.floor(startTime / 1000)).format('HH:mm:ss');

formatters.toTimestringWithMs = startTime => moment(Math.floor(startTime / 1000)).format('HH:mm:ss.SSS, DD MMM YY');

formatters.toTimeago = startTime => timeago().format(Math.floor(startTime / 1000));

formatters.toDurationMsString = duration => `${Math.floor(duration / 1000)}ms`;

formatters.toDurationString = (duration) => {
    if (duration === 0) {
        return '0';
    } else if (duration < 1000000) {
        return `${Math.floor(duration / 1000)} ms`;
    } else if (duration < 60000000) {
        return `${(duration / 1000000).toFixed(2)} s`;
    } else if (duration < 7200000000) {
        return `${(duration / 60000000).toFixed(2)} mins`;
    }
    return `${(duration / 3600000000).toFixed(2)} h`;
};

formatters.toDurationStringInSecAndMin = (duration) => {
    if (duration === 0) {
        return '0';
    } else if (duration < 60000000) {
        return `${Math.floor(duration / 1000000)} sec`;
    }
    return `${Math.floor(duration / 60000000)} min`;
};

formatters.toDurationStringFromMs = (duration) => {
    if (duration === 0) {
        return '0ms';
    } else if (duration < 1000) {
        return `${duration}ms`;
    }
    return `${(duration / 1000).toFixed(1)}s`;
};

formatters.toNumberString = (num) => {
    if (num === 0) {
        return '0';
    } else if (num < 1000) {
        return `${num}`;
    } else if (num < 1000000) {
        return `${(num / 1000).toFixed(1)}k`;
    }
    return `${(num / 1000000).toFixed(1)}m`;
};

formatters.toTimeRangeString = (fromInMs, untilInMs) => {
    const start = moment(fromInMs);
    const end = moment(untilInMs);

    return `${start.format('L')} ${start.format('LT')} - ${end.format('L')} ${end.format('LT')}`;
};

formatters.toTimeRangeTextFromTimeWindow = (timePreset, startTime, endTime) => {
    if (timePreset) {
        return toPresetDisplayText(timePreset);
    }

    return formatters.toTimeRangeString(parseInt(startTime, 10), parseInt(endTime, 10));
};

export default formatters;
