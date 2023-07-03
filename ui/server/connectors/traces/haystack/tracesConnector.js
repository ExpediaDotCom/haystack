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

const grpc = require('grpc');

const messages = require('../../../../static_codegen/traceReader_pb');
const searchResultsTransformer = require('./search/searchResultsTransformer');
const callGraphResultTransformer = require('./protobufConverters/callGraphConverter');
const pbTraceConverter = require('./protobufConverters/traceConverter');
const pbTraceCountsConverter = require('./protobufConverters/traceCountsConverter');
const searchRequestBuilder = require('./search/searchRequestBuilder');
const traceCountsRequestBuilder = require('./timeline/traceCountsRequestBuilder');
const objectUtils = require('../../utils/objectUtils');
const fetcher = require('../../operations/grpcFetcher');
const config = require('../../../config/config');

const trendsConnector =
    config.connectors.trends &&
    config.connectors.trends.connectorName !== 'disabled' &&
    require(`../../trends/${config.connectors.trends.connectorName}/trendsConnector`); // eslint-disable-line import/no-dynamic-require, global-require

const services = require('../../../../static_codegen/traceReader_grpc_pb');

const grpcOptions = config.grpcOptions || {};

const client = new services.TraceReaderClient(
    `${config.connectors.traces.haystackHost}:${config.connectors.traces.haystackPort}`,
    grpc.credentials.createInsecure(),
    grpcOptions
); // TODO make client secure

const fieldValueFetcher = fetcher('getFieldValues', client);
const fieldNameFetcher = fetcher('getFieldNames', client);
const traceFetcher = fetcher('getTrace', client);
const rawTraceFetcher = fetcher('getRawTrace', client);
const rawTracesFetcher = fetcher('getRawTraces', client);
const rawSpanFetcher = fetcher('getRawSpan', client);
const tracesSearchFetcher = fetcher('searchTraces', client);
const traceCallGraphFetcher = fetcher('getTraceCallGraph', client);
const traceCountsFetcher = fetcher('getTraceCounts', client);
const connector = {};

connector.getServices = () => {
    const request = new messages.FieldValuesRequest();
    request.setFieldname('serviceName');

    return fieldValueFetcher
        .fetch(request)
        .then((result) => result.getValuesList())
        .then((result) =>
            result.filter((value) => {
                const servicesFilter = config.connectors.traces.servicesFilter;
                if (servicesFilter) {
                    for (let i = 0; i < servicesFilter.length; i += 1) {
                        if (servicesFilter[i].test(value)) {
                            return false;
                        }
                    }
                }
                return true;
            })
        );
};

connector.getSearchableKeys = () => {
    const request = new messages.Empty();

    return fieldNameFetcher.fetch(request).then((result) => {
        const fieldNamesWithMetadata = {};
        const names = result.getNamesList();
        const metadata = result.getFieldmetadataList();

        // create map with key as whitelisted field name
        names.forEach((name, index) => {
            fieldNamesWithMetadata[name] = {isRangeQuery: metadata[index] ? metadata[index].getIsrangequery() : false};
        });

        // additional keys which are not part of Index
        fieldNamesWithMetadata.traceId = {isRangeQuery: false};
        fieldNamesWithMetadata.serviceName = {isRangeQuery: false};
        fieldNamesWithMetadata.operationName = {isRangeQuery: false};
        fieldNamesWithMetadata.duration = {isRangeQuery: true, description: '(unit: microseconds)'};

        return fieldNamesWithMetadata;
    });
};

connector.getOperations = (serviceName) => {
    const service = new messages.Field();
    service.setName('serviceName');
    service.setValue(serviceName);

    const request = new messages.FieldValuesRequest();
    request.setFieldname('operationName');
    request.setFiltersList(new messages.Field());
    request.setFiltersList([service]);

    return fieldValueFetcher.fetch(request).then((result) => result.getValuesList());
};

connector.getTrace = (traceId) => {
    const request = new messages.TraceRequest();
    request.setTraceid(traceId);

    return traceFetcher.fetch(request).then((result) => pbTraceConverter.toTraceJson(messages.Trace.toObject(false, result)));
};

