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

/**
 * detectCycles()
 * Function that takes nodes/links and idAccessor function to mark nodes/links with cycles
 * @param {object} dag - contains "nodes" and "links" properties describe a graph
 * @param {fn} idAccessor - Function that identifies ID of nodes
 * @returns {int} number of found cycles
 */
function detectCycles({nodes, links}, idAccessor = (node) => node.id) {
    // Build "graph" map to enable cycle analysis
    const graph = new Map();
    nodes.forEach((node) => {
        const nodeId = idAccessor(node);
        graph.set(nodeId, {
            data: node,
            out: [],
            depth: -1
        });
    });

    // Extract link information into graph of nodes
    links.forEach(({source, target}) => {
        // Grab source and target nodes
        const sourceNode = graph.get(source);
        const targetNode = graph.get(target);

        // Sanity check graph to make sure ID accessor function
        // eslint-disable-next-line no-throw-literal
        if (!sourceNode) throw new Error(`Missing source node with id: ${source}`);
        // eslint-disable-next-line no-throw-literal
        if (!targetNode) throw new Error(`Missing target node with id: ${target}`);

        // Create "out" dependency for sourceNode
        sourceNode.out.push(targetNode);
    });

    // Convert Map to Array of objects
    const graphNodes = [];
    graph.forEach((graphNode) => {
        graphNodes.push(graphNode);
    });

    // Simple map for found offenses
    const foundOffenses = {};

    // traverse() - Recursive helper function that detects cycles
    // Uses LR - TD recursive stepping in LR DAG graph
    // eslint-disable-next-line consistent-return, no-shadow
    function traverse(nodes, nodeStack = []) {
        // What depth are we at?
        const currentDepth = nodeStack.length;

        // Look at nodes at current depth, top to bottom
        for (let i = 0, l = nodes.length; i < l; i++) {
            const node = nodes[i];
            if (nodeStack.indexOf(node) !== -1) {
                const loop = [...nodeStack.slice(nodeStack.indexOf(node)), node].map((d) => {
                    const id = idAccessor(d.data);
                    d.data.invalidCycleDetected = true;
                    return id;
                });
                // eslint-disable-next-line array-callback-return
                [...nodeStack.slice(nodeStack.indexOf(node)), node].map((d) => {
                    // Cycle detected.  Mark relevant nodes with data.
                    const invalidLoopPathString = loop.join(' -> ');
                    d.data.invalidCyclePath = invalidLoopPathString;
                    foundOffenses[invalidLoopPathString] = foundOffenses[invalidLoopPathString] ? foundOffenses[invalidLoopPathString] + 1 : 1;
                });
                return true; // Return from recursive depth
            }
            if (currentDepth > node.depth) {
                // Don't unnecessarily revisit chunks of the graph
                node.depth = currentDepth;
                traverse(node.out, [...nodeStack, node]);
            }
        }
    }

    // Begin cycle analysis
    traverse(graphNodes);

    return Object.keys(foundOffenses).length;
}

module.exports = {
    detectCycles
};
