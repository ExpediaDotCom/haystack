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

import moment from 'moment';
import metricGranularity from '../components/trends/utils/metricGranularity';
import formatters from './formatters';

const timeWindow = {};

timeWindow.presets = window.haystackUiConfig.timeWindowPresetOptions;

timeWindow.defaultPreset = timeWindow.presets && timeWindow.presets[2] || {shortName: '1h', longName: '1 hour', value: 60 * 60 * 1000};

timeWindow.toTimeRange = (ms) => {
    const nowInMilliseconds = new Date().getTime();
    return {
        from: moment(nowInMilliseconds).subtract(ms, 'milliseconds').valueOf(),
        until: nowInMilliseconds
    };
};

timeWindow.findMatchingPreset = value => timeWindow.presets.find(preset => preset.value === value);

timeWindow.findMatchingPresetByShortName = shortName => timeWindow.presets.find(preset => preset.shortName === shortName);

timeWindow.toCustomTimeRange = (from, until) => ({
    from,
    until,
    value: until - from,
    longName: formatters.toTimeRangeString(from, until),
    isCustomTimeRange: true
});

timeWindow.getLowerGranularity = (timeInMs) => {
    const maxNumberOfPoints = 100;
    return metricGranularity.getMinGranularity(timeInMs / maxNumberOfPoints);
};

timeWindow.getHigherGranularity = (timeInMs) => {
    if (timeInMs <= 60 * 60 * 1000) {
        // lte 60min
        return 1 * 60 * 1000; // then 1min
    } else if (timeInMs <= 5 * 60 * 60 * 1000) {
        // lte 5hrs
        return 5 * 60 * 1000; // then 5min
    }
    return 1 * 60 * 60 * 1000; // else 1hr
};

timeWindow.isAfterTTL = (current, ttlKey) => {
    const ttl = window.haystackUiConfig[ttlKey];
    if (ttl && ttl > 0) {
        return current.isAfter(moment().subtract(ttl));
    }
    return true;
};

export default timeWindow;
