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
import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {observer} from 'mobx-react';
import _ from 'lodash';

import CountGraph from './countGraph';
import DurationGraph from './durationGraph';
import SuccessGraph from './successGraph';
import './graphContainer.less';

const GraphContainer = observer(({trendsStore}) => {
    const {count, meanDuration, tp95Duration, tp99Duration, failureCount, successCount} = trendsStore.trendsResults;
    const {from, until} = trendsStore.trendsQuery;
    const [xAxesTicks, setXAxesTicks] = useState({
        min: from,
        max: until
    });

    const [showResetZoom, setShowResetZoom] = useState(false);

    const handleZoomReset = () => {
        const sortedCountPoints = _.sortBy(count, (datapoint) => datapoint.timestamp);
        setXAxesTicks({
            min: sortedCountPoints[0].timestamp,
            max: sortedCountPoints[sortedCountPoints.length - 1].timestamp
        });
        setShowResetZoom(false);
    };

    return (
        <div className="row">
            {showResetZoom ? (
                <button className="reset-button btn-sm pull-right" onClick={() => handleZoomReset()}>
                    Reset Zoom
                </button>
            ) : null}
            <CountGraph
                setShowResetZoom={setShowResetZoom}
                xAxesTicks={xAxesTicks}
                setXAxesTicks={setXAxesTicks}
                countPoints={count}
                successPoints={successCount}
                failurePoints={failureCount}
            />
            <DurationGraph
                setShowResetZoom={setShowResetZoom}
                xAxesTicks={xAxesTicks}
                setXAxesTicks={setXAxesTicks}
                meanPoints={meanDuration}
                tp95Points={tp95Duration}
                tp99Points={tp99Duration}
            />
            <SuccessGraph
                setShowResetZoom={setShowResetZoom}
                xAxesTicks={xAxesTicks}
                setXAxesTicks={setXAxesTicks}
                successCount={successCount}
                failureCount={failureCount}
            />
        </div>
    );
});

GraphContainer.propTypes = {
    trendsStore: PropTypes.object.isRequired
};

export default GraphContainer;
