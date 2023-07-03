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

// Service Insights node types
const type = {
    edge: 'edge', // the network edge, which should be a source
    mesh: 'mesh', // a mesh network node, which is uniquely identified by its operation
    gateway: 'gateway', // a network gateway, which is uniquely identified by its datacenter
    service: 'service', // application
    database: 'database', // database
    outbound: 'outbound', // outbound nodes, i.e. representation of client spans
    uninstrumented: 'uninstrumented' // node that is not instrumented with open tracing
};

// Service Insights node relationships, using river terms (equivalent family terms in parentheses)
const relationship = {
    central: 'central', // other relationships are relative to this node (self)
    upstream: 'upstream', // directly upstream of the central node (ancestors)
    distributary: 'distributary', // distributary of an upstream node (siblings, aunts, uncles, cousins)
    downstream: 'downstream', // directly downstream of the central node (descendants)
    // tributaries of downstream nodes are not supported (other parents of your children, other grandparents of your grandchildren, etc…)
    unknown: 'unknown', // unknown relationship to central node, due to missing spans
    all: 'all' // convenience value for all relationships
};

module.exports = {
    type,
    relationship
};
