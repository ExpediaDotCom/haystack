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
import axios from 'axios';
import _ from 'lodash';
import {observable, action, computed} from 'mobx';
import { fromPromise } from 'mobx-utils';

import { ErrorHandlingStore } from '../../../stores/errorHandlingStore';
import {toDurationMicroseconds} from '../utils/presets';
import {toQueryUrlString} from '../../../utils/queryParser';

export function setChildExpandState(timelineSpans, parent, shouldCollapseWaterfall) {
    parent.children.forEach((childId) => {
        let showExpanded = parent.expanded;
        let displaySpans = parent.expanded;
        if (shouldCollapseWaterfall && parent.expanded) {
            showExpanded = false;
            displaySpans = true;
        }
        const childSpan = timelineSpans.find(s => s.spanId === childId);
        childSpan.display = displaySpans;
        childSpan.expanded = showExpanded;
        setChildExpandState(timelineSpans, childSpan, shouldCollapseWaterfall);
    });
}

export function formatResults(results) {
    return results.map((result) => {
        const flattenedResult = {...result};
        flattenedResult.rootUrl = result.root.url;
        flattenedResult.serviceName = result.root.serviceName;
        flattenedResult.operationName = result.root.operationName;
        flattenedResult.rootError = result.root.error;
        flattenedResult.operationDuration = result.queriedOperation && result.queriedOperation.duration;
        flattenedResult.operationError = result.queriedOperation && result.queriedOperation.error;
        flattenedResult.operationDurationPercent = result.queriedOperation && result.queriedOperation.durationPercent;
        flattenedResult.serviceDuration = result.queriedService && result.queriedService.duration;
        flattenedResult.serviceDurationPercent = result.queriedService && result.queriedService.durationPercent;

        return flattenedResult;
    });
}

function createSpanTree(span, trace, groupByParentId = null) {
    const spansWithParent = _.filter(trace, s => s.parentSpanId);
    const grouped = groupByParentId !== null ? groupByParentId : _.groupBy(spansWithParent, s => s.parentSpanId);
    return {
        span,
        children: (grouped[span.spanId] || [])
            .map(s => createSpanTree(s, trace, grouped))
    };
}

function createFlattenedSpanTree(spanTree, depth, traceStartTime, totalDuration, shouldCollapseWaterfall) {
    let showExpanded = true;
    let displaySpans = true;
    if (shouldCollapseWaterfall) {
        if (depth === 1) {
            showExpanded = false;
            displaySpans = true;
        } else if (depth > 1) {
            displaySpans = false;
        }
    }

    return [observable({
        ...spanTree.span,
        children: spanTree.children.map(child => child.span.spanId),
        startTimePercent: (((spanTree.span.startTime - traceStartTime) / totalDuration) * 100),
        depth,
        expandable: !!spanTree.children.length,
        display: displaySpans,
        expanded: showExpanded
    })]
        .concat(_.flatMap(spanTree.children, child => createFlattenedSpanTree(child, depth + 1, traceStartTime, totalDuration, shouldCollapseWaterfall)));
}

export class TraceDetailsStore extends ErrorHandlingStore {
    static maxSpansBeforeCollapse = 100;
    @observable promiseState = null;
    @observable spans = [];

    @observable relatedTracesPromiseState = { case: ({empty}) => empty() };
    @observable searchQuery = {};
    @observable apiQuery = {};
    @observable relatedTraces = [];

    traceId = null;

    @action
    rejectRelatedTracesPromise(message) {
        this.relatedTracesPromiseState = fromPromise.reject(message);
    }

    @action
    fetchTraceDetails(traceId) {
        if (traceId === this.traceId) return;
        this.traceId = traceId;

        this.promiseState = fromPromise(
            axios
                .get(`/api/trace/${traceId}`)
                .then((result) => {
                    this.spans = result.data;
                })
                .catch((result) => {
                    TraceDetailsStore.handleError(result);
                })
        );
    }

    @action
    uploadSpans(spans) {
        this.spans = spans;
    }

    @action
    fetchRelatedTraces(query) {
        const serviceName = decodeURIComponent(query.serviceName);
        const operationName = (!query.operationName || query.operationName === 'all') ? null : decodeURIComponent(query.operationName);
        const startTime = query.startTime ? query.startTime * 1000 : ((Date.now() * 1000) - toDurationMicroseconds(query.timePreset));
        const ARTIFICIAL_DELAY = 45 * 1000;  // artificial delay of 45 sec to get completed traces
        const endTime = query.endTime ? query.endTime * 1000 : (Date.now() - ARTIFICIAL_DELAY) * 1000;

        const apiQuery = {...query,
            serviceName,
            operationName,
            startTime,
            endTime,
            timePreset: null
        };

        const queryUrlString = toQueryUrlString(apiQuery);

        this.fetchTraceResults(queryUrlString);
        this.searchQuery = query;
        this.apiQuery = apiQuery;
    }

    @action
    fetchTraceResults(queryUrlString) {
        this.relatedTracesPromiseState = fromPromise(
            axios
            .get(`/api/traces?${queryUrlString}`)
            .then((result) => {
                this.relatedTraces = formatResults(result.data);
            })
            .catch((result) => {
                this.relatedTraces = [];
                TraceDetailsStore.handleError(result);
            })
        );
    }

    // Returns an object with the tag key value of the current trace
    // When it is generated, if two tags of teh spans of the trace have the same key but different values,
    // one value will simply overwrite the other, something that can be improved on in the future. As a
    // result, tags that have multiple values across the spans of the trace will have 'randomly' selected values.
    @computed
    get tags() {
        let tags = [];
        this.spans.forEach((span) => { tags = _.union(tags, span.tags); }); // Create a union of tags of all spans

        return tags.reduce((result, keyValuePair) => {
            result[keyValuePair.key.toLowerCase()] = keyValuePair.value; // eslint-disable-line no-param-reassign
            return result;
        }, {});
    }

    @computed
    get rootSpan() {
        return this.spans.find(span => !span.parentSpanId);
    }

    @computed
    get startTime() {
        return this.spans.reduce(
            (earliestTime, span) => (earliestTime ? Math.min(earliestTime, span.startTime) : span.startTime),
            null);
    }

    @computed
    get totalDuration() {
        const end = this.spans.reduce((latestTime, span) =>
            (latestTime ? Math.max(latestTime, (span.startTime + span.duration)) : (span.startTime + span.duration)), null
        );
        const difference = end - this.startTime;
        return difference || 1;
    }

    @computed
    get timelineSpans() {
        if (this.spans.length === 0) return [];

        const tree = createSpanTree(this.rootSpan, this.spans);
        return createFlattenedSpanTree(tree, 0, this.startTime, this.totalDuration, this.spans.length > TraceDetailsStore.maxSpansBeforeCollapse);
    }

    @computed
    get maxDepth() {
        return this.timelineSpans.reduce((max, span) => Math.max(max, span.depth), 0);
    }

    @action
    toggleExpand(selectedParentId) {
        const parent = this.timelineSpans.find(s => s.spanId === selectedParentId);
        parent.expanded = !parent.expanded;
        setChildExpandState(this.timelineSpans, parent, this.timelineSpans.length > TraceDetailsStore.maxSpansBeforeCollapse);
    }
}

export default new TraceDetailsStore();
