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

import axios from 'axios';
import {action, observable} from 'mobx';
import {fromPromise} from 'mobx-utils';
import {ErrorHandlingStore} from '../../../stores/errorHandlingStore';

export class ServiceInsightsStore extends ErrorHandlingStore {
    @observable serviceInsights = {};
    @observable promiseState = null;
    @observable filterQuery = null;
    @observable hasValidSearch = null;

    @action fetchServiceInsights(filterQuery) {
        const {serviceName, operationName, traceId, startTime, endTime, relationship} = filterQuery;

        // optional parameters
        const operationNameParam = operationName ? `&operationName=${operationName}` : '';
        const traceIdParam = traceId ? `&traceId=${traceId}` : '';
        const relationshipParam = relationship ? `&relationship=${relationship}` : '';

        this.promiseState = fromPromise(
            axios
                .get(
                    `/api/serviceInsights?serviceName=${serviceName}${operationNameParam}${traceIdParam}&startTime=${startTime}&endTime=${endTime}${relationshipParam}`
                )
                .then((result) => {
                    this.filterQuery = filterQuery;
                    this.serviceInsights = result.data;
                    return result;
                })
                .catch((result) => {
                    ServiceInsightsStore.handleError(result);
                })
        );
    }
}
export default new ServiceInsightsStore();
