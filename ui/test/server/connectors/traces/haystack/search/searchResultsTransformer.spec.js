/*
 * Copyright 2019 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the License);
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an AS IS BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */

import {expect} from 'chai';

const searchResultsTransformer = require('../../../../../../server/connectors/traces/haystack/search/searchResultsTransformer');

describe('searchResultsTransformer.transform', () => {
    function getMockQuery() {
        const mockQuery = {
            useExpressionTree: 'true',
            spanLevelFilters: '[]',
            serviceName: 'frontend',
            startTime: '1568195881224000',
            endTime: '1568210236224000'
        };
        return mockQuery;
    }

    function getMockTraces() {
        const mockTraces = [
            [
                {
                    traceId: 'd847a696-fe9d-4df6-bcf6-f9f01cd46f21',
                    spanId: 'ba56f464-af74-4e1a-be61-70455741da4a',
                    parentSpanId: '',
                    serviceName: 'frontend',
                    operationName: 'callBackend',
                    startTime: 1568208053743000,
                    duration: 331000,
                    logs: [],
                    tags: []
                },
                {
                    traceId: 'd847a696-fe9d-4df6-bcf6-f9f01cd46f21',
                    spanId: 'd749d645-e446-4ed7-a874-8166d0027056',
                    parentSpanId: 'ba56f464-af74-4e1a-be61-70455741da4a',
                    serviceName: 'backend',
                    operationName: 'sayHello',
                    startTime: 1568208053895500,
                    duration: 79000,
                    logs: [],
                    tags: []
                }
            ]
        ];
        return mockTraces;
    }

    it('should transform input correctly', () => {
        // given
        const mockQuery = getMockQuery();
        const mockTraces = getMockTraces();

        // when
        const result = searchResultsTransformer.transform(mockTraces, mockQuery);

        // should
        const expected = [
            {
                traceId: 'd847a696-fe9d-4df6-bcf6-f9f01cd46f21',
                spanCount: 2,
                errorSpanCount: 0,
                services: [{name: 'frontend', spanCount: 1}, {name: 'backend', spanCount: 1}],
                root: {url: '', serviceName: 'frontend', operationName: 'callBackend', duration: 331000, error: false},
                queriedOperation: undefined,
                queriedService: {duration: 331000, durationPercent: 100, error: false},
                startTime: 1568208053743000,
                duration: 331000
            }
        ];
        expect(result).to.deep.equal(expected);
    });

    it('should find errors', () => {
        // given
        const mockQuery = getMockQuery();
        const mockTraces = getMockTraces();
        mockTraces[0][0].tags.push({
            key: 'error',
            value: true
        });
        mockTraces[0][1].tags.push({
            key: 'error',
            value: 'true'
        });
        // when
        const result = searchResultsTransformer.transform(mockTraces, mockQuery);

        // should
        expect(result[0]).to.have.property('errorSpanCount', 2);
        expect(result[0]).to.have.nested.property('root.error', true);
        expect(result[0]).to.have.nested.property('queriedService.error', true);
    });

    it('should find by operationName', () => {
        // given
        const mockQuery = getMockQuery();
        mockQuery.operationName = 'callBackend';
        const mockTraces = getMockTraces();

        // when
        const result = searchResultsTransformer.transform(mockTraces, mockQuery);

        // should
        expect(result[0]).to.have.nested.property('queriedOperation.duration', 331000);
        expect(result[0]).to.have.nested.property('queriedOperation.durationPercent', 100);
        expect(result[0]).to.have.nested.property('queriedOperation.error', false);
    });

    it('should default duration to 1 when end to end duration is 0', () => {
        // given
        const mockQuery = getMockQuery();
        const mockTraces = getMockTraces();
        mockTraces[0][0].startTime = 1568208053895500;
        mockTraces[0][1].startTime = 1568208053895500;
        mockTraces[0][0].endTime = 1568208053895500;
        mockTraces[0][1].endTime = 1568208053895500;
        mockTraces[0][0].duration = 0;
        mockTraces[0][1].duration = 0;

        // when
        const result = searchResultsTransformer.transform(mockTraces, mockQuery);

        // should
        expect(result[0]).to.have.nested.property('duration', 1);
    });
});
