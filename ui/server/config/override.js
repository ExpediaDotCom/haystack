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

const _ = require('lodash');

const camelize = str => str.replace(/\W+(.)/g, (match, chr) => chr.toUpperCase());

// this code will tokenize incoming keys to produce json objects
// For example, a key like 'HAYSTACK_CONNECTORS_TRACES_ZIPKIN__URL'; with value 'foo'
// will turn into '{ connectors: { traces: { zipkinUrl: 'foo' } } }'
// Note:  a single _ splits the token into words and a __ combines them
// to a camelCase
module.exports = {

  readOverrides: (collection) => {
    const overrideData = {};
    _.each(collection, (value, key) => {
      if (key.startsWith('HAYSTACK_PROP_')) {
        const parts = key.toLowerCase().replace(/__/g, ' ').split('_');
        parts.splice(0, 2);

        let configObject = overrideData;
        let part = parts.shift();
        // console.log(`${key} [${parts}] ${value}`);

        while (part) {
          part = camelize(part);
          if (parts.length) {
            if (configObject[part] == null) {
              configObject[part] = {};
            }
            configObject = configObject[part];
          } else {
            configObject[part] = value;
          }
          part = parts.shift();
        }
      }
    });
    return overrideData;
  }
};
