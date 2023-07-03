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
import operationStore from '../../../trends/stores/operationStore';
import timeWindow from '../../../../utils/timeWindow';

const subsystems = (window.haystackUiConfig && window.haystackUiConfig.subsystems) || [];
const enabled = subsystems.includes('trends');

// TODO remove once we are out of older states
function createWindow(search) {
    const from = search.time.from;
    const until = search.time.to;
    const isCustomTimeRange = !!(from && until);

    let activeWindow;
    if (isCustomTimeRange) {
        activeWindow = timeWindow.toCustomTimeRange(parseInt(from, 10), parseInt(until, 10));
    } else {
        activeWindow = timeWindow.findMatchingPresetByShortName(search.time.preset) || timeWindow.defaultPreset;
        const activeWindowTimeRange = timeWindow.toTimeRange(activeWindow.value);
        activeWindow.from = activeWindowTimeRange.from;
        activeWindow.until = activeWindowTimeRange.until;
    }

    return activeWindow;
}

export class TrendsTabStateStore {
    search = null;
    isAvailable = false;
    serviceName = null;
    operationName = null;

    init(search, tabProperties) {
        // initialize observables using search object
        // check if for the given search context, tab is available
        this.search = search;

        this.serviceName = tabProperties.serviceName;
        this.operationName = tabProperties.operationName;
        this.isAvailable = enabled && (tabProperties.onlyService || tabProperties.onlyServiceAndOperation);
    }

    fetch() {
        // TODO acting as a wrapper for older stores for now,
        // TODO fetch logic here
        const window = createWindow(this.search);

        const granularity = timeWindow.getLowerGranularity(window.value);

        const query = {
            granularity: granularity.value,
            from: window.from,
            until: window.until
        };

        operationStore.fetchStats(this.serviceName, query, !!(window.isCustomTimeRange), { operationName: this.operationName});

        return operationStore;
    }
}

export default new TrendsTabStateStore();
