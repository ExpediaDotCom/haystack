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

import React, {useState, useEffect} from 'react';
import { observer } from 'mobx-react';
import PropTypes from 'prop-types';

import Loading from '../../../common/loading';
import Error from '../../../common/error';
import RelatedTracesTab from './relatedTracesTab';
import { toPresetDisplayText } from '../../utils/presets';

const RelatedTracesTabContainer = observer(({store, traceId}) => {
    const timePresetOptions = (window.haystackUiConfig && window.haystackUiConfig.tracesTimePresetOptions);

    const fieldOptions = (window.haystackUiConfig && window.haystackUiConfig.relatedTracesOptions);
    const [selectedFieldIndex, setSelectedFieldIndex] = useState(null);
    const [selectedTimeIndex, setSelectedTimeIndex] = useState(2); // The default time preset is the third

    /**
     * The following computes a dictionary tags of all spans of this trace
     * This computation relies that the spans have already been calculated in the traceDetailsStore, which happens
     * when the Timeline View (which is default) is viewed, and fetchTraceDetails has complete.
     */
    const tags = store.tags;

    const fetchRelatedTraces = () => {
        // If the field is unselected
        if (selectedFieldIndex === null) {
            return store.rejectRelatedTracesPromise('Select a field to find related traces');
        }

        const chosenField = fieldOptions[selectedFieldIndex];

        // Rejects API promise if the trace does not have the chosen field
        if (!tags[chosenField.propertyToMatch] && chosenField.propertyToMatch !== 'traceId') {
            return store.rejectRelatedTracesPromise('This trace does not have the chosen field');
        }

        const selectedTraceId = chosenField.propertyToMatch === 'traceId' ? traceId : null;

        // Builds Query
        const query =  {
            serviceName: '',
            [chosenField.fieldTag]: selectedTraceId || tags[chosenField.propertyToMatch],
            timePreset: timePresetOptions[selectedTimeIndex].shortName
        };

        return store.fetchRelatedTraces(query);
    };

    const handleFieldChange = (event) => {
        setSelectedFieldIndex(event.target.value);
    };

    const handleTimeChange = (event) => {
        setSelectedTimeIndex(event.target.value);
    };

    useEffect(() => {
        fetchRelatedTraces(selectedTimeIndex);
    }, [selectedFieldIndex, selectedTimeIndex]);

    const MessagePlaceholder = ({message}) =>
        (<section className="text-center">
            <div className="no-search_text">
                <h5>{message}</h5>
            </div>
        </section>);

    MessagePlaceholder.propTypes = {
        message: PropTypes.string.isRequired
    };


    return (
        <section>
            <div className="text-left">
                <span>Find Related Traces by: </span>
                <select id="field" className="time-range-selector" value={selectedFieldIndex || ''} onChange={handleFieldChange}>
                    {!selectedFieldIndex ? <option key="empty" value="">{'<select field>'}</option> : null}
                    {fieldOptions.map((fieldOp, index) => (
                        <option
                            key={fieldOp.fieldTag}
                            value={index}
                        >{fieldOp.fieldDescription}</option>))}
                </select>
                <select id="time" className="pull-right time-range-selector" value={selectedTimeIndex} onChange={handleTimeChange}>
                    {timePresetOptions.map((preset, index) => (
                        <option
                            key={preset.shortName}
                            value={index}
                        >{toPresetDisplayText(preset.shortName)}</option>))}
                </select>
            </div>
            { store.relatedTracesPromiseState && store.relatedTracesPromiseState.case({
                empty: () => <></>,
                pending: () => <Loading />,
                rejected: message => <MessagePlaceholder message={message}/>,
                fulfilled: () => ((store.relatedTraces && store.relatedTraces.length)
                    ? <RelatedTracesTab searchQuery={store.searchQuery} relatedTraces={store.relatedTraces}/>
                    : <Error errorMessage="No related traces found"/>)
            })
            }
        </section>
    );
});

RelatedTracesTabContainer.propTypes = {
    store: PropTypes.object.isRequired
};



export default RelatedTracesTabContainer;
