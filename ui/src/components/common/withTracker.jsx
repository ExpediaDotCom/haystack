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

import React, {Component} from 'react';
import ReactGA from 'react-ga';
import PropTypes from 'prop-types';

const gaTrackingID = (window.haystackUiConfig && window.haystackUiConfig.gaTrackingID) || null;
ReactGA.initialize(gaTrackingID);

const trackPage = (page) => {
    ReactGA.set({page});
    ReactGA.pageview(page);
};

const withTracker = (WrappedComponent) => {
    class Wrapper extends Component {
        static propTypes = {
            location: PropTypes.objectOf(PropTypes.string).isRequired
        };

        componentDidMount() {
            const page = this.props.location.pathname;
            trackPage(page);
        }

        componentWillReceiveProps(nextProps) {
            const currentPage = this.props.location.pathname;
            const nextPage = nextProps.location.pathname;

            if (currentPage !== nextPage) {
                trackPage(nextPage);
            }
        }

        render() {
            return <WrappedComponent {...this.props} />;
        }
    }

    return Wrapper;
};

export default withTracker;
