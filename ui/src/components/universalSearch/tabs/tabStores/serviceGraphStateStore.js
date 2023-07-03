/* eslint-disable class-methods-use-this */
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

import store from '../../../serviceGraph/stores/serviceGraphStore';

const subsystems = (window.haystackUiConfig && window.haystackUiConfig.subsystems) || [];
const enabled = subsystems.includes('serviceGraph');

export class ServiceGraphStateStore {
    search = null;
    isAvailable = false;

    init(search, tabProperties) {
        // initialize observables using search object
        // check if for the given search context tab should be available
        this.search = search;

        this.isAvailable = enabled && (tabProperties.onlyService || !tabProperties.queries.length);
    }

    fetch() {
        return store;
    }
}

export default new ServiceGraphStateStore();
