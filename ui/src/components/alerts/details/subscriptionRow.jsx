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


import React from 'react';
import PropTypes from 'prop-types';
import {observer} from 'mobx-react';

import newSubscriptionConstructor from '../utils/subscriptionConstructor';

@observer
export default class SubscriptionRow extends React.Component {
    static propTypes = {
        subscription: PropTypes.object.isRequired,
        alertDetailsStore: PropTypes.object.isRequired,
        successCallback: PropTypes.func.isRequired,
        errorCallback: PropTypes.func.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            modifySubscription: false,
            dispatcher: JSON.parse(JSON.stringify(this.props.subscription.dispatchersList[0])) // deep copy, so client side changes don't affect store
        };

        this.handleDeleteSubscription = this.handleDeleteSubscription.bind(this);
        this.handleSubmitModifiedSubscription = this.handleSubmitModifiedSubscription.bind(this);
        this.handleCancelModifiedSubscription = this.handleCancelModifiedSubscription.bind(this);
        this.handleModifySubscription = this.handleModifySubscription.bind(this);
        this.updateDispatcherType = this.updateDispatcherType.bind(this);
        this.updateDispatcherEndpoint = this.updateDispatcherEndpoint.bind(this);
    }

    handleSubmitModifiedSubscription() {
        const oldSubscription = this.props.subscription;
        const dispatcher = this.state.dispatcher;
        dispatcher.endpoint = dispatcher.endpoint.trim();
        if (!dispatcher.endpoint.length) {
            const endpointName = dispatcher.type.toString() === '1' ? 'Updated Slack channel name' : 'Updated email address';
            this.props.errorCallback(`${endpointName} cannot be empty.`);
            return;
        }
        const dispatchers = [dispatcher];

        const {serviceName, operationName, type, interval} = this.props.subscription.expressionTree;

        const modifiedSubscription = newSubscriptionConstructor(
            serviceName,
            operationName,
            type,
            interval,
            dispatchers,
        );
        const subscriptions = {old: oldSubscription, modified: modifiedSubscription};
        this.props.alertDetailsStore.updateSubscription(
            subscriptions,
            this.props.successCallback,
            this.props.errorCallback('Error updating subscription.')
        );
        this.setState({activeModifyInput: null});
    }

    handleCancelModifiedSubscription() {
        this.setState({
            modifySubscription: false,
            dispatcher: JSON.parse(JSON.stringify(this.props.subscription.dispatchersList[0]))
        });
    }

    handleModifySubscription() {
        this.setState({
            modifySubscription: true
        });
    }

    handleDeleteSubscription(subscriptionId) {
        this.props.alertDetailsStore.deleteSubscription(
            subscriptionId
        );
    }

    updateDispatcherType(e) {
        this.setState({dispatcher: {type: e.target.value, endpoint: this.state.dispatcher.endpoint}});
    }

    updateDispatcherEndpoint(e) {
        this.setState({dispatcher: {type: this.state.dispatcher.type, endpoint: e.target.value}});
    }


    render() {
        const dispatcher = this.state.dispatcher;

        const HandleSubscriptionModifyButtons = () => (<div className="btn-group btn-group-sm">
            <button onClick={() => this.handleSubmitModifiedSubscription()} className="btn btn-success alert-modify-submit">
                <span className="ti-check"/>
            </button>
            <button onClick={() => this.handleCancelModifiedSubscription()} className="btn btn-danger alert-modify-cancel">
                <span className="ti-na"/>
            </button>
        </div>);

        const DefaultSubscriptionButtons = () => (<div className="btn-group btn-group-sm">
            <button onClick={() => this.handleModifySubscription()} className="btn btn-default alert-modify">
                <span className="ti-pencil"/>
            </button>
            <button onClick={() => this.handleDeleteSubscription(this.props.subscription.subscriptionId)} className="btn btn-default">
                <span className="ti-trash"/>
            </button>
        </div>);

        return (
            <tr className="non-highlight-row subscription-row">
                <td className={!this.state.modifySubscription ? 'default-cursor' : null}>
                    <div className="subscription-dispatcher-row">
                        <select
                            className="subscription-select form-control subscription-form"
                            value={dispatcher.type.toString()}
                            name="type"
                            disabled={!this.state.modifySubscription}
                            onChange={this.updateDispatcherType}
                        >
                            <option value="1"> Slack</option>
                            <option value="0"> Email</option>
                        </select>
                        <input
                            className="dispatcher-input form-control subscription-form"
                            placeholder={dispatcher.type.toString() === '1' ? 'Public Slack Channel' : 'Email Address'}
                            value={dispatcher.endpoint}
                            name="endpoint"
                            disabled={!this.state.modifySubscription}
                            onChange={this.updateDispatcherEndpoint}
                        />
                        <br />
                    </div>
                </td>
                <td>
                    {
                        this.state.modifySubscription ? <HandleSubscriptionModifyButtons /> : <DefaultSubscriptionButtons />
                    }
                </td>
            </tr>

        );
    }
}
