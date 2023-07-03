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
import {observer} from 'mobx-react';

import TrendDetailsToolbar from './trendDetailsToolbar';
import GraphContainer from './graphs/graphContainer';
import Error from '../../common/error';
import Loading from '../../common/loading';
import './trendDetails.less';

@observer
export default class TrendDetails extends React.Component {
    static propTypes = {
        store: PropTypes.object.isRequired,
        serviceName: PropTypes.string.isRequired,
        serviceSummary: PropTypes.bool,
        opName: PropTypes.string,
        statsType: PropTypes.string,
        interval: PropTypes.oneOfType([PropTypes.oneOf([null]), PropTypes.string]).isRequired
    };

    static defaultProps = {
        serviceSummary: false,
        opName: null,
        statsType: null
    };

    render() {
        return (
            <div className="table-row-details">
                <TrendDetailsToolbar
                    serviceSummary={this.props.serviceSummary}
                    trendsStore={this.props.store}
                    serviceName={this.props.serviceName}
                    opName={this.props.opName}
                    statsType={this.props.statsType}
                    interval={this.props.interval}
                />
                { this.props.store.trendsPromiseState && this.props.store.trendsPromiseState.case({
                    empty: () => <Loading />,
                    pending: () => <Loading />,
                    rejected: () => <Error />,
                    fulfilled: () => (this.props.store.trendsResults && Object.keys(this.props.store.trendsResults).length
                        ? <GraphContainer trendsStore={this.props.store} serviceName={this.props.serviceName}/>
                        : <Error errorMessage="There was a problem displaying trend details. Please try again later." />)
                })
                }
            </div>
        );
    }
}
