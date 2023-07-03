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
const config = require('../../config/config');
const LoaderBackedCache = require('../utils/LoaderBackedCache');

const tracesConnector = config.connectors.traces && require(`../traces/${config.connectors.traces.connectorName}/tracesConnector`); // eslint-disable-line import/no-dynamic-require, global-require
const refreshIntervalInSecs = config.connectors.traces.serviceRefreshIntervalInSecs;
const connector = {};

const serviceCache = new LoaderBackedCache(() => tracesConnector.getServices(), refreshIntervalInSecs * 1000);
connector.getServices = () => serviceCache.get();

const operationsCache = new LoaderBackedCache((serviceName) => tracesConnector.getOperations(serviceName), refreshIntervalInSecs * 1000);
connector.getOperations = (serviceName) => operationsCache.get(serviceName);

module.exports = connector;
