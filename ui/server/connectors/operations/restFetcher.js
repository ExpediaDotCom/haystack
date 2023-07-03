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

const axios = require('axios');
const Q = require('q');
const errorConverter = require('../utils/errorConverter');
const logger = require('../../utils/logger').withIdentifier('fetcher.rest');
const metrics = require('../../utils/metrics');

const fetcher = (fetcherName) => ({
    fetch: (url, headers = {}) => {
        const deferred = Q.defer();
        const timer = metrics.timer(`fetcher_rest_${fetcherName}`).start();

        axios.get(url, {headers}).then(
            (response) => {
                timer.end();
                logger.info(`fetch successful: ${url}`);

                deferred.resolve(response.data);
            },
            (error) => {
                timer.end();
                metrics.meter(`fetcher_rest_failure_${fetcherName}`).mark();
                logger.error(`fetch failed: ${url}`);

                deferred.reject(errorConverter.fromAxiosError(error));
            }
        );

        return deferred.promise;
    }
});

module.exports = fetcher;
