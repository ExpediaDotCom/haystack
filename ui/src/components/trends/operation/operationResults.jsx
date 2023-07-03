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
import {observer} from 'mobx-react';
import PropTypes from 'prop-types';

import Loading from '../../common/loading';
import OperationResultsTable from './operationResultsTable';
import './operationResults.less';
import Error from '../../common/error';
import OperationResultsHeatmap from './operationResultsHeatmap';

@observer
export default class operationResults extends React.Component {
    static propTypes = {
        operationStore: PropTypes.object.isRequired,
        serviceName: PropTypes.string.isRequired,
        interval: PropTypes.oneOfType([PropTypes.oneOf([null]), PropTypes.string]).isRequired
    };

    constructor(props) {
        super(props);
        this.state = {};
        this.toggleView = this.toggleView.bind(this);
    }

    toggleView() {
        this.setState({showHeatmap: !this.state.showHeatmap});
    }

    render() {
        const showHeatmap = this.state.showHeatmap;
        return (
            <section className="operation-results">
                {this.props.operationStore.statsPromiseState &&
                    this.props.operationStore.statsPromiseState.case({
                        empty: () => <Loading />,
                        pending: () => <Loading />,
                        rejected: () => <Error />,
                        fulfilled: () =>
                            this.props.operationStore.statsPromiseState && this.props.operationStore.statsResults.length ? (
                                <section>
                                    <div className="operation-results-view-selector text-center">
                                        <div className="btn-group btn-group-sm">
                                            <button
                                                className={showHeatmap ? 'btn btn-sm btn-default' : 'btn btn-sm btn-primary'}
                                                onClick={() => this.toggleView()}
                                            >
                                                Graph View
                                            </button>
                                            <button
                                                className={!showHeatmap ? 'btn btn-sm btn-default' : 'btn btn-sm btn-primary'}
                                                onClick={() => this.toggleView()}
                                            >
                                                Availability
                                            </button>
                                        </div>
                                    </div>
                                    {showHeatmap ? (
                                        <OperationResultsHeatmap
                                            operationStore={this.props.operationStore}
                                            serviceName={this.props.serviceName}
                                            interval={this.props.interval}
                                        />
                                    ) : (
                                        <OperationResultsTable
                                            operationStore={this.props.operationStore}
                                            serviceName={this.props.serviceName}
                                            interval={this.props.interval}
                                        />
                                    )}
                                </section>
                            ) : (
                                <Error errorMessage="There was a problem displaying operation results. Please try again later." />
                            )
                    })}
            </section>
        );
    }
}
