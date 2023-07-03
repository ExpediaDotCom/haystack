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
const _ = require('lodash');
const Q = require('q');

class LoaderBackedCache {
    constructor(loader, timeout) {
        this.cache = {};
        this.timeout = timeout;
        this.loader = loader;
    }

    populateIfPresent(result, key) {
        if (!_.isEmpty(result)) {
            this.cache[key] = {
                item: result,
                expiryTimestamp: Date.now() + this.timeout
            };
        }
    }

    get(key = 'key') {
        const cachedItem = this.cache[key];

        if (cachedItem) {
            const isExpired = cachedItem.expiryTimestamp < Date.now();

            if (isExpired) {
                this.loader(key).then((result) => this.populateIfPresent(result, key));
            }

            return Q.fcall(() => cachedItem.item);
        }

        return this.loader(key).then((result) => {
            this.populateIfPresent(result, key);
            return result;
        });
    }

    getCached(key = 'key') {
        const cachedItem = this.cache[key];

        if (cachedItem) {
            const isExpired = cachedItem.expiryTimestamp < Date.now();

            if (isExpired) {
                this.loader(key).then((result) => this.populateIfPresent(result, key));
            }

            return cachedItem.item;
        }

        this.loader(key).then((result) => this.populateIfPresent(result, key));
        return null;
    }

    get size() {
        return Object.keys(this.cache).length;
    }
}

module.exports = LoaderBackedCache;
