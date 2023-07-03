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
import {ErrorHandlingStore} from '../../../../stores/errorHandlingStore';

export class SearchableKeysStore extends ErrorHandlingStore  {
    @observable keys = {};

    @action fetchKeys() {
        if (Object.keys(this.keys).length > 3) { // traceId, serviceName and operationName are default
            return;                              // don't re-trigger if other keys are available
        }
        axios
            .get('/api/traces/searchableKeys')
            .then((response) => {
                this.keys = response.data;
                if (this.keys.error) {
                    this.keys.error.values = ['true', 'false'];
                }
            })
            .catch((result) => {
                SearchableKeysStore.handleError(result);
            }
        );
    }
}

export default new SearchableKeysStore();
