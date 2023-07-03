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

import { ErrorHandlingStore } from './errorHandlingStore';

export class OperationStore extends ErrorHandlingStore {
    @observable operations = [];

    @action fetchOperations(serviceName, callback) {
        this.operations = [];
        axios({
            method: 'get',
            url: `/api/operations?serviceName=${serviceName}`
            })
            .then((response) => {
                this.operations = ['all', ...response.data.sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()))];
                callback();
            })
            .catch((result) => {
                OperationStore.handleError(result);
            });
    }
}

export default new OperationStore();
