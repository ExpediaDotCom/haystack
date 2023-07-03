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

class Graph {
    constructor() {
        this.nodes = [];
        this.edges = [];
        this.errorCountsByVertex = new Map();
    }

    static buildGraph = (rawEdges) => {
        const graph = new Graph();
        _.forEach(rawEdges, (edge) => {
            graph.addEdge(edge);
        });
        return graph;
    }

    addEdge(edge) {
        if (!this.allNodes().includes(edge.source.name)) {
            this.nodes.push({
                name: edge.source.name,
                tags: edge.source.tags
            });
        }
        if (!this.allNodes().includes(edge.destination.name)) {
            this.nodes.push({
                name: edge.destination.name,
                tags: edge.destination.tags
            });
        }
        this.edges.push(edge);
    }

    allNodes() {
        return _.map(this.nodes, node => node.name);
    }

    allEdges() {
        return this.edges;
    }

    incomingTrafficForNode(name) {
        return _.filter(this.edges, e => e.destination.name === name);
    }

    outgoingTrafficForNode(name) {
        return _.filter(this.edges, e => e.source.name === name);
    }

    buildErrorRateMapForNode(name) {
        _.filter(this.edges, edge => edge.destination.name === name).forEach((edge) => {
            if (this.errorCountsByVertex.get(edge.destination.name)) {
                const totalCnt = this.errorCountsByVertex.get(edge.destination.name).count + edge.stats.count;
                const errorCnt = this.errorCountsByVertex.get(edge.destination.name).errorCount + edge.stats.errorCount;
                this.errorCountsByVertex.set(edge.destination.name, {count: totalCnt, errorCount: errorCnt});
            } else {
                this.errorCountsByVertex.set(edge.destination.name, {
                    count: edge.stats.count,
                    errorCount: edge.stats.errorCount
                });
            }
        });
        // Fill in zero for vertex that are only in source but not in destination of a edge
        if (!this.errorCountsByVertex.get(name)) {
            this.errorCountsByVertex.set(name, {count: 0, errorCount: 0});
        }
    }

    tagsForNode(name) {
        return _.first((_.filter(this.nodes, node => node.name === name))).tags;
    }

    errorRateForNode(name) {
        if (!this.errorCountsByVertex.get(name)) {
            this.buildErrorRateMapForNode(name);
        }
        const stats = this.errorCountsByVertex.get(name);
        if (stats.count === 0) {
            return '0';
        }
        return ((stats.errorCount * 100) / stats.count).toFixed(2);
    }

    errorRateForConnection(source, dest) {
        const stats = _.first(_.filter(this.edges, edge => edge.source.name === source && edge.destination.name === dest)).stats;
        if (stats.count === 0) {
            return '0';
        }
        return ((stats.errorCount * 100) / stats.count).toFixed(2);
    }

    requestRateForNode(name) {
        if (!this.errorCountsByVertex.get(name)) {
            this.buildErrorRateMapForNode(name);
        }
        return this.errorCountsByVertex.get(name).count.toFixed(2);
    }

    requestRateForConnection(source, dest) {
        const stats = _.first(_.filter(this.edges, edge => edge.source.name === source && edge.destination.name === dest)).stats;
        return stats.count.toFixed(2);
    }
}

module.exports = Graph;
