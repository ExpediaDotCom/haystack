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
const searchResultsTransformer = require('../haystack/search/searchResultsTransformer');

// NOTICE: This converter was originally ported from the following ASL 2.0 code:
// https://github.com/openzipkin/zipkin/blob/6fbef6bcfc84e721215c1037771300643eb1b0ed/zipkin-ui/js/spanConverter.js
function toHaystackLog(annotation) {
  return {
    timestamp: annotation.timestamp,
    fields: [
      {
        key: 'event',
        value: annotation.value
      }
    ]
  };
}

function normalizeTraceId(traceId) {
  if (traceId.length > 16) {
    return traceId.padStart(32, '0');
  }
  return traceId.padStart(16, '0');
}

// NOTE: 'not_found' is different than Zipkin's 'unknown' default
function sanitizeName(name) {
  return (name && name !== '' && name !== 'unknown') ? name : 'not_found';
}

// Note: the tag 'success' is not something defined in Zipkin, nor commonly used
function convertSuccessTag(tags) {
  const successTag = tags.find(tag => tag.key.toLowerCase() === 'success');
  if (successTag) {
    successTag.key = 'error';
    successTag.value = successTag.value === 'false' ? 'true' : 'false';
  }
}

// Note: the tag 'methoduri' is not something defined in Zipkin, nor commonly used
function convertMethodUriTag(tags) {
  const methodUriTag = tags.find(tag => tag.key.toLowerCase() === 'methoduri');
  if (methodUriTag) {
    methodUriTag.key = 'url';
  }
}

function toHaystackSpan(span) {
  const res = {
    traceId: normalizeTraceId(span.traceId)
  };

  // take care not to create self-referencing spans even if the input data is incorrect
  const id = span.id.padStart(16, '0');
  if (span.parentId) {
    const parentId = span.parentId.padStart(16, '0');
    if (parentId !== id) {
      res.parentSpanId = parentId;
    }
  }

  res.spanId = id;

  if (span.localEndpoint) {
    res.serviceName = sanitizeName(span.localEndpoint.serviceName);
  } else {
    res.serviceName = 'not_found';
  }

  res.operationName = sanitizeName(span.name);

  // Don't report timestamp and duration on shared spans (should be server, but not necessarily)
  if (!span.shared) {
    if (span.timestamp) res.startTime = span.timestamp;
    if (span.duration) res.duration = span.duration;
  }

  let startTs = span.timestamp || 0;
  let endTs = startTs && span.duration ? startTs + span.duration : 0;
  let msTs = 0;
  let wsTs = 0;
  let wrTs = 0;
  let mrTs = 0;

  let begin;
  let end;

  let kind = span.kind;

  // scan annotations in case there are better timestamps, or inferred kind
  (span.annotations || []).forEach((a) => {
    switch (a.value) {
      case 'cs':
        kind = 'CLIENT';
        if (a.timestamp < startTs) startTs = a.timestamp;
        break;
      case 'sr':
        kind = 'SERVER';
        if (a.timestamp < startTs) startTs = a.timestamp;
        break;
      case 'ss':
        kind = 'SERVER';
        if (a.timestamp > endTs) endTs = a.timestamp;
        break;
      case 'cr':
        kind = 'CLIENT';
        if (a.timestamp > endTs) endTs = a.timestamp;
        break;
      case 'ms':
        kind = 'PRODUCER';
        msTs = a.timestamp;
        break;
      case 'mr':
        kind = 'CONSUMER';
        mrTs = a.timestamp;
        break;
      case 'ws':
        wsTs = a.timestamp;
        break;
      case 'wr':
        wrTs = a.timestamp;
        break;
      default:
    }
  });

  let remoteAddressTag;
  switch (kind) {
    case 'CLIENT':
      remoteAddressTag = 'server.service_name';
      begin = 'cs';
      end = 'cr';
      break;
    case 'SERVER':
      remoteAddressTag = 'client.service_name';
      begin = 'sr';
      end = 'ss';
      break;
    case 'PRODUCER':
      remoteAddressTag = 'broker.service_name';
      begin = 'ms';
      end = 'ws';
      if (startTs === 0 || (msTs !== 0 && msTs < startTs)) {
        startTs = msTs;
      }
      if (endTs === 0 || (wsTs !== 0 && wsTs > endTs)) {
        endTs = wsTs;
      }
      break;
    case 'CONSUMER':
      remoteAddressTag = 'broker.service_name';
      if (startTs === 0 || (wrTs !== 0 && wrTs < startTs)) {
        startTs = wrTs;
      }
      if (endTs === 0 || (mrTs !== 0 && mrTs > endTs)) {
        endTs = mrTs;
      }
      if (endTs !== 0 || wrTs !== 0) {
        begin = 'wr';
        end = 'mr';
      } else {
        begin = 'mr';
      }
      break;
    default:
  }

  const beginAnnotation = startTs && begin;
  const endAnnotation = endTs && end;

  res.logs = []; // prefer empty to undefined for arrays

  if (beginAnnotation) {
    res.logs.push(toHaystackLog({
      value: begin,
      timestamp: startTs
    }));
  }

  (span.annotations || []).forEach((a) => {
    if (beginAnnotation && a.value === begin) return;
    if (endAnnotation && a.value === end) return;
    res.logs.push(toHaystackLog(a));
  });

  if (endAnnotation) {
    res.logs.push(toHaystackLog({
      value: end,
      timestamp: endTs
    }));
  }

  res.tags = []; // prefer empty to undefined for arrays
  const keys = Object.keys(span.tags || {});
  if (keys.length > 0) {
    res.tags = keys.map(key => ({
      key,
      value: span.tags[key]
    }));

    // handle special tags defined by Haystack
    convertSuccessTag(res.tags);
    convertMethodUriTag(res.tags);
  }

  if (span.remoteEndpoint) {
    const remoteService = sanitizeName(span.remoteEndpoint.serviceName);
    if (remoteService !== 'not_found') {
      res.tags.push({
        key: remoteAddressTag || 'remote.service_name',
        value: remoteService
      });
    }
  }

  return res;
}
/*
 * Instrumentation should set span.startTime when recording a span so that guess-work
 * isn't needed. Since a lot of instrumentation don't, we have to make some guesses.
 *
 * * If there is a 'cs', use that
 * * Fall back to 'sr'
 * * Otherwise, return undefined
 */
