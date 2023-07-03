/*
 * Copyright 2020 Expedia, Inc.
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

import React, { useState, useRef } from 'react';
import PropTypes from 'prop-types';

import { constructExternalLinksList } from '../../../utils/externalLinkFormatter';

function constructLinks(kvPairs) {
    const externalLinks = (window.haystackUiConfig && window.haystackUiConfig.externalLinking) ? window.haystackUiConfig.externalLinking.map(link => link.key) : [];
    const matches = externalLinks.filter(key => (Object.keys(kvPairs).includes(key)));
    return matches.map(key => constructExternalLinksList(key, kvPairs[key]));
}

const ExternalLinksList = ({search}) => {
    const filteredSearchKeys = Object.keys(search).filter(key => key !== 'time' && key !== 'tabId');

    const searchKeyValuePairs = filteredSearchKeys.length && Object.assign(
        {}, ...filteredSearchKeys.map(query => (search[query]))
    );

    const constructedLinks = constructLinks(searchKeyValuePairs);
    const wrapperRef = useRef(null);
    const [linksListOpen, setLinksListOpen] = useState(false);

    const handleOutsideClick = (e) => {
        if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
            document.removeEventListener('mousedown', handleOutsideClick);
            setLinksListOpen(false);
        }
    };

    const handleTabSelection = () => {
        if (!linksListOpen) {
            document.addEventListener('mousedown', handleOutsideClick);
            setLinksListOpen(true);
        }
    };

    if (constructedLinks && constructedLinks.length) {
        return (
            <li>
                <a role="button" className="universal-search-bar-tabs__nav-text" tabIndex="-1" onClick={() => handleTabSelection()}>
                    <span className="usb-tab-icon ti-new-window" />
                    <span>External Links</span> <span className="caret" />
                </a>
                <div ref={wrapperRef} className={linksListOpen ? 'dropdown open' : 'dropdown'}>
                    <ul className="dropdown-menu" aria-labelledby="dropdownMenu1">
                        {constructedLinks}
                    </ul>
                </div>
            </li>
        );
    }
    return null;
};

ExternalLinksList.propTypes = {
    search: PropTypes.object.isRequired
};

export default ExternalLinksList;