connector.findTraces = (query) => {
    const traceId = objectUtils.getPropIgnoringCase(JSON.parse(query.spanLevelFilters), 'traceId');

    if (traceId) {
        // if search is for a singe trace, perform getTrace instead of search
        const request = new messages.TraceRequest();
        request.setTraceid(traceId);

        return traceFetcher.fetch(request).then((result) => {
            const pbTrace = messages.Trace.toObject(false, result);
            const jsonTrace = pbTraceConverter.toTraceJson(pbTrace);

            return searchResultsTransformer.transform([jsonTrace], query);
        });
    }

    return tracesSearchFetcher.fetch(searchRequestBuilder.buildRequest(query)).then((result) => {
        const pbTraceResult = messages.TracesSearchResult.toObject(false, result);
        const jsonTraceResults = pbTraceResult.tracesList.map((pbTrace) => pbTraceConverter.toTraceJson(pbTrace));

        return searchResultsTransformer.transform(jsonTraceResults, query);
    });
};

connector.findTracesFlat = (query) => {
    const traceId = objectUtils.getPropIgnoringCase(JSON.parse(query.spanLevelFilters), 'traceId');

    if (traceId) {
        // if search is for a singe trace, perform getTrace instead of search
        const request = new messages.TraceRequest();
        request.setTraceid(traceId);

        return traceFetcher.fetch(request).then((result) => {
            const pbTrace = messages.Trace.toObject(false, result);
            return pbTraceConverter.toTraceJson(pbTrace);
        });
    }

    return tracesSearchFetcher.fetch(searchRequestBuilder.buildRequest(query)).then((result) => {
        const pbTraceResult = messages.TracesSearchResult.toObject(false, result);
        return pbTraceResult.tracesList.map((pbTrace) => pbTraceConverter.toTraceJson(pbTrace));
    });
};

connector.getRawTrace = (traceId) => {
    const request = new messages.TraceRequest();
    request.setTraceid(traceId);

    return rawTraceFetcher.fetch(request).then((result) => pbTraceConverter.toTraceJson(messages.Trace.toObject(false, result)));
};

connector.getRawTraces = (traceIds) => {
    const request = new messages.RawTracesRequest();
    request.setTraceidList(JSON.parse(traceIds));

    return rawTracesFetcher.fetch(request).then((result) => pbTraceConverter.toTracesJson(messages.RawTracesResult.toObject(false, result)));
};

connector.getRawSpan = (traceId, spanId, serviceName) => {
    const request = new messages.SpanRequest();
    request.setTraceid(traceId);
    request.setSpanid(spanId);
    return rawSpanFetcher.fetch(request).then((result) => {
        const spanResponse = messages.SpanResponse.toObject(false, result);
        const pbSpan = spanResponse.spansList.find((span) => span.servicename === serviceName);
        return pbTraceConverter.toSpanJson(pbSpan);
    });
};

connector.getLatencyCost = (traceId) => {
    const request = new messages.TraceRequest();
    request.setTraceid(traceId);

    return traceCallGraphFetcher.fetch(request).then((result) => {
        const latencyCost = callGraphResultTransformer.transform(messages.TraceCallGraph.toObject(false, result));
        const edges = latencyCost.map((e) => ({
            serviceName: e.from.serviceName,
            operationName: e.from.operationName
        }));

        return (
            trendsConnector &&
            trendsConnector.getEdgeLatency(edges).then((trends) => {
                if (trends && trends.length) {
                    const latencyCostTrends = callGraphResultTransformer.mergeTrendsWithLatencyCost(latencyCost, trends);
                    return {latencyCost, latencyCostTrends};
                }
                return {latencyCost};
            })
        );
    });
};

connector.getTimeline = (query) => traceCountsFetcher.fetch(traceCountsRequestBuilder.buildRequest(query)).then((result) => {
        const pbTraceCounts = messages.TraceCounts.toObject(false, result);
        return pbTraceCountsConverter.toTraceCountsJson(pbTraceCounts)
            .sort((a, b) => (a.x - b.x));
    });

module.exports = connector;
