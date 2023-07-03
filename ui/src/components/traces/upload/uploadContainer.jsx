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

import UploadHeader from './uploadHeader';
import Header from '../../layout/slimHeader';
import Footer from '../../layout/footer';
import TimelineTab from '../details/timeline/timelineTab';
import traceDetailsStore from '../stores/traceDetailsStore';
import Error from '../../common/error';
import '../traces.less';


const UploadContainer = observer(() => {
    const toggleExpand = (id) => {
        traceDetailsStore.toggleExpand(id);
    };

    return (
        <section>
            <Header />
            <section className="universal-search-tab__content">
                <section className="container table-row-details table-row-details-upload">
                    {traceDetailsStore.spans && traceDetailsStore.spans.length > 0 ?
                        <>
                            <UploadHeader
                                traceDetailsStore={traceDetailsStore}
                            />
                            <TimelineTab
                                timelineSpans={traceDetailsStore.timelineSpans}
                                totalDuration={traceDetailsStore.totalDuration}
                                startTime={traceDetailsStore.startTime}
                                toggleExpand={toggleExpand}
                            />
                        </> :
                        <Error errorMessage="Invalid JSON or file. Please ensure the uploaded file is a valid downloaded trace"/>
                    }
                </section>
            </section>
            <Footer />
        </section>
    );
});

export default withRouter(UploadContainer);
