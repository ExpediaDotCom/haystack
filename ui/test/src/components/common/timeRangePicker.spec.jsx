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
import TimeRangePicker from '../../../../src/components/common/timeRangePicker';

describe('TimeRangePicker.isFromValid', () => {
    window.haystackUiConfig.trendsTTL = 3 * 24 * 60 * 60 * 1000;

    it('should be true when current is before today and after the ttl', () => {
        expect(TimeRangePicker.isFromValid(DateTime.moment().subtract(1, 'day'))).to.equal(true);
    });

    it('should be false when current is after today and after the ttl', () => {
        expect(TimeRangePicker.isFromValid(DateTime.moment().add(1, 'day'))).to.equal(false);
    });

    it('should be false when current is before today and before the ttl', () => {
        expect(TimeRangePicker.isFromValid(DateTime.moment().subtract(11, 'day'))).to.equal(false);
    });
});
