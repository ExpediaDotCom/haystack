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

const winston = require('winston');
const expressWinston = require('express-winston');
const _ = require('lodash');
const moment = require('moment');

const BASE_TRANSPORT_OPTIONS = {
    json: false
};
const CONSOLE_TRANSPORT_OPTIONS = _.merge({}, BASE_TRANSPORT_OPTIONS);

function getLogFormatterOptionsWithIdentifier(identifier) {
    return {
        timestamp: () => Date.now(),
        formatter: (options) => {
            const level = options.level || 'unknown';
            const meta = _.isEmpty(options.meta) ? '' : JSON.stringify(options.meta);
            return `${moment.utc(options.timestamp()).format()}: identifier="${identifier}" level="${level.toUpperCase()}" message="${options.message}" ${meta}`;
        }
    };
}

exports.withIdentifier = (identifier) => {
    if (!identifier) {
        throw new Error('Identifier is required while setting up a logger. For example, pass the module name that will use this logger.');
    }
    return new winston.Logger({
        transports: [new winston.transports.Console(_.merge({}, CONSOLE_TRANSPORT_OPTIONS, getLogFormatterOptionsWithIdentifier(identifier)))]
    });
};

exports.REQUEST_LOGGER = expressWinston.logger({
    transports: [new winston.transports.Console(CONSOLE_TRANSPORT_OPTIONS)]
});

exports.ERROR_LOGGER = expressWinston.errorLogger({
    transports: [new winston.transports.Console(CONSOLE_TRANSPORT_OPTIONS)]
});
