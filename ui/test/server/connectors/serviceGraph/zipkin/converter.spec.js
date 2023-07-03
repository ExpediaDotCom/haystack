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

const converter = require('../../../../../server/connectors/serviceGraph/zipkin/converter');
const config = require('../../../../../server/config/config');

const WINDOW_SIZE_IN_SECS =  config.connectors.serviceGraph && config.connectors.serviceGraph.windowSizeInSecs;


describe('converter.toHaystackServiceEdges', () => {
  it('converts dependency links', () => {
    const dependencyLinks = [
      {
        parent: 'mobile-gateway',
        child: 'transfer-service',
        callCount: WINDOW_SIZE_IN_SECS * 2
      },
      {
        parent: 'mobile-gateway',
        child: 'auth-service',
        callCount: WINDOW_SIZE_IN_SECS * 2
      },
      {
        parent: 'transfer-service',
        child: 'rabbitmq',
        callCount: WINDOW_SIZE_IN_SECS
      },
      {
        parent: 'rabbitmq',
        child: 'notification-service',
        callCount: WINDOW_SIZE_IN_SECS
      },
      {
        parent: 'mobile-gateway',
        child: 'content-service',
        callCount: WINDOW_SIZE_IN_SECS * 2,
        errorCount: WINDOW_SIZE_IN_SECS // number in callCount subject to error
      }
    ];

    const serviceEdges = [
      {
        destination: {name: 'transfer-service'},
        operation: 'unknown',
        source: {name: 'mobile-gateway'},
        stats: {count: 2, errorCount: 0}
      },
      {
        destination: {name: 'auth-service'},
        operation: 'unknown',
        source: {name: 'mobile-gateway'},
        stats: {count: 2, errorCount: 0}
      },
      {
        destination: {name: 'rabbitmq'},
        operation: 'unknown',
        source: {name: 'transfer-service'},
        stats: {count: 1, errorCount: 0}
      },
      {
        destination: {name: 'notification-service'},
        operation: 'unknown',
        source: {name: 'rabbitmq'},
        stats: {count: 1, errorCount: 0}
      },
      {
        destination: {name: 'content-service'},
        operation: 'unknown',
        source: {name: 'mobile-gateway'},
        stats: {count: 2, errorCount: 1}
      }
    ];

    expect(converter.toHaystackServiceEdges(dependencyLinks)).to.deep.equal(serviceEdges);
  });
});
