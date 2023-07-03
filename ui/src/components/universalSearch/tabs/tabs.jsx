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
import PropTypes from 'prop-types';
import {observer} from 'mobx-react';
import timeWindow from '../../../utils/timeWindow';
import EmptyTab from './emptyTabPlaceholder';
import TraceResults from '../../traces/results/traceResults';
import OperationResults from '../../trends/operation/operationResults';
import Alerts from '../../alerts/alerts';
import ServiceGraph from './serviceGraph';
import ServicePerformance from './servicePerformance';
import ServiceInsights from './serviceInsights';
import tracesTabState from './tabStores/tracesTabStateStore';
import trendsTabState from './tabStores/trendsTabStateStore';
import alertsTabState from './tabStores/alertsTabStateStore';
import serviceGraphState from './tabStores/serviceGraphStateStore';
import servicePerformanceState from './tabStores/servicePerformanceStateStore';
import serviceInsightsTabStateStore from './tabStores/serviceInsightsTabStateStore';
import AlertCounter from '../../alerts/alertCounter';
import ExternalLinksList from './externalLinksList';

@observer
export default class Tabs extends React.Component {
    static propTypes = {
        search: PropTypes.object.isRequired,
        handleTabSelection: PropTypes.func.isRequired,
        history: PropTypes.object.isRequired,
        location: PropTypes.object.isRequired,
        tabProperties: PropTypes.object.isRequired
    };

    static tabs = [
        {
            tabId: 'traces',
            displayName: 'Traces',
            icon: 'ti-align-left',
            store: tracesTabState
        },
        {
            tabId: 'trends',
            displayName: 'Trends',
            icon: 'ti-stats-up',
            store: trendsTabState
        },
        {
            tabId: 'alerts',
            displayName: 'Alerts',
            icon: 'ti-bell',
            store: alertsTabState
        },
        {
            tabId: 'serviceGraph',
            displayName: 'Service Graph',
            icon: 'ti-vector',
            store: serviceGraphState
        },
        {
            tabId: 'servicePerformance',
            displayName: 'Service Performance',
            icon: 'ti-pie-chart',
            store: servicePerformanceState
        },
        {
            tabId: 'serviceInsights',
            displayName: 'Service Insights (beta)',
            icon: 'ti-pie-chart',
            store: serviceInsightsTabStateStore
        }
    ];

    static initTabs(search, tabProperties) {
        Tabs.tabs.forEach((tab) => tab.store && tab.store.init(search, tabProperties));
    }

    constructor(props) {
        super(props);

        // bindings
        this.TabViewer = this.TabViewer.bind(this);

        // init state stores for tabs
        Tabs.initTabs(props.search, props.tabProperties);
    }

    componentWillReceiveProps(nextProps) {
        Tabs.initTabs(nextProps.search, nextProps.tabProperties);
    }

    TabViewer({tabId, history, location}) {
        // trigger fetch request on store for the tab
        // TODO getting a nested store used by original non-usb components, instead pass results object
        const store = Tabs.tabs.find((tab) => tab.tabId === tabId).store.fetch();

        switch (tabId) {
            case 'traces':
                return <TraceResults tracesSearchStore={store} history={history} />;
            case 'trends':
                return (
                    <OperationResults
                        operationStore={store}
                        history={history}
                        serviceName={this.props.tabProperties.serviceName}
                        interval={this.props.tabProperties.interval}
                    />
                );
            case 'alerts':
                return (
                    <Alerts
                        alertsStore={store}
                        history={history}
                        location={location}
                        defaultPreset={timeWindow.presets[5]}
                        serviceName={this.props.tabProperties.serviceName}
                        interval={this.props.tabProperties.interval || 'FiveMinute'}
                    />
                );
            case 'serviceGraph':
                return <ServiceGraph store={store} search={this.props.search} history={history} serviceName={this.props.tabProperties.serviceName} />;
            case 'servicePerformance':
                return <ServicePerformance store={store} history={history} />;
            case 'serviceInsights':
                return <ServiceInsights store={store} search={this.props.search} history={history} />;
            default:
                return null;
        }
    }

    render() {
        const {search, history, location, handleTabSelection} = this.props;
        const availableTabs = Tabs.tabs.filter((t) => t.store.isAvailable);
        const tabId = search.tabId || (availableTabs.length && availableTabs[0].tabId); // pick traces as default
        const noTabAvailable = !availableTabs.length;

        // tab selectors for navigation between tabs
        // eslint-disable-next-line no-confusing-arrow
        const TabSelector = (tab) =>
            tab.store.isAvailable ? (
                <li key={tab.tabId} className={tab.tabId === tabId ? 'active' : ''}>
                    <a role="button" className="universal-search-bar-tabs__nav-text" tabIndex="-1" onClick={() => handleTabSelection(tab.tabId)}>
                        <span className={`usb-tab-icon ${tab.icon}`} />
                        <span>{tab.displayName}</span>
                        {tab.tabId === 'alerts' ? (
                            <div className="universal-search-bar-tabs__alert-counter">
                                <AlertCounter serviceName={this.props.tabProperties.serviceName} interval={this.props.tabProperties.interval} />
                            </div>
                        ) : null}
                    </a>
                </li>
            ) : null;

        const TabsContainer = () => (
            <article>
                <section className="container">
                    <nav>
                        <ul className="nav nav-tabs">
                            {Tabs.tabs.map((tab) => TabSelector(tab))}
                            {(window.haystackUiConfig.externalLinking && window.haystackUiConfig.externalLinking.length) ? <ExternalLinksList
                                search={search}
                            /> : null}
                        </ul>
                    </nav>
                </section>
                <section className="universal-search-tab__content">
                    <div className="container">
                        <this.TabViewer tabId={tabId} history={history} location={location} />
                    </div>
                </section>
            </article>
        );

        return noTabAvailable ? <EmptyTab /> : <TabsContainer />;
    }
}
