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

const grpc = require('grpc');
const messages = require('../../../../static_codegen/subscription/subscriptionManagement_pb');
const expressionTreeBuilder = require('./expressionTreeBuilder');
const config = require('../../../config/config');
const services = require('../../../../static_codegen/subscription/subscriptionManagement_grpc_pb');
const fetcher = require('../../operations/grpcFetcher');
const putter = require('../../operations/grpcPutter');
const deleter = require('../../operations/grpcDeleter');
const poster = require('../../operations/grpcPoster');

const grpcOptions = config.grpcOptions || {};

const MetricpointNameEncoder = require('../../utils/encoders/MetricpointNameEncoder');

const metricpointNameEncoder = new MetricpointNameEncoder(config.encoder);

const client = new services.SubscriptionManagementClient(
    `${config.connectors.alerts.haystackHost}:${config.connectors.alerts.haystackPort}`,
    grpc.credentials.createInsecure(),
    grpcOptions); // TODO make client secure

const subscriptionPoster = poster('createSubscription', client);
const subscriptionPutter = putter('updateSubscription', client);
const subscriptionDeleter = deleter('deleteSubscription', client);
const getSubscriptionFetcher = fetcher('getSubscription', client); // get individual subscription
const searchSubscriptionFetcher = fetcher('searchSubscription', client); // get group of subscriptions

const converter = {};

converter.pbExpressionTreeToJson = (pbExpressionTree) => {
    const expressionTree = {};
    pbExpressionTree.operandsList.forEach((kvPair) => {
        expressionTree[kvPair.field.name] = kvPair.field.value;
    });
    return expressionTree;
};

converter.toSubscriptionJson = pbSub => ({
    subscriptionId: pbSub.subscriptionid,
    user: pbSub.user,
    dispatchersList: pbSub.dispatchersList,
    expressionTree: converter.pbExpressionTreeToJson(pbSub.expressiontree),
    lastModifiedTime: pbSub.lastmodifiedtime,
    createdTime: pbSub.createdtime
});

const connector = {};

// Get subscription from subscriptionId. Returns SubscriptionResponse.
connector.getPBSubscription = (subscriptionId) => {
    const request = new messages.GetSubscriptionRequest();
    request.setSubscriptionid(subscriptionId);

    return getSubscriptionFetcher
        .fetch(request)
        .then(result => converter.toSubscriptionJson(messages.SubscriptionResponse.toObject(false, result)));
};

// Get subscription from subscriptionId. Returns JSON Subscription.
connector.getSubscription = (subscriptionId) => {
    const pbSub = connector.getPBSubscription(subscriptionId);
    return converter.toSubscriptionJson(messages.SubscriptionResponse.toObject(false, pbSub));
};

// Search subscriptions given a set of labels. Returns a SearchSubscriptionResponse (array of SubscriptionResponses).
connector.searchSubscriptions = (serviceName, operationName, alertType, interval) => {
    const stat = alertType === 'failure-span' ? 'count' : '*_99';

    const request = new messages.SearchSubscriptionRequest();
    request.getLabelsMap()
        .set('serviceName', metricpointNameEncoder.encodeMetricpointName(decodeURIComponent(serviceName)))
        .set('operationName', metricpointNameEncoder.encodeMetricpointName(decodeURIComponent(operationName)))
        .set('metric_key', alertType)
        .set('stat', stat)
        .set('interval', interval)
        .set('product', 'haystack')
        .set('mtype', 'gauge');

    return searchSubscriptionFetcher
        .fetch(request)
        .then((result) => {
            const pbResult = messages.SearchSubscriptionResponse.toObject(false, result);
            return pbResult.subscriptionresponseList.map(pbSubResponse => converter.toSubscriptionJson(pbSubResponse));
        });
};

function constructSubscription(subscriptionObj) {
    const subscription = new messages.SubscriptionRequest();


    // construct dispatcher list containing type (email or slack) and handle
    const uiDispatchers =  subscriptionObj.dispatchers.map((inputtedDispatcher) => {
        const dispatcher = new messages.Dispatcher();

        const type = inputtedDispatcher.type.toString() === '1' ? messages.DispatchType.SLACK : messages.DispatchType.EMAIL;
        dispatcher.setType(type);
        dispatcher.setEndpoint(inputtedDispatcher.endpoint);

        return dispatcher;
    });

    subscription.setDispatchersList(uiDispatchers);
    // construct expression tree from KV pairs in subscription object (e.g. serviceName, operationName, etc)
    const expressionTree = expressionTreeBuilder.createSubscriptionExpressionTree(subscriptionObj);
    subscription.setExpressiontree(expressionTree);

    return subscription;
}

// Create a new subscription. Returns a subscription id.
connector.addSubscription = (userName, subscriptionObj) => {
    const user = new messages.User();
    user.setUsername(userName);

    const subscription = constructSubscription(subscriptionObj);

    const request = new messages.CreateSubscriptionRequest();
    request.setUser(user);
    request.setSubscriptionrequest(subscription);

    return subscriptionPoster.post(request)
        .then(result => result);
};

// Update a subscription. Checks server for changes. If none, replace existing subscription with new SubscriptionRequest
connector.updateSubscription = (id, clientSubscription) => (
    connector.getPBSubscription(id)
        .then((serverSubscription) => {
            if (serverSubscription.lastModifiedTime === clientSubscription.old.lastModifiedTime) {
                const subscription = constructSubscription(clientSubscription.modified);
                const request = new messages.UpdateSubscriptionRequest();

                request.setSubscriptionid(id);
                request.setSubscriptionrequest(subscription);
                return subscriptionPutter.put(request)
                    .then(result => result);
            }

            // todo: let UI know that subscription has already been modified
            return null;
        })
);

// Delete a subscription. Returns empty.
connector.deleteSubscription = (id) => {
    const request = new messages.DeleteSubscriptionRequest();
    request.setSubscriptionid(id);

    return subscriptionDeleter.delete(request)
        .then(() => {});
};

module.exports = connector;
