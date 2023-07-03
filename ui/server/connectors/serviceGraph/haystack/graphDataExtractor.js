/* eslint-disable no-param-reassign */
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

const _ = require('lodash');

const extractor = {};

const config = require('../../../config/config');

const WINDOW_SIZE_IN_SECS =  config.connectors.serviceGraph && config.connectors.serviceGraph.windowSizeInSecs;

function getEdgeName(vertex) {
    if (vertex.name) {
        return vertex.name;
    }
    return vertex;
}

function flattenStats(edges) {
    const serviceEdges = edges.map(edge => ({
        source: {
            name: getEdgeName(edge.source),
            tags: edge.source.tags
        },
        destination: {
            name: getEdgeName(edge.destination),
            tags: edge.destination.tags
        },
        stats: {
            count: (edge.stats.count / WINDOW_SIZE_IN_SECS),
            errorCount: (edge.stats.errorCount / WINDOW_SIZE_IN_SECS)
        }
    }));
    return _.uniqWith(serviceEdges, _.isEqual);
}

function filterEdgesInComponent(component, edges) {
    const componentEdges = [];

    edges.forEach((edge) => {
        if (component.includes(edge.source.name) || component.includes(edge.destination.name)) {
            componentEdges.push(edge);
        }
    });

    return componentEdges;
}

function updatedDestination(graph, destination, source) {
    if (graph[destination]) {
        graph[destination].to = [...graph[destination].to, source];
    } else {
        graph[destination] = { to: [source] };
    }
}

function toUndirectedGraph(edges) {
    const graph = {};
    edges.forEach((edge) => {
        if (graph[edge.source.name]) {
            // add or update source
            graph[edge.source.name].to = [...graph[edge.source.name].to, edge.destination.name];

            // add or update destination
            // graph[edge.destination] = updatedDestination(graph[edge.destination], edge.source);
            updatedDestination(graph, edge.destination.name, edge.source.name);
        } else {
            // create edge at the source
            graph[edge.source.name] = { to: [edge.destination.name] };

            // add or update destination
            // graph[edge.destination] = updatedDestination(graph[edge.destination], edge.source);
            updatedDestination(graph, edge.destination.name, edge.source.name);
        }
    });

    return graph;
}

function doDepthFirstTraversal(graph, node) {
    const traversedNodes = [];
    const traversing = [];

    traversing.push(node);
    graph[node].isTraversing = true;

    while (traversing.length) {
        const nextNode = traversing.pop();
        traversedNodes.push(nextNode);
        graph[nextNode].isTraversed = true;

        graph[nextNode].to.forEach((to) => {
            if (!graph[to].isTraversing) {
                graph[to].isTraversing = true;
                traversing.push(to);
            }
        });
    }

    return traversedNodes;
}

function filterUntraversed(graph) {
    return Object.keys(graph).filter(node => !graph[node].isTraversed);
}

function extractConnectedComponents(edges) {
    // converting to adjacency list undirected graph
    const graph = toUndirectedGraph(edges);
    // perform depth first graph traversals to get connected components list
    // until all the disjoint graps are traversed
    const connectedComponents = [];
    let untraversedNodes = filterUntraversed(graph);
    while (untraversedNodes.length) {
        connectedComponents.push(doDepthFirstTraversal(graph, untraversedNodes[0]));
        untraversedNodes = filterUntraversed(graph);
    }

    // return list of connected components
    return connectedComponents;
}

extractor.extractGraphFromEdges = (serviceToServiceEdges) => {
    // get list of connected components in the full graph
    const connectedComponents = extractConnectedComponents(serviceToServiceEdges);

    // order components by service count
    const sortedConnectedComponents = connectedComponents.sort((a, b) => b.length - a.length);

    // split edges list by connected components
    // thus form multiple sub-graphs
    const graphs = [];
    sortedConnectedComponents.forEach(component => graphs.push(filterEdgesInComponent(component, serviceToServiceEdges)));

    // return graphs, one for each connected component
    return graphs;
};

extractor.extractGraphs = (data) => {
    // convert servicegraph to expected ui data format
    const serviceToServiceEdges = flattenStats(data.edges);
    return extractor.extractGraphFromEdges(serviceToServiceEdges);
};

module.exports = extractor;
