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

const serverSpanTags = [
    {
        key: 'span.kind',
        value: 'server'
    },
    {
        key: 'X-HAYSTACK-IS-MERGED-SPAN',
        value: true
    }
];

const clientSpanTags = [
    {
        key: 'span.kind',
        value: 'client'
    }
];

module.exports = {
    edge: {
        serviceName: 'edge',
        tags: serverSpanTags.concat([
            {
                key: 'edge.route',
                value: '/path/{id}'
            }
        ])
    },
    gateway: {
        serviceName: 'gateway',
        tags: serverSpanTags.concat([
            {
                key: 'app.datacenter',
                value: 'us-east-1'
            }
        ])
    },
    'mock-web-ui': {
        serviceName: 'mock-web-ui',
        tags: serverSpanTags
    },
    'auth-service': {
        serviceName: 'auth-service',
        tags: serverSpanTags
    },
    'mock-api-service': {
        serviceName: 'mock-api-service',
        tags: serverSpanTags
    },
    'key-value-store': {
        serviceName: 'key-value-store',
        tags: clientSpanTags.concat([
            {
                key: 'db.type',
                value: 'key-value-store'
            }
        ])
    },
    'document-store': {
        serviceName: 'document-store',
        tags: clientSpanTags.concat([
            {
                key: 'db.type',
                value: 'document-store'
            }
        ])
    },
    'distributed-database': {
        serviceName: 'distributed-database',
        tags: clientSpanTags.concat([
            {
                key: 'db.type',
                value: 'distributed-database'
            }
        ])
    },
    'service-mesh': {
        serviceName: 'service-mesh',
        tags: serverSpanTags
    },
    client: {
        tags: clientSpanTags
    }
};
