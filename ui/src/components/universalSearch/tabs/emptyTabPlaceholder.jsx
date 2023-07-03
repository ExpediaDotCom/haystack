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

export default () => (
    <section className="universal-search-tab__content">
        <div className="container">
            <section className="text-center">
                <div className="no-search_text">
                    <h5>
                        <span>Start with a query for serviceName, traceId, or any other </span>
                        <a href="/api/traces/searchableKeys" target="_blank" className="underlined-anchor">searchable key</a>
                    </h5>
                    <p>
                        e.g. <span className="traces-error-message__code">serviceName=test-service</span>
                    </p>
                </div>
            </section>
        </div>
    </section>
);
