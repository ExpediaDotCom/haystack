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
const createFilterExpression = require('../expressionTreeBuilder').createFilterExpression;

const requestBuilder = {};
const messages = require('../../../../../static_codegen/traceReader_pb');

const DEFAULT_INTERVAL_LIMIT = 60 * 1000 * 1000; // 5m in micro seconds

function roundUpToGranularity(timeString, granularityString) {
    const granularity = parseInt(granularityString, 10);
    const time = parseInt(timeString, 10);

    return ((parseInt((time / granularity), 10) + 1) * granularity) - 1;
}

function roundDownToGranularity(timeString, granularityString) {
    const granularity = parseInt(granularityString, 10);
    const time = parseInt(timeString, 10);
    return parseInt((time / granularity), 10) * granularity;
}

requestBuilder.buildRequest = (query) => {
    const request = new messages.TraceCountsRequest();

    request.setFilterexpression(createFilterExpression(query));
    request.setStarttime(roundDownToGranularity(query.startTime, query.granularity));
    request.setEndtime(roundUpToGranularity(query.endTime, query.granularity));
    request.setInterval(parseInt(query.granularity, 10) || DEFAULT_INTERVAL_LIMIT);

    return request;
};

module.exports = requestBuilder;
