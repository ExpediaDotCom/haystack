/* eslint-disable class-methods-use-this */
/*
 * Copyright 2019 Expedia Group
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

import store from '../../../serviceInsights/stores/serviceInsightsStore';
import timeWindow from '../../../../utils/timeWindow';
import Enums from '../../../../../universal/enums';

// is Service Insights enabled in the appliation config?
const subsystems = (window.haystackUiConfig && window.haystackUiConfig.subsystems) || [];
const enabled = subsystems.includes('serviceInsights');

export class ServiceInsightsTabStateStore {
    isAvailable = false;
    hasValidSearch = false;

    init(search, tabProperties) {
        this.search = search;
        this.tabProperties = tabProperties;

        // If user is directly accessing URL, show this feature
        const isAccessingServiceInsights = this.search.tabId === 'serviceInsights';

        // has required minimal search terms
        this.hasValidSearch = !!(tabProperties.serviceName || tabProperties.traceId);

        this.isAvailable = enabled && (this.hasValidSearch || isAccessingServiceInsights);
    }

    fetch() {
        const search = this.search || {};
        const timePresetOptions = window.haystackUiConfig.tracesTimePresetOptions;
        const isCustomTimeRange = !!(search.time && search.time.from && search.time.to);

        let activeWindow;

        if (isCustomTimeRange) {
            activeWindow = timeWindow.toCustomTimeRange(search.time.from, search.time.to);
        } else if (search.time && search.time.preset) {
            activeWindow = timePresetOptions.find((preset) => preset.shortName === search.time.preset);
        } else {
            activeWindow = timeWindow.defaultPreset;
        }

        const activeWindowTimeRange = timeWindow.toTimeRange(activeWindow.value);

        const micro = (milli) => milli * 1000;
        const startTime = micro(activeWindowTimeRange.from);
        const endTime = micro(activeWindowTimeRange.until);
        const relationship = this.tabProperties.serviceName ? search.relationship : Enums.relationship.all;

        // Get service insights
        store.fetchServiceInsights({
            serviceName: this.tabProperties.serviceName,
            operationName: this.tabProperties.operationName,
            traceId: this.tabProperties.traceId,
            startTime,
            endTime,
            relationship
        });

        store.hasValidSearch = true;

        return store;
    }
}

export default new ServiceInsightsTabStateStore();
