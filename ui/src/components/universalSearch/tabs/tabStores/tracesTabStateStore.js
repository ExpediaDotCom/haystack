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
import tracesSearchStore from '../../../traces/stores/tracesSearchStore';

const subsystems = (window.haystackUiConfig && window.haystackUiConfig.subsystems) || [];
const enabled = subsystems.includes('traces');

function spanLevelFiltersToList(filteredNames, traceSearch) {
    return JSON.stringify(filteredNames.map((name) => JSON.stringify(traceSearch[name])));
}

export class TracesTabStateStore {
    search = null;
    isAvailable = false;
    tabProperties = null;

    init(search, tabProperties) {
        // initialize observables using search object
        // check if for the given search context, tab is available
        this.search = search;
        this.tabProperties = tabProperties;

        // check all keys except time
        // eslint-disable-next-line no-unused-vars
        const isAccessingTraces = search.tabId === 'traces';
        this.isAvailable = isAccessingTraces || (enabled && !!tabProperties.queries.length);
    }

    fetch() {
        // TODO acting as a wrapper for older stores for now,
        // TODO fetch logic here
        // eslint-disable-next-line no-unused-vars
        const {time, tabId, type, interval, ...traceSearch} = this.search;

        const filteredNames = Object.keys(traceSearch).filter(name => /query_[0-9]/.test(name));

        traceSearch.spanLevelFilters = spanLevelFiltersToList(filteredNames, traceSearch);
        traceSearch.timePreset = this.search.time.preset;
        traceSearch.startTime = this.search.time.from;
        traceSearch.endTime = this.search.time.to;

        tracesSearchStore.fetchSearchResults(traceSearch);
        return tracesSearchStore;
    }
}

export default new TracesTabStateStore();
