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

const axios = require('axios');
const sinon = require('sinon');
const proxyquire = require('proxyquire');

// loads the tracesConnector, but allowing us to override the configuration
function tracesConnector(config) {
  return proxyquire('../../../../../server/connectors/traces/zipkin/tracesConnector', {
      '../../../config/config': config
  });
}

// When an HTTP GET is invoked, this returns the supplied data as a response.
function stubGetReturns(sandbox, data) {
  const resolved = new Promise(r => r({ data }));
  return sandbox.stub(axios, 'get').returns(resolved);
}

const zipkinUrl = 'http://localhost/api/v2';
const config = {connectors: {traces: {zipkinUrl}, trends: {connectorName: 'stub'}}};

describe('tracesConnector.getServices', () => {
  let sandbox;
  beforeEach(() => {
    sandbox = sinon.sandbox.create();
  });
  afterEach(() => sandbox.restore());

  it('should not error when empty', (done) => {
    stubGetReturns(sandbox, []);

    tracesConnector(config).getServices().then((result) => {
      expect(result).to.have.length(0);
    }).then(done, done);
  });

  it('should call correct URL', (done) => {
    const spy = stubGetReturns(sandbox, []);

    tracesConnector(config).getServices().then(() => {
      expect(spy.args[0][0]).to.equal('http://localhost/api/v2/services');
    }).then(done, done);
  });

  it('should not filter out services when servicesFilter is unconfigured', (done) => {
    stubGetReturns(sandbox, ['frontend', 'backend']);

    tracesConnector(config).getServices().then((result) => {
      expect(result).to.deep.equal(['frontend', 'backend']);
    }).then(done, done);
  });

  it('should not filter out services when empty servicesFilter is configured', (done) => {
    stubGetReturns(sandbox, ['frontend', 'backend']);

    config.connectors.traces.servicesFilter = [];
    tracesConnector(config).getServices().then((result) => {
      expect(result).to.deep.equal(['frontend', 'backend']);
    }).then(done, done);
  });
  
  it('should filter out services that match regex servicesFilter', (done) => {
    stubGetReturns(sandbox, [
        'value-1',
        '$/special/values',
        '#/special/.values',
        'values'
    ]);

    config.connectors.traces.servicesFilter = [new RegExp('value')];
    tracesConnector(config).getServices().then((result) => {
      expect(result.length).to.equal(0); // all service names contain 'value'
    }).then(done, done);
  });

  it('should filter out services that match any regex servicesFilter', (done) => {
    stubGetReturns(sandbox, [
        'value-1',
        '$/special/values',
        '#/special/.values',
        'values'
    ]);

    config.connectors.traces.servicesFilter = [new RegExp('special'), new RegExp('-1')];
    tracesConnector(config).getServices().then((result) => {
      expect(result).to.deep.equal(['values']);
    }).then(done, done);
  });

  it('should filter out services that match regex servicesFilter - special character', (done) => {
    stubGetReturns(sandbox, [
        'value-1',
        '$/special/values',
        '#/special/.values',
        'values'
    ]);

    config.connectors.traces.servicesFilter = [new RegExp('^\\$/special/.*$')];
    tracesConnector(config).getServices().then((result) => {
      expect(result).to.deep.equal([
          'value-1',
          '#/special/.values',
          'values'
      ]);
    }).then(done, done);
  });
});

describe('tracesConnector.getOperations', () => {
  let sandbox;
  beforeEach(() => {
    sandbox = sinon.sandbox.create();
  });
  afterEach(() => sandbox.restore());

  it('should not error when empty', (done) => {
    stubGetReturns(sandbox, []);

    tracesConnector(config).getOperations('frontend').then((result) => {
      expect(result).to.have.length(0);
    }).then(done, done);
  });

  it('should call correct URL', (done) => {
    const spy = stubGetReturns(sandbox, []);

    tracesConnector(config).getOperations('frontend').then(() => {
      expect(spy.args[0][0]).to.equal('http://localhost/api/v2/spans?serviceName=frontend');
    }).then(done, done);
  });

  it('should return list of operations from spans response', (done) => {
    const spanNames = ['get /users/{userId}', 'post /'];
    stubGetReturns(sandbox, spanNames);

    tracesConnector(config).getOperations('frontend').then((result) => {
      expect(result).to.deep.equal(spanNames);
    }).then(done, done);
  });
});

describe('tracesConnector.getTrace', () => {
  let sandbox;
  beforeEach(() => {
    sandbox = sinon.sandbox.create();
  });
  afterEach(() => sandbox.restore());

  it('should call correct URL', (done) => {
    const spy = stubGetReturns(sandbox, []);

    tracesConnector(config).getTrace('0000000000000001').then(() => {
      expect(spy.args[0][0]).to.equal('http://localhost/api/v2/trace/0000000000000001');
    }).then(done, done);
  });

  it('should convert Zipkin v2 span into a Haystack one', (done) => {
    stubGetReturns(sandbox, [{
      traceId: '0000000000000001',
      id: '0000000000000002',
      name: 'get',
      localEndpoint: {serviceName: 'frontend'},
      annotations: [],
      tags: {}
    }]);

    tracesConnector(config).getTrace('0000000000000001').then((result) => {
      expect(result[0]).to.deep.equal({
        traceId: '0000000000000001',
        spanId: '0000000000000002',
        operationName: 'get',
        serviceName: 'frontend',
        logs: [],
        tags: []
      });
    }).then(done, done);
  });
});
