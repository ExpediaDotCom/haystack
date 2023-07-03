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

const Q = require('q');
const _ = require('lodash');
const objectUtils = require('../../utils/objectUtils');

function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return `${s4()}${s4()}-${s4()}-${s4()}-${s4()}-${s4()}${s4()}${s4()}`;
}

const trace = () => {
    const traceId = guid();
    const span1 = guid();
    const span2 = guid();
    const span3 = guid();
    const span4 = guid();
    const span5 = guid();
    const span6 = guid();
    const span7 = guid();
    const span8 = guid();
    const span9 = guid();
    const span10 = guid();
    const span11 = guid();
    const span12 = guid();

    return [
        {
            traceId,
            spanId: span1,
            serviceName: 'stark-service',
            operationName: 'snow-1',
            startTime: 1504784384000,
            duration: 3525000,
            logs: [],
            tags: [
                {
                    key: 'url',
                    value: 'http://trace.io/blah'
                },
                {
                    key: 'url2',
                    value: 'some:data'
                },
                {
                    key: 'error',
                    value: false
                },
                {
                    key: 'request-blob',
                    value: 'test-blob-client_message_4d35d44f-719e-4196-a784-7b4d506db6de_response_77b56acc-91d6-48fd-96ce-5f66de8fc7e7'
                },
                {
                    key: 'response-blob',
                    value: 'test-blob-client_message_4d35d44f-719e-4196-a784-7b4d506db6de_response_77b56acc-91d6-48fd-96ce-5f66de8fc7e7'
                }
            ]
        },
        {
            traceId,
            parentSpanId: span1,
            spanId: span2,
            serviceName: 'westeros-service',
            operationName: 'mormont-1',
            startTime: 1504784384000 + 250000,
            duration: 1505000,
            logs: [],
            tags: [
                {
                    key: 'url',
                    value: 'http://trace.io/blah'
                },
                {
                    key: 'error',
                    value: true
                },
                {
                    key: 'url2',
                    value: 'some:data'
                },
                {
                    key: 'request-blob',
                    value: 'test-blob-client_message_4d35d44f-719e-4196-a784-7b4d506db6de_response_77b56acc-91d6-48fd-96ce-5f66de8fc7e7'
                },
                {
                    key: 'response-blob',
                    value: 'test-blob-client_message_4d35d44f-719e-4196-a784-7b4d506db6de_response_77b56acc-91d6-48fd-96ce-5f66de8fc7e7'
                },
                {
                    key: 'url3',
                    value: 'http://trace.io/blah'
                },
                {
                    key: 'url4',
                    value: 'some:data'
                },
                {
                    key: 'url5',
                    value: 'http://trace.io/blah'
                },
                {
                    key: 'url6',
                    value: 'some:data'
                },
                {
                    key: 'url7',
                    value: 'http://trace.io/blah'
                },
                {
                    key: 'url8',
                    value: 'some:data'
                }
            ]
        },
        {
            traceId,
            parentSpanId: span2,
            spanId: span3,
            serviceName: 'tyrell-service',
            operationName: 'tully-1',
            startTime: 1504784384000 + 250000 + 120000,
            duration: 605000,
            logs: [],
            tags: [
                {
                    key: 'blob-request',
                    value: '/getBlob/tyrell-service/tully-1_9ff6c0cf-03ba-4675-991f-5dfbbf45af03'
                },
                {
                    key: 'blob-response',
                    value: '/getBlob/tyrell-service/tully-1_9ff6c0cf-03ba-4675-991f-5dfbbf45af03'
                }
            ]
        },
        {
            traceId,
            parentSpanId: span2,
            spanId: span4,
            serviceName: 'dragon-service',
            operationName: 'drogo-1',
            startTime: 1504784384000 + 250000 + 680000,
            duration: 645000,
            logs: [],
            tags: [
                {
                    key: 'error',
                    value: 'true'
                }
            ]
        },
        {
            traceId,
            parentSpanId: span2,
            spanId: span5,
            serviceName: 'dragon-service',
            operationName: 'grayjoy-1',
            startTime: 1504784384000 + 250000 + 680000,
            duration: 805000,
            logs: [],
            tags: [
                {
                    key: 'error',
                    value: '500'
                }
            ]
        },
        {
            traceId,
            parentSpanId: span5,
            spanId: span6,
            serviceName: 'blackwater-service',
            operationName: 'clegane-1',
            startTime: 1504784384000 + 250000 + 920000,
            duration: 675000,
            logs: [],
            tags: []
        },
        {
            traceId,
            parentSpanId: span1,
            spanId: span7,
            serviceName: 'baratheon-service',
            operationName: 'dondarrion-1',
            startTime: 1504784384000 + 1760000,
            duration: 834000,
            logs: [],
            tags: []
        },
        {
            traceId,
            parentSpanId: span7,
            spanId: span8,
            serviceName: 'blackwater-service',
            operationName: 'grayjoy-1',
            startTime: 1504784384000 + 1960000,
            duration: 234000,
            logs: [],
            tags: []
        },
        {
            traceId,
            parentSpanId: span1,
            spanId: span9,
            serviceName: 'westeros-service',
            operationName: 'tarley-1',
            startTime: 1504784384000 + 2560000 + 105000,
            duration: 105000,
            logs: [],
            tags: [
                {
                    key: 'external-link-key',
                    value: 'external-link-value'
                }
            ]
        },
        {
            traceId,
            parentSpanId: span1,
            spanId: span10,
            serviceName: 'westeros-service',
            operationName: 'snow-1',
            startTime: 1504784384000 + 2560000 + 105000,
            duration: 505000,
            logs: [],
            tags: []
        },
        {
            traceId,
            parentSpanId: span1,
            spanId: span11,
            serviceName: 'westeros-service',
            operationName: 'tarley-1',
            startTime: 1504784384000 + 2560000 + 105000,
            duration: 505000 + 225000,
            logs: [],
            tags: []
        },
        {
            traceId,
            parentSpanId: span1,
            spanId: span12,
            serviceName: 'westeros-service',
            operationName: 'dondarrion-1',
            startTime: 1504784384000 + 2560000 + 105000 + 505000 + 225000,
            duration: 150000,
            logs: [],
            tags: [
                {
                    key: 'error',
                    value: true
                }
            ]
        }
    ];
};

