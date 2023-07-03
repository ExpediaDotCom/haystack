import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {observer} from 'mobx-react';
import {toJS} from 'mobx';
import SpansView from './spansView';
import SpanSearchStore from '../stores/spansSearchStore';
import TraceResultsTable from './traceResultsTable';

const TracesContainer = observer(({tracesSearchStore}) => {
    const {timelineResults, searchQuery, searchResults, totalCount} = toJS(tracesSearchStore);
    const traceIds = tracesSearchStore.searchResults.map(t => t.traceId);
    const [showSpans, toggleView] = useState(false);

    return (
        <article>
            <div className="trace-result-summary">
                {searchResults.length > 1 ?
                    <span>
                            <span>Showing latest <b>{searchResults.length}</b> {searchResults.length === 1 ? 'trace' : 'traces'} out of total {totalCount ? <b>{totalCount}</b> : null} for time window. </span>
                        {
                            timelineResults && timelineResults.length
                                ? <span className="text-muted text-right">Select a timeline bar to drill down.</span>
                                : null
                        }
                        </span> : null}
            </div>
            <div className="trace-result-view-selector text-center">
                <div className="btn-group btn-group-sm">
                    <button className={showSpans ? 'btn btn-sm btn-default' : 'btn btn-sm btn-primary'} onClick={() => toggleView(!showSpans)}>Traces View</button>
                    <button className={!showSpans ? 'btn btn-sm btn-default' : 'btn btn-sm btn-primary'} onClick={() => toggleView(!showSpans)}>Spans View</button>
                </div>
            </div>
            <section>
                {
                    showSpans
                        ? (<SpansView
                            traceIds={traceIds}
                            location={{}}
                            store={SpanSearchStore}
                        />)
                        : (<TraceResultsTable
                            query={searchQuery}
                            results={searchResults}
                        />)
                }
            </section>
        </article>
    );
});

TracesContainer.propTypes = {
    tracesSearchStore: PropTypes.object.isRequired
};

export default TracesContainer;
