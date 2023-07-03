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

const PeriodReplacementEncoder = require('../encoders/PeriodReplacementEncoder');
const Base64Encoder = require('../encoders/Base64Encoder');
const NoopEncoder = require('../encoders/NoopEncoder');

class MetricpointNameEncoder {
    constructor(encoderType) {
        if (encoderType === 'periodreplacement') {
            this.encoder = PeriodReplacementEncoder;
        } else if (encoderType === 'base64') {
            this.encoder = Base64Encoder;
        } else {
            this.encoder = NoopEncoder;
        }
    }

    encodeMetricpointName(operationName) {
        return this.encoder.encode(decodeURIComponent(operationName));
    }

    decodeMetricpointName(operationName) {
        return this.encoder.decode(operationName);
    }
}

module.exports = MetricpointNameEncoder;
