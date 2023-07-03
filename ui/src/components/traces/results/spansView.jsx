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

import React, {useEffect} from 'react';
import {observer} from 'mobx-react';
import PropTypes from 'prop-types';

import Loading from '../../common/loading';
import Error from '../../common/error';
import SpanResultsTable from './spanResultsTable';

const SpansView = observer(({traceIds, store}) => {
    useEffect(() => {
        store.fetchSpans(traceIds);
    }, [traceIds]);

    return (
        <section>
            { store.promiseState && store.promiseState.case({
                empty: () => <Loading />,
                pending: () => <Loading />,
                rejected: () => <Error />,
                fulfilled: () => ((store.results && store.results.length)
                    ? <SpanResultsTable results={store.results}/>
                    : <Error errorMessage="There was a problem displaying the spans. Please try again later."/>)
            })
            }
        </section>
    );
});

SpansView.propTypes = {
    traceIds: PropTypes.array.isRequired,
    store: PropTypes.object.isRequired
};

export default SpansView;

