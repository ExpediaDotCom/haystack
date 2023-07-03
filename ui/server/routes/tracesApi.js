/*
 *  Copyright 2018 Expedia Group
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

const express = require('express');

const config = require('../config/config');
const handleResponsePromise = require('./utils/apiResponseHandler').handleResponsePromise;

const tracesConnector = require(`../connectors/traces/${config.connectors.traces.connectorName}/tracesConnector`); // eslint-disable-line import/no-dynamic-require

const router = express.Router();

router.get('/traces', (req, res, next) => {
    handleResponsePromise(res, next, 'traces')(
        () => tracesConnector.findTraces(req.query)
    );
});

router.get('/traces/raw', (req, res, next) => {
    handleResponsePromise(res, next, 'traces_raw')(
        () => tracesConnector.getRawTraces(req.query.traceIds)
    );
});

router.get('/traces/timeline', (req, res, next) => {
    handleResponsePromise(res, next, 'trace_timeline')(
        () => tracesConnector.getTimeline(req.query)
    );
});

router.get('/traces/searchableKeys', (req, res, next) => {
    handleResponsePromise(res, next, 'trace_searchableKeys')(
        () => tracesConnector.getSearchableKeys()
    );
});

router.get('/trace/:traceId', (req, res, next) => {
    handleResponsePromise(res, next, 'trace_TRACEID')(
        () => tracesConnector.getTrace(req.params.traceId)
    );
});

router.get('/trace/raw/:traceId', (req, res, next) => {
    handleResponsePromise(res, next, 'trace_raw_TRACEID')(
        () => tracesConnector.getRawTrace(req.params.traceId)
    );
});

router.get('/trace/raw/:traceId/:spanId', (req, res, next) => {
    handleResponsePromise(res, next, 'trace_raw_TRACEID_SPANID')(
        () => tracesConnector.getRawSpan(req.params.traceId, req.params.spanId, req.query.serviceName)
    );
});

router.get('/trace/:traceId/latencyCost', (req, res, next) => {
    handleResponsePromise(res, next, 'trace_TRACEID_latencyCost')(
        () => tracesConnector.getLatencyCost(req.params.traceId)
    );
});

module.exports = router;