const connector = {};

connector.getServices = () =>
    Q.fcall(() => [
        'root-service',
        'lannister-service',
        'stark-service',
        'tyrell-service',
        'targaryen-service',
        'baratheon-service',
        'dragon-service',
        'westeros-service'
    ]);

connector.getOperations = () =>
    Q.fcall(() => [
        'mormont-1',
        'seaworth-1',
        'bolton-1',
        'baelish-1',
        'snow-1',
        'tully-1',
        'dondarrion-1',
        'grayjoy-1',
        'clegane-1',
        'drogo-1',
        'tarley-1'
    ]);

function getValue(min, max) {
    return _.round(Math.random() * (max - min) + min, 0);
}

function getRandomValues(granularity, dataPoints, from) {
    const valuesArr = [];
    _.range(dataPoints).forEach((i) => valuesArr.push({x: from + i * granularity, y: getValue(0, 3000)}));
    return valuesArr;
}

connector.getTimeline = (query) =>
    Q.fcall(() => {
        const granularity = query.granularity || (query.endTime - query.startTime) / 15;
        const range = query.endTime - query.startTime;
        const points = range / granularity;

        return getRandomValues(granularity, points, parseInt(query.startTime, 10));
    });

connector.getSearchableKeys = () =>
    Q.fcall(() => ({
        serviceName: {isRangeQuery: false},
        operationName: {isRangeQuery: false},
        traceId: {isRangeQuery: false},
        error: {isRangeQuery: false, values: ['true', 'false']},
        duration: {isRangeQuery: true, description: '(unit: microseconds)'},
        guid: {isRangeQuery: false},
        testid: {isRangeQuery: false}
    }));

