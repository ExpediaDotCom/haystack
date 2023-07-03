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

import {expect} from 'chai';
import DateTime from 'react-datetime';
import timeWindow from '../../../src/utils/timeWindow';

describe('timeWindow.isAfterTTL', () => {
    const threeDayTTL = 3 * 24 * 60 * 60 * 1000;

    it('should be true when no ttl is defined', () => {
        expect(timeWindow.isAfterTTL(DateTime.moment(), 'unknownTTL')).to.equal(true);
    });

    it('should be true when the ttl is <= 0', () => {
        window.haystackUiConfig.someTTL = 0;
        expect(timeWindow.isAfterTTL(DateTime.moment(), 'someTTL')).to.equal(true);
    });

    it('should be true when the ttl exists and the date is after it', () => {
        window.haystackUiConfig.someTTL = threeDayTTL;
        expect(timeWindow.isAfterTTL(DateTime.moment().subtract(2, 'day'), 'someTTL')).to.equal(true);
    });

    it('should be false when the ttl exists and the date is before it', () => {
        window.haystackUiConfig.someTTL = threeDayTTL;
        expect(timeWindow.isAfterTTL(DateTime.moment().subtract(4, 'day'), 'someTTL')).to.equal(false);
    });
});
