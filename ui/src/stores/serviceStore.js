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

import { ErrorHandlingStore } from './errorHandlingStore';

export class ServiceStore extends ErrorHandlingStore {
    @observable services = [];

    @action fetchServices() {
        if (this.services.length) {
            return; // services already available, don't retrigger
        }
        if (window.haystackUiConfig.services) {
            this.services = window.haystackUiConfig.services;
            return; // services found in the html, don't retrigger
        }
        axios({
            method: 'get',
            url: '/api/services'
        })
        .then((response) => {
            this.services = response.data.sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()));
        })
        .catch((result) => {
            ServiceStore.handleError(result);
        });
    }
}

export default new ServiceStore();
