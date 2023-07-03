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
import PropTypes from 'prop-types';
import { observer } from 'mobx-react';

import '../../serviceGraph/serviceGraph.less';

@observer
export default class ServiceGraph extends React.Component {
    static propTypes = {
        history: PropTypes.object.isRequired,
        search: PropTypes.object.isRequired,
        store: PropTypes.object.isRequired,
        serviceName: PropTypes.string
    };

    static defaultProps = {
        serviceName: undefined
    }

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        import(/* webpackChunkName: "serviceGraphContainer", webpackPreload: true */ '../../serviceGraph/serviceGraphContainer')
        .then((mod) => {
            this.setState({ServiceGraphContainer: mod.default});
        });
    }

    render() {
        const ServiceGraphContainer = this.state.ServiceGraphContainer;
        return (
            <section className="service-graph-panel">
                {ServiceGraphContainer && <ServiceGraphContainer graphStore={this.props.store} search={this.props.search} history={this.props.history} serviceName={this.props.serviceName} isUniversalSearch /> }
            </section>
        );
    }
}
