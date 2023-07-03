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
import {useObserver} from 'mobx-react';
import PropTypes from 'prop-types';

import TraceTimeline from './traceTimeline';
import TracesContainer from './tracesContainer';
import Loading from '../../common/loading';
import Error from '../../common/error';
import NoSearch from './noSearch';
import '../traces.less';

const TraceResults = ({tracesSearchStore, history}) => useObserver(() => (
        <section>
            { !tracesSearchStore.traceId && tracesSearchStore.timelinePromiseState && tracesSearchStore.timelinePromiseState.case({
                pending: () => <div className="text-center timeline-loader">Loading timeline...</div>,
                rejected: () => <Error />,
                empty: () => <div />,
                fulfilled: () => (
                    (tracesSearchStore.timelineResults && tracesSearchStore.timelineResults.length)
                        ? <TraceTimeline store={tracesSearchStore} history={history} />
                        : null)
            })
            }
            { tracesSearchStore.traceResultsPromiseState && tracesSearchStore.traceResultsPromiseState.case({
                pending: () => <Loading />,
                rejected: () => <Error />,
                empty: () => <NoSearch />,
                fulfilled: () => ((tracesSearchStore.searchResults && tracesSearchStore.searchResults.length)
                    ? <TracesContainer
                        tracesSearchStore={tracesSearchStore}
                    />
                    : <Error errorMessage={'No results found, please try expanding your query.'}/>)
            })
            }
        </section>
        ));

TraceResults.propTypes = {
    tracesSearchStore: PropTypes.object.isRequired,
    history: PropTypes.object.isRequired
};

export default TraceResults;
