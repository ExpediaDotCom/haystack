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
import {Sparklines, SparklinesCurve} from 'react-sparklines';
import PropTypes from 'prop-types';

import formatters from '../../../utils/formatters';

const sparklines = {};

sparklines.CountSparkline = ({total, points}) =>
    (<div className="sparkline-container">
        { !isNaN(total) &&
            <div className="sparkline-title">total <b>{formatters.toNumberString(total)}</b></div>
        }
        <div className="sparkline-graph">
            <Sparklines className="sparkline" data={points} min={0} height={48}>
                <SparklinesCurve style={{ strokeWidth: 1.5 }} color="#36a2eb" />
            </Sparklines>
        </div>
    </div>);

sparklines.CountSparkline.propTypes = {
    total: PropTypes.number.isRequired,
    points: PropTypes.array.isRequired
};

sparklines.DurationSparkline = ({latest, points}) =>
    (<div className="sparkline-container">
        { !isNaN(latest) &&
            <div className="sparkline-title">latest <b>{formatters.toDurationString(latest)}</b></div>
        }
        <div className="sparkline-graph">
            <Sparklines className="sparkline" data={points} min={0} height={48}>
                <SparklinesCurve style={{ strokeWidth: 1 }} color="#e23474" />
            </Sparklines>
        </div>
    </div>);

sparklines.DurationSparkline.propTypes = {
    latest: PropTypes.number.isRequired,
    points: PropTypes.array.isRequired
};

sparklines.SuccessPercentSparkline = ({average, points}) => {
    if (average === null) {
        return null;
    }

    let stateColor = 'green';
    if (average < 95) {
        stateColor = 'red';
    } else if (average < 99) {
        stateColor = 'orange';
    }

    return (<div className="sparkline-container ">
        { !isNaN(average) &&
            <div className={'sparkline-title'}>average <b className={`sparkline-percentColor ${stateColor}`}> {average.toFixed(2)}% </b></div>
        }
        <div className="sparkline-graph">
            <Sparklines className="sparkline" data={points} min={0} max={100} height={48}>
                <SparklinesCurve style={{ strokeWidth: 1.5 }} color="#4bc0c0" />
            </Sparklines>
        </div>
    </div>);
};

sparklines.SuccessPercentSparkline.propTypes = {
    average: PropTypes.number.isRequired,
    points: PropTypes.array.isRequired
};

export default sparklines;
