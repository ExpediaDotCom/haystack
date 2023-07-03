/*
 * Copyright 2019 Expedia Group
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

import dagre from 'dagre';

const nodeSpacingX = 170; // horizontal spacing between nodes
const nodeSpacingY = 25; // vertical spacing between nodes

/**
 * buildDataLayout()
 *
 * Given a set of nodes and links, calculate the x, y positions of nodes and
 * the array of points that represent the lines between points.
 */
export function buildGraphLayout(data) {
    // Create a new directed graph
    const graph = new dagre.graphlib.Graph();

    // Set an object for the graph label
    graph.setGraph({
        rankdir: 'LR',
        ranksep: nodeSpacingX,
        nodesep: nodeSpacingY
    });

    data.nodes.forEach((node) => {
        // Note that width only affects how closely the lines touch (converge)
        graph.setNode(node.id, {
            label: node.name,
            width: 0,
            height: 0,
            data: node
        });
    });

    // Map from API data to dagre view data
    data.links.forEach((link) => {
        graph.setEdge(link.source, link.target, {
            link
        });
    });

    dagre.layout(graph);

    const nodes = graph.nodes().map((id) => graph.node(id));
    const edges = graph.edges().map((id) => graph.edge(id));

    return {
        nodes,
        edges
    };
}

/**
 * computeGraphSize()
 *
 * Determine the overall size of the resulting graph
 */
export function computeGraphSize(nodes) {
    let maxX = 0;
    let maxY = 0;

    nodes.forEach((n) => {
        if (n.y > maxY) maxY = n.y;
        if (n.x > maxX) maxX = n.x;
    });

    return {
        width: maxX,
        height: maxY
    };
}

/**
 * computeGraphPosition()
 *
 * Position the graph. If the graph is larger than the available space, then left
 * align with some padding. If it is smaller than the available space, center it.
 */
export function computeGraphPosition(graphSize) {
    const viewport = document.body.getBoundingClientRect();

    let x = 80;
    const y = 100;

    // If the graph is smaller than the available horizontal space, center it
    if (graphSize.width + nodeSpacingX < viewport.width) {
        const diff = viewport.width - graphSize.width - nodeSpacingX;
        x = diff / 2;
    }

    return {x, y};
}
