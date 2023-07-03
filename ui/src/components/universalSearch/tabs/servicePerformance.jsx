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

import React from 'react';
import { observer } from 'mobx-react';
import PropTypes from 'prop-types';

import servicePerfStore from '../../servicePerf/stores/servicePerfStore';

@observer
export default class ServicePerformance extends React.Component {
    static propTypes = {
        history: PropTypes.object.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {};
        const until = Date.now();
        const from = until - (15 * 60 * 1000);
        servicePerfStore.fetchServicePerf('5-min', from, until);
    }

    componentDidMount() {
        import(/* webpackChunkName: "servicePerformance", webpackPreload: true */ '../../servicePerf/servicePerformance')
        .then((mod) => {
            this.setState({ServicePerformanceView: mod.default});
        });
    }

    render() {
        const ServicePerformanceView = this.state.ServicePerformanceView;

        return (
            <section>
                {ServicePerformanceView &&
                    <ServicePerformanceView servicePerfStore={servicePerfStore} servicePerfStats={servicePerfStore.servicePerfStats} history={this.props.history} />
                }
            </section>
        );
    }
}