const latencyCost = {
    latencyCost: [
        {
            from: {
                serviceName: 'stark-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            networkDelta: 65
        },
        {
            from: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'tyrell-service',
                infrastructureProvider: '',
                infrastructureLocation: ''
            },
            networkDelta: null
        },
        {
            from: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'dragon-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-1'
            },
            networkDelta: 55
        },
        {
            from: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'dragon-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-1'
            },
            networkDelta: 64
        },
        {
            from: {
                serviceName: 'dragon-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-1'
            },
            to: {
                serviceName: 'blackwater-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-east-2'
            },
            networkDelta: 121
        },
        {
            from: {
                serviceName: 'stark-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'baratheon-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-east-1'
            },
            networkDelta: 180
        },
        {
            from: {
                serviceName: 'baratheon-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-east-1'
            },
            to: {
                serviceName: 'blackwater-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-east-1'
            },
            networkDelta: 109
        },
        {
            from: {
                serviceName: 'stark-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            networkDelta: 99
        },
        {
            from: {
                serviceName: 'stark-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            networkDelta: 128
        },
        {
            from: {
                serviceName: 'stark-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            networkDelta: 77
        },
        {
            from: {
                serviceName: 'stark-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-3'
            },
            networkDelta: 98
        }
    ],
    latencyCostTrends: [
        {
            from: {
                serviceName: 'stark-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            tp99NetworkDelta: 333,
            meanNetworkDelta: 21
        },
        {
            from: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'tyrell-service',
                infrastructureProvider: '',
                infrastructureLocation: ''
            },
            tp99NetworkDelta: 1031,
            meanNetworkDelta: 310
        },
        {
            from: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'dragon-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-1'
            },
            tp99NetworkDelta: 198,
            meanNetworkDelta: 88
        },
        {
            from: {
                serviceName: 'dragon-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-1'
            },
            to: {
                serviceName: 'blackwater-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-east-2'
            },
            tp99NetworkDelta: 355,
            meanNetworkDelta: 301
        },
        {
            from: {
                serviceName: 'stark-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'baratheon-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-east-1'
            },
            tp99NetworkDelta: 34,
            meanNetworkDelta: 21
        },
        {
            from: {
                serviceName: 'baratheon-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-east-1'
            },
            to: {
                serviceName: 'blackwater-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-east-1'
            },
            tp99NetworkDelta: 50,
            meanNetworkDelta: 31
        },
        {
            from: {
                serviceName: 'stark-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-2'
            },
            to: {
                serviceName: 'westeros-service',
                infrastructureProvider: 'aws',
                infrastructureLocation: 'us-west-3'
            },
            tp99NetworkDelta: 46,
            meanNetworkDelta: 45
        }
    ]
};

connector.getLatencyCost = () => Q.fcall(() => latencyCost);

connector.getTrace = () => Q.fcall(() => trace());

connector.getRawTrace = () => Q.fcall(() => trace());

connector.getRawSpan = () => Q.fcall(() => trace()[0]);

connector.getRawTraces = () => Q.fcall(() => [...trace(), ...trace()]);

