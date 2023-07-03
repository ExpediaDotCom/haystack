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
const transformer = {};

function toCallNode(pbNode) {
    return {
        serviceName: pbNode.servicename,
        operationName: pbNode.operationname,
        infrastructureProvider: pbNode.infrastructureprovider || '',
        infrastructureLocation: pbNode.infrastructurelocation || ''
    };
}

transformer.transform = pbCallGraph =>
    pbCallGraph.callsList.map(call => ({
        networkDelta: call.networkdelta / 1000,
        from: toCallNode(call.from),
        to: toCallNode(call.to)
    }));

const TP99_FIELD = '*_99.latency';
const MEAN_FIELD = 'mean.latency';

transformer.mergeTrendsWithLatencyCost = (latencyCost, trends) => latencyCost.map((edge) => {
    const tp99 =
        trends.filter(t =>
        t.serviceName.toLowerCase() === edge.from.serviceName.toLowerCase()
        && t.operationName.toLowerCase() === edge.from.operationName.toLowerCase()
        && t[TP99_FIELD]
        && t[TP99_FIELD].length);

    const mean =
        trends.filter(t =>
        t.serviceName.toLowerCase() === edge.from.serviceName.toLowerCase()
        && t.operationName.toLowerCase() === edge.from.operationName.toLowerCase()
        && t[MEAN_FIELD]
        && t[MEAN_FIELD].length);

    const tp99NetworkDelta = tp99 && tp99.length && tp99[0][TP99_FIELD][0].value;
    const meanNetworkDelta = mean && mean.length && mean[0][MEAN_FIELD][0].value;
    if (tp99NetworkDelta && meanNetworkDelta) {
        return {
            ...edge,
            tp99NetworkDelta: tp99 && tp99.length && tp99[0][TP99_FIELD][0].value,
            meanNetworkDelta: mean && mean.length && mean[0][MEAN_FIELD][0].value
        };
    }
    return {...edge};
});

module.exports = transformer;
