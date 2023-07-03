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

const Q = require('q');
const errorConverter = require('../utils/errorConverter');
const logger = require('../../utils/logger').withIdentifier('poster.grpc');
const metrics = require('../../utils/metrics');

const config = require('../../config/config');

function generateCallDeadline() {
    return new Date().setMilliseconds(new Date().getMilliseconds() + config.upstreamTimeout);
}

const poster = (posterName, client) => ({
    post: (request) => {
        const deferred = Q.defer();
        const timer = metrics.timer(`poster_grpc_${posterName}`).start();

        client[posterName](request, {deadline: generateCallDeadline()}, (error, result) => {
            timer.end();
            if (error || !result) {
                logger.info(`post failed: ${posterName}`);
                metrics.meter(`poster_grpc_failure_${posterName}`).mark();

                deferred.reject(errorConverter.fromGrpcError(error));
            } else {
                logger.info(`post successful: ${posterName}`);

                deferred.resolve(result);
            }
        });

        return deferred.promise;
    }
});

module.exports = poster;
