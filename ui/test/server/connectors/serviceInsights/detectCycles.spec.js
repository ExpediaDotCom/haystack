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

const {detectCycles} = require('../../../../server/connectors/serviceInsights/detectCycles');

function getBaseGraph(primaryIdKey = 'id') {
    return {
        nodes: [
            {
                [primaryIdKey]: 'service-a'
            },
            {
                [primaryIdKey]: 'service-b'
            },
            {
                [primaryIdKey]: 'service-c'
            }
        ],
        links: [
            {
                source: 'service-a',
                target: 'service-b'
            },
            {
                source: 'service-b',
                target: 'service-c'
            }
        ]
    };
}

describe('detectCycles', () => {
    it('should find 0 offenses given a normal graph', () => {
        // given
        let graph = getBaseGraph();

        // when
        let result = detectCycles(graph);

        // then
        expect(result).to.equal(0);
    });

    it('should find 0 offenses given a normal graph and custom id accessor function', () => {
        // given
        let graph = getBaseGraph('customId');

        // when
        let result = detectCycles(graph, (node) => node.customId);

        // then
        expect(result).to.equal(0);
    });

    it('should find 1 cycle violation given a graph with a cycle', () => {
        // given
        let graph = getBaseGraph();
        graph.links.push({
            source: 'service-c',
            target: 'service-a'
        });

        // when
        let result = detectCycles(graph);

        // then
        expect(result).to.equal(1);
        expect(graph).to.have.nested.property('nodes[0].invalidCyclePath', 'service-a -> service-b -> service-c -> service-a');
    });

    it('should throw an exception given a missing source node id', () => {
        // given
        let graph = getBaseGraph();
        graph.links.push({
            source: 'service-z',
            target: 'service-a'
        });

        // when
        try {
            detectCycles(graph);
            expect.fail('Failed to throw exception.');
        } catch (e) {
            // then
            expect(e.message).to.equal('Missing source node with id: service-z');
        }
    });

    it('should throw an exception given a missing target node id', () => {
        // given
        let graph = getBaseGraph();
        graph.links.push({
            source: 'service-a',
            target: 'service-z'
        });

        // when
        try {
            detectCycles(graph);
            expect.fail('Failed to throw exception.');
        } catch (e) {
            // then
            expect(e.message).to.equal('Missing target node with id: service-z');
        }
    });
});