// originally zipkin.internal.ApplyTimestampAndDuration.guessTimestamp
function guessTimestamp(span) {
  if (span.startTime || span.logs.length === 0) {
    return span.startTime;
  }
  let rootServerRecv;
  for (let i = 0; i < span.logs.length; i += 1) {
    const a = span.logs[i];
    if (a.fields[0].value === 'cs') {
      return a.timestamp;
    } else if (a.fields[0].value === 'sr') {
      rootServerRecv = a.timestamp;
    }
  }
  return rootServerRecv;
}

/*
 * For RPC two-way spans, the duration between 'cs' and 'cr' is authoritative. RPC one-way spans
 * lack a response, so the duration is between 'cs' and 'sr'. We special-case this to avoid
 * setting incorrect duration when there's skew between the client and the server.
 */
// originally zipkin.internal.ApplyTimestampAndDuration.apply
function applyTimestampAndDuration(span) {
  const logsLength = span.logs.length;
  // Don't overwrite authoritatively set startTime and duration!
  if ((span.startTime && span.duration) || logsLength === 0) {
    return span;
  }

  // We cannot backfill duration on a span with less than two logs. However, we
  // can backfill timestamp.
  if (logsLength < 2) {
    if (span.startTime) return span;
    const guess = guessTimestamp(span);
    if (!guess) return span;
    span.startTime = guess; // eslint-disable-line no-param-reassign
    return span;
  }

  // Prefer RPC one-way (cs -> sr) vs arbitrary annotations.
  let first = span.logs[0].timestamp;
  let last = span.logs[logsLength - 1].timestamp;
  span.logs.forEach((a) => {
    if (a.fields[0].value === 'cs') {
      first = a.timestamp;
    } else if (a.fields[0].value === 'cr') {
      last = a.timestamp;
    }
  });

  if (!span.startTime) {
    span.startTime = first; // eslint-disable-line no-param-reassign
  }
  if (!span.duration && last !== first) {
    span.duration = last - first; // eslint-disable-line no-param-reassign
  }
  return span;
}

// This guards to ensure we don't add duplicate logs on merge
function maybePushHaystackLog(annotations, a) {
  if (annotations.findIndex(b => a.fields[0].value === b.fields[0].value) === -1) {
    annotations.push(a);
  }
}

