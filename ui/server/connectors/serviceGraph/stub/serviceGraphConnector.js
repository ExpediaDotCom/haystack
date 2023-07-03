/*
 * Copyright 2018 Expedia Group
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

const Q = require('q');

const connector = {};

const extractor = require('../haystack/graphDataExtractor');

const serviceGraph = {
    edges: [
        {
            source: {
                name: 'stark-service',
                tags: {
                    DEPLOYMENT: 'aws'
                }
            },
            destination: {
                name: 'baratheon-service'
            },
            operation: 'baratheon-1',
            stats: {
                count: 55500,
                errorCount: 9000
            }
        },
        {
            source: {
                name: 'stark-service'
            },
            destination: {
                name: 'grayjoy-service'
            },
            operation: 'grayjoy-1',
            stats: {
                count: 21005,
                errorCount: 1009
            }
        },
        {
            source: {
                name: 'baratheon-service'
            },
            destination: {
                name: 'lannister-service'
            },
            operation: 'lannister-1',
            stats: {
                count: 23456,
                errorCount: 678
            }
        },
        {
            source: {
                name: 'baratheon-service'
            },
            destination: {
                name: 'clegane-service'
            },
            operation: 'clegane-1',
            stats: {
                count: 401,
                errorCount: 13
            }
        },
        {
            source: {
                name: 'lannister-service'
            },
            destination: {
                name: 'tyrell-service'
            },
            operation: 'tyrell-1',
            stats: {
                count: 30000,
                errorCount: 2
            }
        },
        {
            source: {
                name: 'tyrell-service'
            },
            destination: {
                name: 'targaryen-service'
            },
            operation: 'targaryen-1',
            stats: {
                count: 50004,
                errorCount: 20000
            }
        },
        {
            source: {
                name: 'tyrell-service'
            },
            destination: {
                name: 'tully-service'
            },
            operation: 'tully-1',
            stats: {
                count: 121,
                errorCount: 1
            }
        },
        {
            source: {
                name: 'targaryen-service'
            },
            destination: {
                name: 'dragon-service'
            },
            operation: 'dragon-1',
            stats: {
                count: 19000,
                errorCount: 800
            }
        },
        {
            source: {
                name: 'targaryen-service'
            },
            destination: {
                name: 'drogo-service'
            },
            operation: 'drogo-1',
            stats: {
                count: 98,
                errorCount: 0
            }
        },
        {
            source: {
                name: 'targaryen-service'
            },
            destination: {
                name: 'mormont-service'
            },
            operation: 'mormont-1',
            stats: {
                count: 5000,
                errorCount: 100
            }
        }
    ]
};

/* eslint-disable-next-line no-unused-vars */
connector.getServiceGraphForTimeLine = (from, to) => Q.fcall(() => extractor.extractGraphs(serviceGraph));

module.exports = connector;
