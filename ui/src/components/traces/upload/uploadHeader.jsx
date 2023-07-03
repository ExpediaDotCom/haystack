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
import { withRouter } from 'react-router';
import PropTypes from 'prop-types';
import colorMapper from '../../../utils/serviceColorMapper';
import formatters from '../../../utils/formatters';

@observer
class UploadHeader extends React.Component {
    static propTypes = {
        traceDetailsStore: PropTypes.object.isRequired
    };

    static getServiceCounts(spans) {
        const serviceCounts = {};
        spans.map(span => (span.serviceName)).forEach((service) => { serviceCounts[service] = (serviceCounts[service] || 0) + 1; });
        return serviceCounts;
    }

    render() {
        const {traceDetailsStore} = this.props;
        const totalDuration = formatters.toDurationString(traceDetailsStore.totalDuration);
        const root = traceDetailsStore.spans[0];
        const traceId = root.traceId;
        const startTime = formatters.toTimestring(root.startTime);
        const services = UploadHeader.getServiceCounts(traceDetailsStore.spans);

        return (
            <div className="tabs-nav-container upload-large-font clearfix">
                <div className="pull-left upload-padding-left">
                    <div>
                        <div className="traces-details-trace-id__name">Trace Id: <span className="traces-details-trace-id__value">{traceId}</span></div>
                        <div className="traces-details-trace-id__name">Start Time: <span className="traces-details-trace-id__value">{startTime}</span></div>
                    </div>
                </div>
                <div className="pull-right upload-padding-right">
                    <div className="traces-details-trace-id__name">Total Duration: <span className="traces-details-trace-id__value">{totalDuration}</span></div>
                    <div className="traces-details-trace-id__name">
                        Span Count: <span className="traces-details-trace-id__value">{traceDetailsStore.spans.length}</span>
                    </div>
                </div>
                <div className="upload-inline-block">{Object.keys(services).map(svc => (
                    <span key={svc} className={`service-spans label ${colorMapper.toBackgroundClass(svc)}`}>{svc} x{services[svc]}</span>))}
                </div>
            </div>
        );
    }
}

export default withRouter(UploadHeader);
