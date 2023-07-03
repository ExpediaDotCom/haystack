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

const Seedrandom = require('seedrandom');
const merge = require('deepmerge');

const defaultTypes = require('./spanTypes');

const mockTraces = {
    'mock-web-ui': require('./mock-web-ui') // eslint-disable-line
};

/**
 * generateRandomId()
 * Generate a ID given "length" and "seed" arguments
 * @param {int} length - length of desired ID returned in [0-9][a-f] format
 * @param {string} seed - seed value for idempotent generation.
 */
function generateRandomId(length = 16, seed) {
    const randomNumberGenerator = new Seedrandom(`${seed}`);
    const characters = 'abcdef0123456789';
    const charactersLength = characters.length;
    let result = '';
    for (let i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(randomNumberGenerator.quick() * charactersLength));
    }
    return result;
}

/**
 * getMockServiceNames()
 * Returns an array of strings which represent available mock traces
 */
function getMockServiceNames() {
    return Object.keys(mockTraces);
}

/**
 * getMockOperationNamesFromTrace()
 * Recursive function that looks through tree of mock data returns a map/dictionary of found operation names
 * @param {object} trace - Object which represents current root of tree-like data strucutre of mock data.
 * @param {object} operationNameMap - Map of operation names found
 */
function getMockOperationNamesFromTrace(trace, operationNameMap = {}) {
    // If operationName found on trace, add to map
    const operationName = trace.data && trace.data.operationName;
    if (operationName) {
        operationNameMap[operationName] = true;
    }

    // Recurse data
    if (trace.children) {
        for (let i = 0; i < trace.children.length; i++) {
            getMockOperationNamesFromTrace(trace.children[i], operationNameMap);
        }
    }
    return operationNameMap;
}

/**
 * getMockOperationNames()
 * Gets all available operation names from all traces
 */
function getMockOperationNames() {
    let operationNames = {};
    const serviceNames = getMockServiceNames();
    for (let i = 0; i < serviceNames.length; i++) {
        const tracePath = mockTraces[serviceNames[i]];
        for (let j = 0; j < tracePath.length; j++) {
            const trace = tracePath[j];
            operationNames = {
                ...operationNames,
                ...getMockOperationNamesFromTrace(trace)
            };
        }
    }
    return Object.keys(operationNames);
}
/**
 * generateMockTrace()
 * Recursive function that generates a single mock trace represented by an array of linked spans
 * @param {object} trace - Object which represents current root of tree-like data strucutre of mock data.   Recursion through "children" property
 * @param {array} spans - Array of mock spans which is the final return result
 * @param {string} seed - Seed value used for idempotent generation of random Ids
 * @param {object} parentSpan - Parent span generated in previous recursive step
 */
function generateMockTrace(trace, spans = [], seed, parentSpan) {
    // Define random number generator
    const randomNumberGenerator = new Seedrandom(seed);

    // Sanity check
    // If current trace defines extension, copy information
    if (trace.extends && !defaultTypes[trace.extends]) {
        throw new Error(`Invalid "extends" property "${trace.extends}" defined on mock trace data.`);
    }

    // Merge custom trace span data with default span data
    const newSpan = merge(trace.data || {}, defaultTypes[trace.extends] || {});
    let spanId;
    let traceId;
    let parentSpanId;

    // If no parent span, assume this is the root. (traceId should also be spanId)
    if (!parentSpan) {
        traceId = generateRandomId(16, randomNumberGenerator.quick() * 100);
        spanId = traceId;
    } else {
        traceId = parentSpan.traceId;
        parentSpanId = parentSpan.spanId;
        spanId = generateRandomId(16, randomNumberGenerator.quick() * 100);
    }

    // Sanity check merged mock span data that is required
    if (!newSpan.operationName) {
        throw new Error(`Mock span is missing property operationName: ${JSON.stringify(newSpan)}`);
    }

    // Set IDs required for proper trace linkage
    newSpan.spanId = spanId;
    newSpan.traceId = traceId;
    newSpan.parentSpanId = parentSpanId;

    // Set random duration
    newSpan.duration = Math.floor(randomNumberGenerator.quick() * 1000);

    // Push span onto array
    spans.push(newSpan);

    // Now, process children
    if (trace.children) {
        for (let i = 0; i < trace.children.length; i++) {
            generateMockTrace(trace.children[i], spans, randomNumberGenerator.quick() * 100, newSpan);
        }
    }

    // Return spans
    return spans;
}

/**
 * generateMockTraceSpans()
 * Generates an array of mock traces
 * @param {string} mockTraceName - Name of mock trace of spans to generate
 */

function generateMockTraceSpans(mockTraceName = 'mock-web-ui') {
    // Sane options
    if (!mockTraces[mockTraceName]) {
        throw new Error(`No mock trace data available for '${mockTraceName}'`);
    }
    const tracePath = mockTraces[mockTraceName];
    const seed = mockTraceName;

    // Define traces variable
    let traceSpans = [];

    // Start generating trace spans
    for (let i = 0; i < tracePath.length; i++) {
        traceSpans = traceSpans.concat(generateMockTrace(tracePath[i], [], `${seed}-${i}`));
    }

    // Return array of spans representing multiple traces
    return traceSpans;
}

module.exports = {
    generateMockTraceSpans,
    getMockServiceNames,
    getMockOperationNames
};
