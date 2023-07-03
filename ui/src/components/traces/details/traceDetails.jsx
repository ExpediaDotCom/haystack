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

import React, {useState, useEffect, useRef} from 'react';
import PropTypes from 'prop-types';
import {observer} from 'mobx-react';
import Clipboard from 'react-copy-to-clipboard';
import './traceDetails.less';
import RawTraceModal from './rawTraceModal';
import TimelineTabContainer from './timeline/timelineTabContainer';
import LatencyCostTabContainer from './latency/latencyCostTabContainer';
import TrendsTabContainer from './trends/trendsTabContainer';
import RelatedTracesTabContainer from './relatedTraces/relatedTracesTabContainer';

import { constructExternalLinksList } from '../../../utils/externalLinkFormatter';
import rawTraceStore from '../stores/rawTraceStore';
import latencyCostStore from '../stores/latencyCostStore';
import linkBuilder from '../../../utils/linkBuilder';

function tabViewer(traceId, tabSelected, traceDetailsStore) {
    switch (tabSelected) {
        case 2:
            return <LatencyCostTabContainer traceId={traceId} store={latencyCostStore} />;
        case 3:
            return <TrendsTabContainer traceId={traceId} store={traceDetailsStore} />;
        case 4:
            return <RelatedTracesTabContainer store={traceDetailsStore}/>;
        default:
            return <TimelineTabContainer traceId={traceId} store={traceDetailsStore} />;
    }
}

const TraceDetails = observer(({traceId, traceDetailsStore}) => {
    const [modalIsOpen, setModalIsOpen] = useState(false);
    const [tabSelected, setTabSelected] = useState(1);
    const [showCopied, setShowCopied] = useState(false);

    useEffect(() => {
        rawTraceStore.fetchRawTrace(traceId);
    }, [traceId]);

    const openModal = () => {
        setModalIsOpen(true);
    };

    const closeModal = () => {
        setModalIsOpen(false);
    };

    const toggleTab = (tabIndex) => {
        setTabSelected(tabIndex);
    };

    const linkWrapperRef = useRef(null);
    const [linksListOpen, setLinksListOpen] = useState(false);

    const handleOutsideClick = (e) => {
        if (linkWrapperRef.current && !linkWrapperRef.current.contains(e.target)) {
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

    const search = {query_1: {traceId}}; // TODO add specific time for trace
    const traceUrl = linkBuilder.withAbsoluteUrl(linkBuilder.universalSearchTracesLink(search));
    const rawTraceDataLink = `data:text/json;charset=utf-8,${encodeURIComponent(JSON.stringify(traceDetailsStore.spans))}`;
    const handleCopy = () => {
        setShowCopied(true);
        setTimeout(() => setShowCopied(false), 2000);
    };
    const externalLinks = constructExternalLinksList('traceId', traceId);


    return (
        <section className="table-row-details">
            <div className="tabs-nav-container clearfix">
                <h5 className="pull-left traces-details-trace-id__name">TraceId: <span className="traces-details-trace-id__value">{traceId}</span></h5>
                <div className="btn-group btn-group-sm pull-right">
                    {
                        showCopied ? (
                            <span className="tooltip fade left in" role="tooltip">
                                    <span className="tooltip-arrow" />
                                    <span className="tooltip-inner">Link Copied!</span>
                                </span>
                        ) : null
                    }
                    <a role="button" className="btn btn-default" href={rawTraceDataLink} download={`${traceId}.json`} tabIndex="-1">
                        <span className="trace-details-toolbar-option-icon ti-download"/> Download Trace
                    </a>
                    <a role="button" className="btn btn-default" onClick={openModal} tabIndex="-1"><span className="trace-details-toolbar-option-icon ti-server"/> Raw Trace</a>
                    <a role="button" className="btn btn-sm btn-default" target="_blank" href={traceUrl}><span className="ti-new-window"/> Open in new tab</a>
                    <Clipboard text={traceUrl} onCopy={handleCopy}>
                        <a role="button" className="btn btn-primary"><span className="trace-details-toolbar-option-icon ti-link"/> Share Trace</a>
                    </Clipboard>
                </div>
                <div className="trace-details-tabs pull-left full-width">
                    <ul className="nav nav-tabs">
                        <li className={tabSelected === 1 ? 'active' : ''}>
                            <a role="button" id="timeline-view" tabIndex="-1" onClick={() => toggleTab(1)} >Timeline</a>
                        </li>
                        { window.haystackUiConfig && window.haystackUiConfig.subsystems.includes('trends') ? (
                            <>
                                <li className={tabSelected === 2 ? 'active' : ''}>
                                    <a role="button" id="latency-view" tabIndex="-1" onClick={() => toggleTab(2)} >Latency Cost</a>
                                </li>
                                <li className={tabSelected === 3 ? 'active' : ''}>
                                    <a role="button" id="trends-view" tabIndex="-1" onClick={() => toggleTab(3)} >Trends</a>
                                </li>
                            </>) : null }
                        { window.haystackUiConfig && window.haystackUiConfig.relatedTracesOptions && window.haystackUiConfig.relatedTracesOptions.length > 0 ? (
                            <li className={tabSelected === 4 ? 'active' : ''}>
                                <a role="button" id="related-view" tabIndex="-1" onClick={() => toggleTab(4)} >Related Traces</a>
                            </li>) : null }
                        { externalLinks && externalLinks.length > 0 ? (
                            <li>
                                <a role="button" tabIndex="-1" onClick={() => handleTabSelection()}>
                                    <span className="usb-tab-icon ti-new-window" />
                                    <span>External Links</span> <span className="caret" />
                                </a>
                                <div ref={linkWrapperRef} className={linksListOpen ? 'dropdown open' : 'dropdown'}>
                                    <ul className="dropdown-menu" aria-labelledby="dropdownMenu1">
                                        {externalLinks}
                                    </ul>
                                </div>
                            </li>) : null }
                    </ul>
                </div>
            </div>

            <section>{tabViewer(traceId, tabSelected, traceDetailsStore)}</section>

            <RawTraceModal isOpen={modalIsOpen} closeModal={closeModal} traceId={traceId} rawTraceStore={rawTraceStore}/>
        </section>
    );
});

TraceDetails.propTypes = {
    traceId: PropTypes.string.isRequired,
    traceDetailsStore: PropTypes.object.isRequired
};

export default TraceDetails;
