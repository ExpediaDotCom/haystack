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

const metrics = require('../../utils/metrics');

const responseHandler = {};

responseHandler.handleResponsePromise = (response, next, pathName) => (operation) => {
    const timer = metrics.timer(`http_rq_${pathName}`).start();

    operation()
        .then(
            (result) => response.json(result),
            (err) => {
                metrics.meter(`http_rq_${pathName}_failed`).mark();
                next(err);
            }
        )
        .fin(() => timer.end())
        .done();
};

module.exports = responseHandler;
