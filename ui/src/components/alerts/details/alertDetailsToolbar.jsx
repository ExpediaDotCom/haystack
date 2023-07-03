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
import {Link} from 'react-router-dom';
import Clipboard from 'react-copy-to-clipboard';

import linkBuilder from '../../../utils/linkBuilder';

const tracesEnabled = window.haystackUiConfig.subsystems.includes('traces');

@observer
export default class AlertDetailsToolbar extends React.Component {
    static propTypes = {
        serviceName: PropTypes.string.isRequired,
        operationName: PropTypes.string.isRequired,
        interval: PropTypes.string.isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            showCopied: false
        };
        this.handleCopy = this.handleCopy.bind(this);
    }

    handleCopy() {
        this.setState({showCopied: true});
        setTimeout(() => this.setState({showCopied: false}), 2000);
    }

    render() {
        const searchConstructor = {
            query_1: {
                serviceName: this.props.serviceName,
                operationName: this.props.operationName
            },
            interval: this.props.interval
        };

        const trendsLink = linkBuilder.universalSearchTrendsLink(searchConstructor);

        const tracesLink = linkBuilder.universalSearchTracesLink(searchConstructor);

        const alertsLink = linkBuilder.withAbsoluteUrl(linkBuilder.universalSearchAlertsLink(searchConstructor));

        return (
            <div>
                <div className="pull-left">
                    <Link
                        to={trendsLink}
                        className="btn btn-primary"
                        target="_blank"
                    >
                        <span className="ti-stats-up"/> Jump to Trends
                    </Link>
                </div>
                <div className="btn-group btn-group-sm pull-right">
                    {   tracesEnabled &&
                        <Link
                            to={tracesLink}
                            className="btn btn-default"
                            target="_blank"
                        >
                            <span className="ti-align-left"/> See Traces
                        </Link>
                    }
                    <Clipboard
                        text={alertsLink}
                        onCopy={this.handleCopy}
                    >
                        <a role="button" className="btn btn-primary"><span className="ti-link"/> Share Alert</a>
                    </Clipboard>
                    {
                        this.state.showCopied && (
                            <span className="tooltip fade left in" role="tooltip">
                            <span className="tooltip-arrow"/>
                            <span className="tooltip-inner">Link Copied!</span>
                        </span>
                        )
                    }
                </div>
            </div>
        );
    }
}
