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

const expressionTreeBuilder = require('../expressionTreeBuilder');

const requestBuilder = {};
const messages = require('../../../../../static_codegen/traceReader_pb');

const DEFAULT_RESULTS_LIMIT = 25;

requestBuilder.buildRequest = (query) => {
    const request = new messages.TracesSearchRequest();

    request.setFilterexpression(expressionTreeBuilder.createFilterExpression(query));
    request.setStarttime(parseInt(query.startTime, 10));
    request.setEndtime(parseInt(query.endTime, 10));
    request.setLimit(parseInt(query.limit, 10) || DEFAULT_RESULTS_LIMIT);

    return request;
};

module.exports = requestBuilder;
