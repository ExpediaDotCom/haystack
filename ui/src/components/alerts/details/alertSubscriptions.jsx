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

import SubscriptionRow from './subscriptionRow';
import newSubscriptionConstructor from '../utils/subscriptionConstructor';

@observer
export default class AlertSubscriptions extends React.Component {
    static propTypes = {
        alertDetailsStore: PropTypes.object.isRequired,
        operationName: PropTypes.string.isRequired,
        serviceName: PropTypes.string.isRequired,
        type: PropTypes.string.isRequired,
        interval: PropTypes.string.isRequired
    };

    static handleInputKeypress(e, escapeCallback, enterCallback) {
        if (e.keyCode === 27) {
            escapeCallback();
        } else if (e.keyCode === 13) {
            enterCallback();
        }
    }

    constructor(props) {
        super(props);

        this.state = {
            showNewSubscriptionBox: false,
            subscriptionError: false,
            // subscriptionErrorMessage: null,
            selectValue: '1',
            inputValue: ''
        };

        this.submitNewSubscription = this.submitNewSubscription.bind(this);
        this.handleSubscriptionSuccess = this.handleSubscriptionSuccess.bind(this);
        this.handleSubscriptionError = this.handleSubscriptionError.bind(this);
        this.toggleNewSubscriptionBox = this.toggleNewSubscriptionBox.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSelectChange = this.handleSelectChange.bind(this);
    }


    handleInputChange(e) {
        this.setState({
            inputValue: e.target.value
        });
    }

    handleSelectChange(e) {
        this.setState({
            selectValue: e.target.value
        });
    }


    toggleNewSubscriptionBox() {
        this.setState(prevState => ({
            showNewSubscriptionBox: !prevState.showNewSubscriptionBox,
            subscriptionError: false
        }));
        this.setState({
            inputValue: '',
            selectValue: '1'
        });
    }

    handleSubscriptionSuccess() {
        this.props.alertDetailsStore.fetchAlertSubscriptions(this.props.serviceName, this.props.operationName, this.props.type, this.props.interval);
        this.setState({
            showNewSubscriptionBox: false,
            subscriptionError: false
        });
    }

    handleSubscriptionError(subscriptionErrorMessage) {
        this.setState({
            subscriptionError: true,
            subscriptionErrorMessage
        });
        setTimeout(() => this.setState({subscriptionError: false, subscriptionErrorMessage: null}), 4000);
    }

    submitNewSubscription() {
        const trimmedInput = this.state.inputValue.trim();
        if (!trimmedInput.length) {
            const endpointName = this.state.selectValue === '1' ? 'Slack channel name' : 'Email address';
            this.handleSubscriptionError(`${endpointName} cannot be empty.`);
            return;
        }
        const dispatchers = [{type: this.state.selectValue, endpoint: trimmedInput}];

        const subscription = newSubscriptionConstructor(
            this.props.serviceName,
            this.props.operationName,
            this.props.type,
            this.props.interval,
            dispatchers,
        );

        this.props.alertDetailsStore.addNewSubscription(
            subscription,
            this.handleSubscriptionSuccess,
            this.handleSubscriptionError('Error creating subscription.')
        );
    }

    render() {
        const {
            subscriptionError,
            showNewSubscriptionBox,
            subscriptionErrorMessage
        } = this.state;

        const alertSubscriptions = this.props.alertDetailsStore.alertSubscriptions;
        return (
            <section className="subscriptions col-md-5">
                <h4>Subscriptions</h4>
                <table className="subscriptions__table table">
                    <thead>
                        <tr>
                            <th width="70%">Dispatchers</th>
                            <th width="30%">Modify</th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            alertSubscriptions && alertSubscriptions.length > 0
                                ? alertSubscriptions.map(subscription =>
                                    (<SubscriptionRow
                                        key={subscription.subscriptionId}
                                        subscription={subscription}
                                        alertDetailsStore={this.props.alertDetailsStore}
                                        successCallback={this.handleSubscriptionSuccess}
                                        errorCallback={this.handleSubscriptionError}
                                    />))
                                : <tr className="non-highlight-row"><td>No Subscriptions Found</td><td /></tr>
                        }
                        <tr className={this.state.showNewSubscriptionBox ? 'non-highlight-row subscription-row' : 'hidden'}>
                            <td>
                                <select
                                    className="subscription-select form-control subscription-form"
                                    onChange={this.handleSelectChange}
                                    value={this.state.selectValue}
                                    name="type"
                                >
                                    <option value="1"> Slack</option>
                                    <option value="0"> Email</option>
                                </select>
                                <input
                                    className="dispatcher-input form-control subscription-form"
                                    onChange={this.handleInputChange}
                                    value={this.state.inputValue}
                                    onKeyDown={event => AlertSubscriptions.handleInputKeypress(event, this.toggleNewSubscriptionBox, this.handleSubmit)}
                                    placeholder={this.state.selectValue === '1' ? 'Public Slack Channel' : 'Email Address'}
                                    name="endpoint"
                                />
                            </td>
                            <td>
                                <div className="btn-group btn-group-sm">
                                    <button className="btn btn-success" onClick={this.submitNewSubscription}>
                                        <span className="ti-plus"/>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div className="text-left subscription-button">
                    {showNewSubscriptionBox ?
                        <button className="btn btn-sm btn-default" onClick={this.toggleNewSubscriptionBox}><div>Cancel</div></button> :
                        <button className="btn btn-sm btn-success" onClick={this.toggleNewSubscriptionBox}><span className="ti-plus"/> Add Subscription</button>
                    }
                </div>
                <div className={subscriptionError ? 'subscription-error' : 'hidden'}>
                    {subscriptionErrorMessage || 'Could not process subscription. Please try again.'}
                </div>
            </section>
        );
    }
}
