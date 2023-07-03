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

import React from 'react';
import {observer} from 'mobx-react';
import PropTypes from 'prop-types';

@observer
export default class ServiceInsights extends React.Component {
    static propTypes = {
        history: PropTypes.object.isRequired,
        search: PropTypes.object.isRequired,
        store: PropTypes.object.isRequired
    };

    state = {};

    componentDidMount() {
        import(/* webpackChunkName: "ServiceInsights", webpackPreload: true */ '../../serviceInsights/serviceInsights').then((mod) => {
            this.setState({ServiceInsightsView: mod.default});
        });
    }

    render() {
        const ServiceInsightsView = this.state.ServiceInsightsView;

        return (
            <section>
                {ServiceInsightsView && <ServiceInsightsView store={this.props.store} search={this.props.search} history={this.props.history} />}
            </section>
        );
    }
}
