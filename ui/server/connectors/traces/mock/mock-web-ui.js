/*
 * Copyright 2019 Expedia Group
 *
 *         Licensed under the Apache License, Version 2.0 (the 'License');
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an 'AS IS' BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */

module.exports = [
    {
        extends: 'edge',
        data: {
            operationName: 'www.website-a.com',
            tags: [
                {
                    key: 'edge.route',
                    value: '/foo/{id}'
                }
            ]
        },
        children: [
            {
                extends: 'gateway',
                data: {
                    operationName: 'proxy request'
                },
                children: [
                    {
                        extends: 'service-mesh',
                        data: {
                            operationName: 'proxy mock-web-ui'
                        },
                        children: [
                            {
                                extends: 'mock-web-ui',
                                data: {
                                    operationName: 'GET /foo/123'
                                },
                                children: [
                                    {
                                        extends: 'service-mesh',
                                        data: {
                                            operationName: 'proxy auth-service'
                                        },
                                        children: [
                                            {
                                                extends: 'auth-service',
                                                data: {
                                                    operationName: 'generate token'
                                                }
                                            }
                                        ]
                                    },
                                    {
                                        extends: 'service-mesh',
                                        data: {
                                            operationName: 'proxy mock-api-service'
                                        },
                                        children: [
                                            {
                                                extends: 'mock-api-service',
                                                data: {
                                                    serviceName: 'Foo service',
                                                    operationName: 'READ'
                                                },
                                                children: [
                                                    {
                                                        extends: 'distributed-database',
                                                        data: {
                                                            operationName: 'SELECT foo'
                                                        }
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    },
    {
        extends: 'edge',
        data: {
            operationName: 'www.website-b.com',
            tags: [
                {
                    key: 'edge.route',
                    value: '/bar/{id}'
                }
            ]
        },
        children: [
            {
                extends: 'gateway',
                data: {
                    operationName: 'proxy request'
                },
                children: [
                    {
                        extends: 'service-mesh',
                        data: {
                            operationName: 'proxy mock-web-ui'
                        },
                        children: [
                            {
                                extends: 'mock-web-ui',
                                data: {
                                    operationName: 'GET /bar/123'
                                },
                                children: [
                                    {
                                        extends: 'key-value-store',
                                        data: {
                                            operationName: 'GET bar'
                                        }
                                    },
                                    {
                                        extends: 'service-mesh',
                                        data: {
                                            operationName: 'proxy missing-service'
                                        },
                                        children: []
                                    },
                                    {
                                        extends: 'service-mesh',
                                        data: {
                                            operationName: 'proxy mock-api-service'
                                        },
                                        children: [
                                            {
                                                extends: 'mock-api-service',
                                                data: {
                                                    serviceName: 'Foo service',
                                                    operationName: 'GET /foo/related'
                                                },
                                                children: [
                                                    {
                                                        extends: 'service-mesh',
                                                        data: {
                                                            operationName: 'proxy other-api-service'
                                                        },
                                                        children: []
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    }
];
