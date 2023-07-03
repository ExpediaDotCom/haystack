/* eslint-disable react/prefer-stateless-function */
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
import {withRouter} from 'react-router';
import PropTypes from 'prop-types';
import {observer} from 'mobx-react';
import _ from 'lodash';

// layout elements
import Header from '../layout/slimHeader';
import Footer from '../layout/footer';

// universal search view
import Tabs from './tabs/tabs';
import SearchBar from './searchBar/searchBar';
import {convertUrlQueryToSearch} from './utils/urlUtils';
import linkBuilder from '../../utils/linkBuilder';

// styling
import './universalSearch.less';

// SSO timeout modal
import authenticationStore from '../../stores/authenticationStore';
import AuthenticationTimeoutModal from '../layout/authenticationTimeoutModal';

// Root component for universal search
// deserialize location to create search object
// search object contains all the information to trigger any search for any subsystem in tabs
// flow search object to all the child modules, child components should not deserialize location
//
// LegacyHeader creates search object and pushes it in URL and that triggers receiveProps for UniversalSearch,
// which in turn re-triggers all tabs

const UniversalSearch = observer(({location, history}) => {
    const DEFAULT_TIME_WINDOW = '1h';

    // if no time window specified, default to last DEFAULT_TIME_WINDOW
    const createSearch = (urlQuery) => {
        const search = convertUrlQueryToSearch(urlQuery);

        if (!search.time) {
            search.time = {preset: DEFAULT_TIME_WINDOW};
        }
        return search;
    };

    const constructTabPropertiesFromSearch = (search) => {
        const queries = Object.keys(search)
            .filter(searchKey => searchKey.startsWith('query_'))
            .map(query => (search[query]));

        const keys = _.flatten(queries.map(query => Object.keys(query)));

        const onlyServiceKey = keys.length === 1 && keys[0] === 'serviceName';
        const onlyServiceAndOperationKeys = keys.length === 2 && keys.filter(key => key === 'operationName').length === 1 && keys.filter(key => key === 'serviceName').length === 1;
        const serviceName = onlyServiceKey || onlyServiceAndOperationKeys ? _.compact(queries.map(query => (query.serviceName)))[0] : null;
        const operationName = onlyServiceAndOperationKeys ? _.compact(queries.map(query => (query.operationName)))[0] : null;
        const traceId = keys.filter(key => key === 'traceId').length ? _.compact(queries.map(query => (query.traceId)))[0] : null;
        const interval = search.interval || 'FiveMinute';

        return {
            queries,
            onlyService: onlyServiceKey,
            onlyServiceAndOperation: onlyServiceAndOperationKeys,
            serviceName,
            operationName,
            traceId,
            interval
        };
    };

    const  search = createSearch(location.search);

    // on update of search in search-bar,
    // convert search to url query string and push to browser history
    const handleSearch = (newSearch) => {
        history.push(linkBuilder.universalSearchLink(newSearch));
    };

    // on update of search in search-bar,
    // convert search to url query string and push to browser history
    const handleTabSelection = (tabId) => {
        history.push(linkBuilder.universalSearchLink({...search, tabId}));
    };

    const tabProperties = constructTabPropertiesFromSearch(search);

    return (
        <article className="universal-search-panel">
            {window.haystackUiConfig.enableSSO && authenticationStore.timedOut ? <AuthenticationTimeoutModal /> : null}
            <Header />
            <SearchBar history={history} search={search} handleSearch={handleSearch} />
            <Tabs search={search} tabProperties={tabProperties} handleTabSelection={handleTabSelection} history={history} location={location} />
            <Footer />
        </article>
    );
});

UniversalSearch.propTypes = {
    location: PropTypes.object.isRequired,
    history: PropTypes.object.isRequired
};

export default withRouter(UniversalSearch);
