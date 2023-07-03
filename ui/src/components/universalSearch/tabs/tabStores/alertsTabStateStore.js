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

import alertsStore from '../../../alerts/stores/serviceAlertsStore';

const subsystems = (window.haystackUiConfig && window.haystackUiConfig.subsystems) || [];
const isAlertsEnabled = subsystems.includes('alerts');

const oneDayAgo = 24 * 60 * 60 * 1000;

export class AlertsTabStateStore {
    search = null;
    isAvailable = false;
    interval = null;

    init(search, tabProperties) {
        // initialize observables using search object
        // check if for the given search context tab should be available
        this.search = search;

        // check all keys except time
        // eslint-disable-next-line no-unused-vars
        const {interval} = search;

        this.serviceName = tabProperties.serviceName;
        this.isAvailable = isAlertsEnabled && (tabProperties.onlyService || tabProperties.onlyServiceAndOperation);
        this.interval = interval || 'FiveMinute';
    }

    fetch() {
        // todo: fetch service alerts based on search time frame
        alertsStore.fetchServiceAlerts(this.serviceName, this.interval, oneDayAgo);
        return alertsStore;
    }
}

export default new AlertsTabStateStore();
