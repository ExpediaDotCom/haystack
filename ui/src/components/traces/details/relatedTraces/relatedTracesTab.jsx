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
import {PropTypes as MobxPropTypes} from 'mobx-react';

import RelatedTracesRow from './relatedTracesRow';
import linkBuilder from '../../../../utils/linkBuilder';

const RelatedTracesTab = ({searchQuery, relatedTraces}) => {
    const numDisplayedTraces = 10;

    const showMoreTraces = () => {
        const traceUrl = linkBuilder.withAbsoluteUrl(linkBuilder.universalSearchTracesLink(searchQuery));

        const tab = window.open(traceUrl, '_blank');
        tab.focus();
    };

    const relatedTracesList = relatedTraces.slice(0, numDisplayedTraces);

    return (
        <article>
            <table className="trace-trend-table">
                <thead className="trace-trend-table_header">
                <tr>
                    <th width="20" className="trace-trend-table_cell">Start Time</th>
                    <th width="30" className="trace-trend-table_cell">Root</th>
                    <th width="20" className="trace-trend-table_cell">Root Success</th>
                    <th width="60" className="trace-trend-table_cell">Span Count</th>
                    <th width="20" className="trace-trend-table_cell text-right">Total Duration</th>
                </tr>
                </thead>
                <tbody>
                {
                    relatedTracesList.map(relatedTrace => (
                        <RelatedTracesRow
                            key={relatedTrace.traceId}
                            {...relatedTrace}
                        />
                    ))
                }
                </tbody>
            </table>
            <footer>
                <div>{relatedTraces.length > numDisplayedTraces ? `10 of ${relatedTraces.length} Results` : 'End of Results'}</div>
                <div className="text-center">
                    <a role="button" className="btn btn-primary" onClick={showMoreTraces} tabIndex="-1">{relatedTraces.length > numDisplayedTraces ? `Show All(${relatedTraces.length}) in Universal` : 'View in Universal Search' }</a>
                </div>
            </footer>
        </article>
    );
};

RelatedTracesTab.propTypes = {
    searchQuery: PropTypes.object.isRequired,
    relatedTraces: MobxPropTypes.observableArray
};

RelatedTracesTab.defaultProps = {
    relatedTraces: []
};

export default RelatedTracesTab;