// This guards to ensure we don't add duplicate tags on merge
function maybePushHaystackTag(tags, a) {
  if (tags.findIndex(b => a.key === b.key) === -1) {
    tags.push(a);
  }
}

function merge(left, right) {
  const res = {
    traceId: right.traceId.length > 16 ? right.traceId : left.traceId
  };

  if (left.parentSpanId) {
    res.parentSpanId = left.parentSpanId;
  } else if (right.parentSpanId) {
    res.parentSpanId = right.parentSpanId;
  }

  res.spanId = left.spanId;

  // When we move to span model 2, remove this code in favor of using Span.kind == CLIENT
  let leftClientSpan;
  let rightClientSpan;
  let rightServerSpan;

  const logs = [];

  (left.logs || []).forEach((a) => {
    if (a.fields[0].value === 'cs') leftClientSpan = true;
    maybePushHaystackLog(logs, a);
  });

  (right.logs || []).forEach((a) => {
    if (a.fields[0].value === 'cs') rightClientSpan = true;
    if (a.fields[0].value === 'sr') rightServerSpan = true;
    maybePushHaystackLog(logs, a);
  });

  res.operationName = left.operationName;
  if (right.operationName !== 'not_found') {
    if (res.operationName === 'not_found') {
      res.operationName = right.operationName;
    } else if (leftClientSpan && rightServerSpan) {
      res.operationName = right.operationName; // prefer the server's span name
    }
  }

  res.serviceName = left.serviceName;
  if (right.serviceName !== 'not_found') {
    if (res.serviceName === 'not_found') {
      res.serviceName = right.serviceName;
    } else if (leftClientSpan && rightServerSpan) {
      res.serviceName = right.serviceName; // prefer the server's service name
    }
  }

  res.logs = logs.sort((a, b) => a.timestamp - b.timestamp);

  res.tags = [];

  (left.tags || []).forEach((b) => {
    maybePushHaystackTag(res.tags, b);
  });

  (right.tags || []).forEach((b) => {
    maybePushHaystackTag(res.tags, b);
  });

  // Single timestamp makes duration easy: just choose max
  if (!left.startTime || !right.startTime || left.startTime === right.startTime) {
    res.startTime = left.startTime || right.startTime;
    if (!left.duration) {
      res.duration = right.duration;
    } else if (right.duration) {
      res.duration = Math.max(left.duration, right.duration);
    } else {
      res.duration = left.duration;
    }

  // We have 2 different timestamps. If we have client data in either one of them, use right,
  // else set timestamp and duration to null
  } else if (rightClientSpan) {
    res.startTime = right.startTime;
    res.duration = right.duration;
  } else if (leftClientSpan) {
    res.startTime = left.startTime;
    res.duration = left.duration;
  }

  return res;
}

/*
 * Zipkin spans can be sent in multiple parts. Also client and server spans can
 * share the same ID. This merges both scenarios.
 */
// originally zipkin.internal.MergeById.apply
function mergeById(spans) {
  const result = [];

  if (!spans || spans.length === 0) return result;

  const spanIdToSpans = {};
  spans.forEach((s) => {
    const id = s.spanId;
    spanIdToSpans[id] = spanIdToSpans[id] || [];
    spanIdToSpans[id].push(s);
  });

  Object.keys(spanIdToSpans).forEach((id) => {
    const spansToMerge = spanIdToSpans[id];
    let left = spansToMerge[0];
    for (let i = 1; i < spansToMerge.length; i += 1) {
      left = merge(left, spansToMerge[i]);
    }

    // attempt to get a timestamp so that the UI can sort results
    result.push(applyTimestampAndDuration(left));
  });

  return result;
}

const converter = {};

// exported for testing
converter.toHaystackSpan = toHaystackSpan;
converter.applyTimestampAndDuration = applyTimestampAndDuration;
converter.merge = merge;
converter.mergeById = mergeById;

// NOTE: unlike Zipkin UI, this neither sorts, nor corrects clock skew in the
// results. Not sure what is in scope of the haystack UI logic.
converter.toHaystackTrace = zipkinTrace =>
  mergeById(zipkinTrace.map(zipkinSpan => toHaystackSpan(zipkinSpan)));

converter.toHaystackSearchResult = (zipkinTraces, query) => {
  const haystackTraces = zipkinTraces.map(zipkinTrace => converter.toHaystackTrace(zipkinTrace));
  return searchResultsTransformer.transform(haystackTraces, query);
};

module.exports = converter;
