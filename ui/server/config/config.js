/*

 *  Copyright 2018 Expedia Group
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *

 */

const _ = require('lodash');
const baseConfiguration = require('../config/base');
const override = require('./override');

let finalConfiguration =  _.merge({}, baseConfiguration);

// if an override configuration file is provided, extend the base config with
// the provided one. This is not a recursive merge, just a top level extend with
// the overriden config
if (process.env.HAYSTACK_OVERRIDES_CONFIG_PATH) {
  let overridesConfigration = process.env.HAYSTACK_OVERRIDES_CONFIG_PATH;
  if (!overridesConfigration.startsWith('/')) {
    overridesConfigration = `${process.cwd()}/${overridesConfigration}`;
  }
  // eslint-disable-next-line global-require, import/no-dynamic-require
  const environmentSpecificConfiguration = require(overridesConfigration);
  finalConfiguration = _.extend({}, finalConfiguration, environmentSpecificConfiguration);
}

// if there are environment variables, read them as objects and merge them
// into the current configuration
const overrideObject = override.readOverrides(process.env);
module.exports = _.merge({}, finalConfiguration, overrideObject);
