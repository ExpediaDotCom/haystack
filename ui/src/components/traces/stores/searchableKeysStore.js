/*
 * Copyright 2018 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
import axios from 'axios';
import {observable, action} from 'mobx';

export class SearchableKeysStore {
    @observable keys = [];

    @action fetchKeys() {
        if (this.keys.length) {
            return; // services already available, don't retrigger
        }
        axios({
            method: 'get',
            url: '/api/traces/searchableKeys'
        })
        .then((response) => {
            this.keys = response.data.sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()));
        });
    }
}

export default new SearchableKeysStore();
