/*
 * Copyright 2018 Expedia, Inc.
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

const ServiceGraphSearch = ({searchString, searchStringChanged}) => (
    <section className="graph-search__wrapper">
        <input
            type="search"
            className="graph-search__input form-control"
            onChange={(event) => {
                searchStringChanged(event.currentTarget.value);
            }}
            value={searchString}
            placeholder="Filter services..."
        />
        <span className="ti-search graph-search__search-icon" />
    </section>
);

ServiceGraphSearch.propTypes = {
    searchStringChanged: PropTypes.func.isRequired,
    searchString: PropTypes.string.isRequired
};

export default ServiceGraphSearch;
