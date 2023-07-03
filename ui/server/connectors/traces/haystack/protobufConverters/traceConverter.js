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
const _ = require('lodash');
const spanProto = require('../../../../../static_codegen/span_pb');

const converter = {};

function toTagJson(pbTag) {
    let tagValue = '';

    switch (pbTag.type) {
        case spanProto.Tag.TagType.STRING:
            tagValue = pbTag.vstr;
            break;
        case spanProto.Tag.TagType.DOUBLE:
            tagValue = pbTag.vdouble;
            break;
        case spanProto.Tag.TagType.BOOL:
            tagValue = pbTag.vbool;
            break;
        case spanProto.Tag.TagType.LONG:
            tagValue = pbTag.vlong;
            break;
        case spanProto.Tag.TagType.BYTES:
            tagValue = pbTag.vbytes;
            break;
        default:
            tagValue = '';
    }

    return {
        key: pbTag.key,
        value: tagValue
    };
}

function toLogJson(pbLog) {
    return {
        timestamp: pbLog.timestamp,
        fields: pbLog.fieldsList.map(pbTag => toTagJson(pbTag))
    };
}

converter.toSpanJson = pbSpan => ({
    traceId: pbSpan.traceid,
    spanId: pbSpan.spanid,
    parentSpanId: pbSpan.parentspanid,
    serviceName: pbSpan.servicename,
    operationName: pbSpan.operationname,
    startTime: pbSpan.starttime,
    duration: pbSpan.duration,
    logs: pbSpan.logsList && pbSpan.logsList.map(pbLog => toLogJson(pbLog)),
    tags: pbSpan.logsList && pbSpan.tagsList.map(pbTag => toTagJson(pbTag))
});

converter.toTraceJson = pbTrace => pbTrace.childspansList.map(pbSpan => converter.toSpanJson(pbSpan));

converter.toTracesJson = pbTraces => _.flatMap(pbTraces.tracesList, t => converter.toTraceJson(t).sort((s1, s2) => s1.startTime - s2.startTime));

module.exports = converter;
