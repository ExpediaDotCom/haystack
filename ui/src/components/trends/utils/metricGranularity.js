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


const granularity = {};

granularity.options = [
    {
        shortName: '1m',
        longName: 'OneMinute',
        value: 60 * 1000
    },
    {
        shortName: '5m',
        longName: 'FiveMinute',
        value: 5 * 60 * 1000
    },
    {
        shortName: '15m',
        longName: 'FifteenMinute',
        value: 15 * 60 * 1000
    },
    {
        shortName: '1h',
        longName: 'OneHour',
        value: 60 * 60 * 1000
    }
];

granularity.getMinGranularity = timeInMs => granularity.options.find(option => option.value >= timeInMs) || granularity.options[granularity.options.length - 1];

const granularityRanges = [60 * 1000, 5 * 60 * 1000, 15 * 60 * 1000, 30 * 60 * 1000, 60 * 60 * 1000, 3 * 60 * 60 * 1000, 6 * 60 * 60 * 1000, 12 * 60 * 60 * 1000, 24 * 60 * 60 * 1000];

granularity.getMaxGranularity = timeInMs => granularityRanges.find(option => option >= timeInMs);

export default granularity;
