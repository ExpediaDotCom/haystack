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

/* eslint-disable no-unused-expressions */

import {expect} from 'chai';
import {
    buildGraphLayout,
    computeGraphSize,
    computeGraphPosition
} from '../../../../../src/components/serviceInsights/serviceInsightsGraph/dataLayout';

describe('serviceInsightsGraph/dataLayout.js', () => {
    describe('buildGraphLayout()', () => {
        it('should create a "nodes" array with x,y values and data objects', () => {
            const nodeData = [{name: 'serviceA', id: 123}, {name: 'serviceB', parent: 123}];
            const {nodes} = buildGraphLayout({nodes: nodeData, links: []});
            expect(nodes[0].label).to.equal('serviceA');
            expect(nodes[0].data).to.equal(nodeData[0]);
            expect(nodes[0].x).to.equal(0);
            expect(nodes[0].y).to.equal(0);
            expect(nodes[1].label).to.equal('serviceB');
            expect(nodes[1].data).to.equal(nodeData[1]);
            expect(nodes[1].x).to.equal(0);
            expect(nodes[1].y).to.equal(25);
        });
        it('should create an "edges" array with x,y values and link data', () => {
            const links = [{target: 'serviceA'}, {source: 'serviceA', target: 'serviceB'}];
            const {edges} = buildGraphLayout({nodes: [{name: 'foo'}], links});
            expect(edges[0].points[0]).to.deep.equal({x: 0, y: 0});
            expect(edges[0].points[2]).to.deep.equal({x: 170, y: 0});
            expect(edges[0].link).to.exist;
            expect(edges[1].points[0]).to.deep.equal({x: 170, y: 0});
            expect(edges[1].points[2]).to.deep.equal({x: 340, y: 0});
            expect(edges[1].link).to.exist;
        });
    });

    describe('computeGraphSize()', () => {
        it('should produce a width and height from dagre node data', () => {
            const nodes = [{label: 'serviceA', x: 0, y: 0}, {label: 'serviceB', x: 12, y: 25}, {label: 'serviceC', x: 15, y: 40}];
            const {width, height} = computeGraphSize(nodes);
            expect(width).to.equal(15);
            expect(height).to.equal(40);
        });
    });

    describe('computeGraphPosition()', () => {
        it('should produce x, y values that match the padding values when graph is larger than available space', () => {
            const stored = document.body.getBoundingClientRect;
            document.body.getBoundingClientRect = () => ({width: 100, height: 80});

            const {x, y} = computeGraphPosition({width: 200, height: 100});
            expect(x).to.equal(80);
            expect(y).to.equal(100);
            document.body.getBoundingClientRect = stored;
        });

        it('should center the graph (horizontally) when graph is smaller than available space', () => {
            const stored = document.body.getBoundingClientRect;
            document.body.getBoundingClientRect = () => ({width: 1000, height: 80});

            const {x, y} = computeGraphPosition({width: 200, height: 100});
            expect(x).to.equal(315);
            expect(y).to.equal(100);
            document.body.getBoundingClientRect = stored;
        });
    });
});