connector.findTraces = (query) =>
    Q.fcall(() => {
        const traceId = query.spanLevelFilters && objectUtils.getPropIgnoringCase(JSON.parse(query.spanLevelFilters), 'traceId');

        if (traceId) {
            return [
                {
                    traceId: '380965e5-e0c4-4c37-91a7-da79def7597b',
                    spanCount: 12,
                    errorSpanCount: 2,
                    services: [
                        {
                            name: 'stark-service',
                            spanCount: 1
                        },
                        {
                            name: 'tyrell-service',
                            spanCount: 29
                        }
                    ],
                    root: {
                        url: '/stark/endpoint',
                        serviceName: 'stark-service',
                        operationName: 'snow-1',
                        duration: 3404000,
                        error: false
                    },
                    queriedService: {
                        duration: 31000,
                        durationPercent: 64,
                        error: true
                    },
                    queriedOperation: {
                        duration: 1,
                        durationPercent: 0,
                        error: false
                    },
                    startTime: new Date().getTime() * 1000,
                    duration: 390000
                }
            ];
        }
        return [
            {
                traceId: 'x00245a5-g0c4-4c37-55a7-da83def7127a',
                spanCount: 34,
                errorSpanCount: 2,
                services: [
                    {
                        name: 'stark-service',
                        spanCount: 16
                    },
                    {
                        name: 'targaryen-service',
                        spanCount: 18
                    }
                ],
                root: {
                    url: '/stark/endpoint',
                    serviceName: 'stark-service',
                    operationName: 'snow-1',
                    duration: 3404000,
                    error: false
                },
                queriedService: {
                    duration: 23000,
                    durationPercent: 99,
                    error: false
                },
                queriedOperation: {
                    duration: 1,
                    durationPercent: 0,
                    error: false
                },
                startTime: new Date().getTime() * 1000 - 10 * 1000 * 1000,
                duration: 240000
            },
            {
                traceId: 'a40165e5-e0c4-4c51-11x7-bb79def7597a',
                spanCount: 34,
                errorSpanCount: 2,
                services: [
                    {
                        name: 'stark-service',
                        spanCount: 1
                    },
                    {
                        name: 'rob-service',
                        spanCount: 8
                    }
                ],
                root: {
                    url: '/rob/endpoint',
                    serviceName: 'rob-service',
                    operationName: 'mormont-1',
                    duration: 3404000,
                    error: false
                },
                queriedService: {
                    duration: 590000,
                    durationPercent: 64,
                    error: false
                },
                queriedOperation: {
                    duration: 1,
                    durationPercent: 0,
                    error: false
                },
                startTime: new Date().getTime() * 1000 - 15 * 1000 * 1000,
                duration: 850000
            },
            {
                traceId: 'a80921e5-e0c4-4c37-91a7-da79def7597a',
                spanCount: 44,
                services: [
                    {
                        name: 'tyrell-service',
                        spanCount: 22
                    },
                    {
                        name: 'renly-service',
                        spanCount: 22
                    }
                ],
                root: {
                    url: '/baratheon/endpoint',
                    serviceName: 'gendry-service',
                    operationName: 'dondarrion-1',
                    duration: 3404000,
                    error: false
                },
                queriedService: {
                    duration: 5990000,
                    durationPercent: 64,
                    error: true
                },
                queriedOperation: {
                    duration: 1,
                    durationPercent: 0,
                    error: false
                },
                startTime: new Date().getTime() * 1000 - 18 * 1000 * 1000,
                duration: 3500000
            },
            {
                traceId: 'a55965e5-e0c4-4a37-91a7-da79def7597a',
                spanCount: 30,
                services: [
                    {
                        name: 'stark-service',
                        spanCount: 1
                    },
                    {
                        name: 'tyrell-service',
                        spanCount: 29
                    }
                ],
                root: {
                    url: '/stark/endpoint',
                    serviceName: 'stark-service',
                    operationName: 'clegane-1',
                    duration: 3404000,
                    error: false
                },
                queriedService: {
                    duration: 120000,
                    durationPercent: 64,
                    error: true
                },
                queriedOperation: {
                    duration: 1,
                    durationPercent: 0,
                    error: false
                },
                startTime: new Date().getTime() * 1000 - 18 * 1000 * 1000,
                duration: 126000
            },
            {
                traceId: 'wb651a1b-146x-4c37-91a7-6r61v513r1v11',
                spanCount: 30,
                services: [
                    {
                        name: 'stark-service',
                        spanCount: 1
                    },
                    {
                        name: 'jon-service',
                        spanCount: 29
                    }
                ],
                root: {
                    url: '/east/endpoint',
                    serviceName: 'stark-service',
                    operationName: 'grayjoy-1',
                    duration: 3404000,
                    error: false
                },
                queriedService: {
                    duration: 5990000,
                    durationPercent: 88,
                    error: true
                },
                queriedOperation: {
                    duration: 1,
                    durationPercent: 0,
                    error: false
                },
                startTime: new Date().getTime() * 1000 - 30 * 1000 * 1000,
                duration: 3500000
            },
            {
                traceId: 'b44165e5-xx14-4c37-91a7-da79def7597b',
                spanCount: 25,
                services: [
                    {
                        name: 'randall-service',
                        spanCount: 1
                    },
                    {
                        name: 'stark-service',
                        spanCount: 29
                    }
                ],
                root: {
                    url: '/tarley/endpoint',
                    serviceName: 'randall-service',
                    operationName: 'tarley-1',
                    duration: 3404000,
                    error: false
                },
                queriedService: {
                    duration: 2450000,
                    durationPercent: 94,
                    error: true
                },
                queriedOperation: {
                    duration: 1,
                    durationPercent: 0,
                    error: false
                },
                startTime: new Date().getTime() * 1000 - 44 * 1000 * 1000,
                duration: 2450000
            },
            {
                traceId: 'c80965e5-e0c4-4c37-91a7-da79def7597b',
                spanCount: 19,
                services: [
                    {
                        name: 'stark-service',
                        spanCount: 1
                    },
                    {
                        name: 'tyrell-service',
                        spanCount: 29
                    }
                ],
                root: {
                    url: '/targaryen/endpoint',
                    serviceName: 'targaryen-service',
                    operationName: 'drogo-1',
                    duration: 3404000,
                    error: false
                },
                queriedService: {
                    duration: 5990000,
                    durationPercent: 76,
                    error: false
                },
                queriedOperation: {
                    duration: 1,
                    durationPercent: 0,
                    error: false
                },
                startTime: new Date().getTime() * 1000 - 56 * 1000 * 1000,
                duration: 3500000
            },
            {
                traceId: 'd80965e5-e0c4-4c37-91a7-da79def7597b',
                spanCount: 88,
                services: [
                    {
                        name: 'stark-service',
                        spanCount: 1
                    },
                    {
                        name: 'tyrell-service',
                        spanCount: 29
                    }
                ],
                root: {
                    url: '/stark/endpoint',
                    serviceName: 'stark-service',
                    operationName: 'tully-1',
                    duration: 3404000,
                    error: false
                },
                queriedService: {
                    duration: 5990000,
                    durationPercent: 64,
                    error: true
                },
                queriedOperation: {
                    duration: 1,
                    durationPercent: 0,
                    error: false
                },
                startTime: new Date().getTime() * 1000 - 90 * 1000 * 1000,
                duration: 3500000
            },
            {
                traceId: 'e80965e5-e0c4-4c37-91a7-da79def7597b',
                spanCount: 12,
                services: [
                    {
                        name: 'stark-service',
                        spanCount: 1
                    },
                    {
                        name: 'westeros-service',
                        spanCount: 5
                    }
                ],
                root: {
                    url: '/stark/endpoint',
                    serviceName: 'stark-service',
                    operationName: 'snow-1',
                    duration: 3404000,
                    error: false
                },
                queriedService: {
                    duration: 1260000,
                    durationPercent: 64,
                    error: true
                },
                queriedOperation: {
                    duration: 1,
                    durationPercent: 0,
                    error: false
                },
                startTime: new Date().getTime() * 1000 - 2 * 1000 * 1000,
                duration: 3545000
            }
        ];
    });

connector.findTracesFlat = () => Q.fcall(() => [...trace(), ...trace()]);

module.exports = connector;
