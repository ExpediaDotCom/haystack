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
import _ from 'lodash';

import { ErrorHandlingStore } from '../../../stores/errorHandlingStore';

export class AlertDetailsStore extends ErrorHandlingStore {
    @observable alertHistory = [];
    @observable alertSubscriptions = [];
    @observable historyPromiseState = null;
    @observable subscriptionsPromiseState = null;


    @action fetchAlertHistory(serviceName, operationName, type, from, interval) {
        this.historyPromiseState = fromPromise(
            axios
                .get(`/api/alert/${encodeURIComponent(serviceName)}/${encodeURIComponent(operationName)}/${type}/history?from=${from}&interval=${interval}`)
                .then((result) => {
                    this.alertHistory = result.data;
                })
                .catch((result) => {
                    AlertDetailsStore.handleError(result);
                })
        );
    }

    @action fetchAlertSubscriptions(serviceName, operationName, alertType, interval) {
        this.subscriptionsPromiseState = fromPromise(
            axios
                .get(`/api/alert/${encodeURIComponent(serviceName)}/${encodeURIComponent(operationName)}/${alertType}/${interval}/subscriptions`)
                .then((result) => {
                    this.alertSubscriptions = result.data;
                })
                .catch((result) => {
                    AlertDetailsStore.handleError(result);
                })
        );
    }

    @action addNewSubscription(subscription, successCallback, errorCallback) {
        this.subscriptionsPromiseState = fromPromise(
            axios
                .post(`/api/alert/${encodeURIComponent(subscription.expressionTree.serviceName)}/${encodeURIComponent(subscription.expressionTree.operationName)}/${subscription.expressionTree.type}/subscriptions`, {subscription})
                .then(() => {
                    this.subscriptionsPromiseState = {case: ({pending}) => pending()}; // Show pending while we wait for refresh
                    setTimeout(successCallback, 1000); // todo: find a better solution to this
                })
                .catch((result) => {
                    errorCallback();
                    AlertDetailsStore.handleError(result);
                })
        );
    }

    @action updateSubscription(subscriptions, successCallback, errorCallback) {
        this.subscriptionsPromiseState = fromPromise(
            axios
                .put(`/api/alert/subscriptions/${subscriptions.old.subscriptionId}`, {subscriptions})
                .then(() => {
                    this.subscriptionsPromiseState = {case: ({pending}) => pending()}; // Show pending while we wait for refresh
                    setTimeout(successCallback, 1000); // todo: find a better solution to this
                })
                .catch((result) => {
                    errorCallback();
                    AlertDetailsStore.handleError(result);
                })
        );
    }

    @action deleteSubscription(subscriptionId) {
        this.subscriptionsPromiseState = fromPromise(
            axios
                .delete(`/api/alert/subscriptions/${subscriptionId}`)
                .then(() => {
                    _.remove(this.alertSubscriptions, subscription => subscription.subscriptionId === subscriptionId);
                })
                .catch((result) => {
                    AlertDetailsStore.handleError(result);
                })
        );
    }
}

export default new AlertDetailsStore();
