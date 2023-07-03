/*
 * Copyright 2019 Expedia Group
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

import React from 'react';
import {expect} from 'chai';
import {shallow} from 'enzyme';

import Tooltip from '../../../../../src/components/serviceInsights/serviceInsightsGraph/tooltip';

describe('<Tooltip/>', () => {
    describe('"line" Tooltips', () => {
        it('should render the expected elements', () => {
            const wrapper = shallow(<Tooltip x={1} y={2} visible type="line" data={{tps: 5}} />);

            expect(wrapper.find('.graph-tooltip')).to.have.lengthOf(1);
            expect(wrapper.find('.graph-tooltip_decoration.line')).to.have.lengthOf(1);
            expect(wrapper.find('.graph-tooltip_decoration.line > div').text()).to.contain(5);
            expect(wrapper.find('.graph-tooltip_decoration.line').prop('style')).to.deep.equal({left: 1, top: 2});
        });

        it('should render the with the x, y values', () => {
            const wrapper = shallow(<Tooltip x={1} y={2} visible type="line" data={{tps: 5}} />);
            expect(wrapper.find('.graph-tooltip_decoration.line').prop('style')).to.deep.equal({left: 1, top: 2});
        });
    });

    describe('"node" Tooltips', () => {
        it('should render the expected elements', () => {
            const data = {
                name: 'foo',
                type: 'service',
                count: 32,
                avgDuration: '55ms',
                operations: {POST_api: 5}
            };
            const wrapper = shallow(<Tooltip x={1} y={1} visible type="node" data={data} />);

            expect(wrapper.find('.graph-tooltip')).to.have.lengthOf(1);
            expect(wrapper.find('.graph-tooltip_decoration')).to.have.lengthOf(1);
            expect(wrapper.find('.graph-tooltip_title').text()).to.equal(data.name);
            expect(wrapper.find('.tip-grid').text()).to.contain(data.type);
            expect(wrapper.find('.tip-grid').text()).to.contain(data.count);
            expect(wrapper.find('.tip-grid').text()).to.contain(data.avgDuration);
            expect(wrapper.find('.tip-grid').text()).to.contain('POST_api');
            expect(wrapper.find('.tip-grid').text()).to.contain('5');
        });

        it('should render the with the x, y values', () => {
            const wrapper = shallow(<Tooltip x={1} y={2} visible type="node" data={{operations: {}}} />);
            expect(wrapper.find('.graph-tooltip_decoration').prop('style')).to.deep.equal({left: 1, top: 2});
        });

        it('should render issues if any exist', () => {
            const data = {
                type: 'uninstrumented',
                operations: {}
            };
            const wrapper = shallow(<Tooltip x={1} y={1} visible type="node" data={data} />);

            expect(wrapper.find('.tip-grid').text()).to.contain('Issues:');
            expect(wrapper.find('.tip-grid').text()).to.contain('⚠️Service not instrumented with open tracing');
        });

        it('should render debug info when debug url param is present', () => {
            const oldLocation = window.location;
            delete window.location;
            window.location = {
                search: '?debug=true'
            };

            const data = {
                id: '123',
                operations: {}
            };

            const wrapper = shallow(<Tooltip x={1} y={1} visible type="node" data={data} />);
            expect(wrapper.find('.graph-tooltip_title.debug')).to.have.lengthOf(1);

            expect(wrapper.find('.tip-grid.debug').text()).to.contain(data.id);
            window.location = oldLocation;
        });

        it('should render `null` if no visible prop present', () => {
            const data = {
                type: 'uninstrumented',
                operations: {}
            };
            const wrapper = shallow(<Tooltip x={1} y={1} type="node" data={data} />);

            expect(wrapper.getElement()).to.equal(null);
        });

        it('should render invalid cycle detected', () => {
            const data = {
                type: 'service',
                operations: {},
                invalidCycleDetected: true
            };
            const wrapper = shallow(<Tooltip x={1} y={1} visible type="line" data={data} />);
            expect(/Invalid cycle detected/.test(wrapper.find('[data-issue]').text())).to.equal(true);
        });
    });
});
