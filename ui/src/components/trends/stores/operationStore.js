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

import axios from 'axios';
import {observable, action} from 'mobx';
import { fromPromise } from 'mobx-utils';

import { toQueryUrlString } from '../../../utils/queryParser';
import { ErrorHandlingStore } from '../../../stores/errorHandlingStore';

export class OperationStore extends ErrorHandlingStore {
    @observable statsPromiseState = { case: ({empty}) => empty() };
    @observable trendsPromiseState = { case: ({empty}) => empty() };
    @observable statsResults = [];
    @observable statsQuery = {};
    @observable trendsResults = [];
    @observable trendsQuery = {};

    @action fetchStats(service, query, isCustomTimeRange, filters) {
        const queryUrlString = toQueryUrlString(query);
        this.statsPromiseState = fromPromise(
                axios
                    .get(`/api/trends/operation/${service}?${queryUrlString}`)
                    .then((result) => {
                        this.statsQuery = {...query, isCustomTimeRange, filters};
                        this.statsResults = result.data;
                    })
                    .catch((result) => {
                        this.statsQuery = {...query, isCustomTimeRange, filters};
                        this.statsResults = [];
                        OperationStore.handleError(result);
                    })
        );
    }
    @action fetchTrends(service, operation, query) {
        const queryUrlString = toQueryUrlString(query);
        this.trendsPromiseState = fromPromise(
            axios
                .get(`/api/trends/operation/${service}/${operation}?${queryUrlString}`)
                .then((result) => {
                    this.trendsResults = result.data;
                    this.trendsQuery = query;
                })
                .catch((result) => {
                    this.trendsQuery = query;
                    this.trendsResults = [];
                    OperationStore.handleError(result);
                })
        );
    }
}

export default new OperationStore();
