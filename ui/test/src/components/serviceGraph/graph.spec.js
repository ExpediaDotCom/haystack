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

const expect = require('chai').expect;
const Graph = require('../../../../src/components/serviceGraph/util/graph');
const _ = require('lodash');
const edges = require('./util/edges.js');

describe('Graph', () => {
    const graph = new Graph();

    before(() => {
        _.forEach(edges, (edge) => {
            graph.addEdge(edge);
        });
    });

    it('should return the error rate and request rate accurately for a given node', () => {
        const errorRate = graph.errorRateForNode('stark-service');
        const requestRate = graph.requestRateForNode('stark-service');
        expect(errorRate).to.equal('0');
        expect(requestRate).to.equal('0.00');

        const errorRate1 = graph.errorRateForNode('baratheon-service');
        const requestRate1 = graph.requestRateForNode('baratheon-service');
        expect(errorRate1).to.equal('16.22');
        expect(requestRate1).to.equal('15.42');
    });

    it('should return the error rate and request rate accurately for a given connection', () => {
        const errorRate = graph.errorRateForConnection('baratheon-service', 'clegane-service');
        const requestRate = graph.requestRateForConnection('baratheon-service', 'clegane-service');
        expect(errorRate).to.equal('3.24');
        expect(requestRate).to.equal('0.11');
    });

    it('should provide the list of tags for a given node', () => {
       const tags = graph.tagsForNode('stark-service');
       expect(tags.DEPLOYMENT).to.equal('aws');
    });
    //
    // it('should list the incoming and outgoing traffic details', () => {
    //     const incoming = graph.incomingTrafficForNode('grayjoy-service');
    //     const outgoing = graph.outgoingTrafficForNode('stark-service');
    //     expect(incoming[0]).to.equal('stark-service');
    //     expect(outgoing[0]).to.equal('baratheon-service');
    // });
});
