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

const Q = require('q');

const subscriptions = (serviceName, operationName, alertType, interval) => (
    [
        {
            subscriptionId: 101,
            user: {userName: 'haystack-team'},
            dispatchersList: [
                {
                    type: 1,
                    endpoint: '#haystack'
                }
            ],
            expressionTree: {
                serviceName,
                operationName,
                metric_key: alertType,
                interval,
                stat: alertType === 'failure-span' ? 'count' : '*_99',
                mtype: 'gauge',
                product: 'haystack'
            }
        },
        {
            subscriptionId: 102,
            user: {userName: 'haystack-team'},
            dispatchersList: [
                {
                    type: 0,
                    endpoint: 'haystack@opentracing.io'
                }
            ],
            expressionTree: {
                serviceName,
                operationName,
                metric_key: alertType,
                interval,
                stat: alertType === 'failure-span' ? 'count' : '*_99',
                mtype: 'gauge',
                product: 'haystack'
            }
        }
    ]
);

function searchSubscriptions(serviceName, operationName, alertType, interval) {
    if (serviceName && operationName && alertType) {
        return subscriptions(serviceName, operationName, alertType, interval);
    }
    throw new Error('Unable to get subscriptions');
}

function addSubscription(userName, subscriptionObj) {
    if (userName && subscriptionObj) {
        return Math.floor(Math.random() * 300);
    }
    throw new Error('Unable to add subscription');
}

function updateSubscription(id, subscription) {
    if (id && subscription && subscription.old && subscription.modified) {
        return;
    }
    throw new Error('Unable to update subscription');
}

function deleteSubscription(subscriptionId) {
    if (subscriptionId) {
        return;
    }
    throw new Error('Unable to delete subscription');
}

const connector = {};

connector.searchSubscriptions = (serviceName, operationName, alertType, interval) => Q.fcall(() => searchSubscriptions(serviceName, operationName, alertType, interval));

connector.addSubscription = (userName, subscriptionObj) => Q.fcall(() => addSubscription(
    userName || 'haystack',
    subscriptionObj)
);

connector.updateSubscription = (id, subscription) => Q.fcall(() => {
    updateSubscription(id, subscription);
});

connector.deleteSubscription = subscriptionId => Q.fcall(() => deleteSubscription(subscriptionId));


module.exports = connector;
