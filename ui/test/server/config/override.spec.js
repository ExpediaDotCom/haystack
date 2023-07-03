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

const {expect} = require('chai');
const override = require('../../../server/config/override');

describe('override configuration reader', () => {
  it('iterates over environment variables and returns an empty object if no matching variable is found', () => {
    const envVariables = {
      rvm_bin_path: '/Users/mchandramouli/.rvm/bin',
      GEM_HOME: '/Users/mchandramouli/.rvm/gems/ruby-2.6.0',
      NVM_CD_FLAGS: '',
      TERM: 'xterm-256color'
    };

    const actual = override.readOverrides(envVariables);
    expect(actual).to.deep.equal({});
  });

  it('iterates over environment variables and returns an object split by _', () => {
    const envVariables = {
      HAYSTACK_PROP_CONNECTORS_TRACES_ENABLED: 'true',
      HAYSTACK_PROP_CONNECTORS_TRACES_PROVIDER: 'haystack',
      TERM: 'xterm-256color'
    };

    const actual = override.readOverrides(envVariables);
    expect(actual).to.deep.equal({
                                   connectors: {
                                     traces: {
                                       enabled: 'true',
                                       provider: 'haystack'
                                     }
                                   }
                                 });
  });

  it('iterates over environment variables and returns an object split by _ and camelCase words split by __', () => {
    const envVariables = {
      HAYSTACK_PROP_CONNECTORS_TRENDS_CONNECTOR__NAME: 'haystack',
      HAYSTACK_PROP_CONNECTORS_TRENDS_METRIC__TANK__URL: 'http://localhost:6000',
      TERM: 'xterm-256color'
    };

    const actual = override.readOverrides(envVariables);
    expect(actual).to.deep.equal({
                                   connectors: {
                                     trends: {
                                       connectorName: 'haystack',
                                       metricTankUrl: 'http://localhost:6000'
                                     }
                                   }
                                 });
  });
});
