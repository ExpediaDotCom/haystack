/*
 * Copyright 2018 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the License);
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an AS IS BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */


import {expect} from 'chai';

const Base64Encoder = require('../../../../../server/connectors/utils/encoders/Base64Encoder');
const MetricpointNameEncoder = require('../../../../../server/connectors/utils/encoders/MetricpointNameEncoder');
const NoopEncoder = require('../../../../../server/connectors/utils/encoders/NoopEncoder');
const PeriodReplacementEncoder = require('../../../../../server/connectors/utils/encoders/PeriodReplacementEncoder');

describe('MetricpointNameEncoder', () => {
    it('sets the correct encoder - base64', () => {
        const encoder = new MetricpointNameEncoder('base64');

        expect(encoder.encoder).equals(Base64Encoder);
    });

    it('sets the correct encoder - periodreplacement', () => {
        const encoder = new MetricpointNameEncoder('periodreplacement');

        expect(encoder.encoder).equals(PeriodReplacementEncoder);
    });

    it('defaults in all other cases', () => {
        const encoder = new MetricpointNameEncoder('noop');

        expect(encoder.encoder).equals(NoopEncoder);
    });
});
