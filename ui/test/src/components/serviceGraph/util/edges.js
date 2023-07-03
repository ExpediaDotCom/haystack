/*
 * Copyright 2018 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */

const edges = [
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
        stats: {
            count: 15.416666666666666,
            errorCount: 2.5
        }
    },
    {
        source: {
            name: 'stark-service'
        },
        destination: {
            name: 'grayjoy-service'
        },
        stats: {
            count: 5.834722222222222,
            errorCount: 0.2802777777777778
        }
    },
    {
        source: {
            name: 'baratheon-service'
        },
        destination: {
            name: 'lannister-service'
        },
        stats: {
            count: 6.515555555555555,
            errorCount: 0.18833333333333332
        }
    },
    {
        source: {
            name: 'baratheon-service'
        },
        destination: {
            name: 'clegane-service'
        },
        stats: {
            count: 0.11138888888888888,
            errorCount: 0.003611111111111111
        }
    },
    {
        source: {
            name: 'lannister-service'
        },
        destination: {
            name: 'tyrell-service'
        },
        stats: {
            count: 8.333333333333334,
            errorCount: 0.0005555555555555556
        }
    },
    {
        source: {
            name: 'tyrell-service'
        },
        destination: {
            name: 'targaryen-service'
        },
        stats: {
            count: 13.89,
            errorCount: 5.555555555555555
        }
    },
    {
        source: {
            name: 'tyrell-service'
        },
        destination: {
            name: 'tully-service'
        },
        stats: {
            count: 0.03361111111111111,
            errorCount: 0.0002777777777777778
        }
    },
    {
        source: {
            name: 'targaryen-service'
        },
        destination: {
            name: 'dragon-service'
        },
        stats: {
            count: 5.277777777777778,
            errorCount: 0.2222222222222222
        }
    },
    {
        source: {
            name: 'targaryen-service'
        },
        destination: {
            name: 'drogo-service'
        },
        stats: {
            count: 0.02722222222222222,
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
        stats: {
            count: 1.3888888888888888,
            errorCount: 0.027777777777777776
        }
    }
];

module.exports = edges;
