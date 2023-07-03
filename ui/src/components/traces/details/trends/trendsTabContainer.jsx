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

import React, {useEffect} from 'react';
import { observer } from 'mobx-react';
import PropTypes from 'prop-types';

import Loading from '../../../common/loading';
import Error from '../../../common/error';
import TrendsTab from './trendsTab';

const TrendsTabContainer = observer(({traceId, store}) => {
    useEffect(() => {
       store.fetchTraceDetails(traceId);
    });

    return (
        <section>
            { store.promiseState && store.promiseState.case({
                pending: () => <Loading />,
                rejected: () => <Error />,
                fulfilled: () => ((store.timelineSpans && store.timelineSpans.length)
                    ? <TrendsTab timelineSpans={store.timelineSpans} />
                    : <Error errorMessage="There was a problem displaying the trends tab. Please try again later." />)
            })
            }
        </section>
    );
});
TrendsTabContainer.propTypes = {
    traceId: PropTypes.string.isRequired,
    store: PropTypes.object.isRequired
};

export default TrendsTabContainer;
