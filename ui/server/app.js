/* eslint-disable global-require */
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

const path = require('path');
const express = require('express');
const favicon = require('serve-favicon');
const compression = require('compression');
const axios = require('axios');
const Q = require('q');

const config = require('./config/config');
const logger = require('./utils/logger');
const metricsMiddleware = require('./utils/metricsMiddleware');
const authChecker = require('./sso/authChecker');

const errorLogger = logger.withIdentifier('invocation:failure');

const app = express();

const bodyParser = require('body-parser');

// CONFIGURATIONS
axios.defaults.timeout = config.upstreamTimeout;
axios.defaults.headers.post['Content-Type'] = 'application/json';
Q.longStackSupport = true;
app.set('port', config.port);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');
app.set('etag', false);
app.set('x-powered-by', false);
app.set('trust proxy', 1);

// MIDDLEWARE SETUP
app.use(compression());
app.use(favicon(`${__dirname}/../public/favicon.ico`));
if (process.env.NODE_ENV === 'development') {
    // dont browser cache if in dev mode
    app.use('/bundles', express.static(path.join(__dirname, '../public/bundles'), {maxAge: 0}));
} else {
    // browser cache aggresively for no-dev environment
    app.use('/bundles', express.static(path.join(__dirname, '../public/bundles'), {maxAge: '60d'}));
}
app.use('/bundles', express.static(path.join(__dirname, '../public/bundles'), {maxAge: 0}));
app.use(express.static(path.join(__dirname, '../public'), {maxAge: '7d'}));
app.use(logger.REQUEST_LOGGER);
app.use(logger.ERROR_LOGGER);
app.use(metricsMiddleware.httpMetrics);
app.use(bodyParser.json());

// MIDDLEWARE AND ROUTES FOR SSO
if (config.enableSSO) {
    const passport = require('passport');
    const cookieSession = require('cookie-session');

    app.use(bodyParser.urlencoded({extended: false}));
    app.use(
        cookieSession({
            secret: config.sessionSecret,
            maxAge: config.sessionTimeout
        })
    );

    app.use(passport.initialize());
    app.use(passport.session());

    app.use('/auth', require('./routes/auth'));
    app.use('/sso', require('./routes/sso'));
    app.use('/user', require('./routes/user'));
    app.use('/api', authChecker.forApi);
}

// API ROUTING

const apis = [];
if (config.connectors.traces && config.connectors.traces.connectorName !== 'disabled') apis.push(require('./routes/servicesApi'));
if (config.connectors.traces && config.connectors.traces.connectorName !== 'disabled') apis.push(require('./routes/tracesApi'));
if (config.connectors.trends && config.connectors.trends.connectorName !== 'disabled') apis.push(require('./routes/trendsApi'));
if (config.connectors.trends && config.connectors.trends.connectorName !== 'disabled') apis.push(require('./routes/servicesPerfApi'));
if (config.connectors.alerts && config.connectors.alerts.connectorName !== 'disabled') apis.push(require('./routes/alertsApi'));
if (config.connectors.serviceGraph && config.connectors.serviceGraph.connectorName !== 'disabled') apis.push(require('./routes/serviceGraphApi'));
// prettier-ignore
if (config.connectors.serviceInsights && config.connectors.serviceInsights.enableServiceInsights !== 'disabled') apis.push(require('./routes/serviceInsightsApi'));

app.use('/api', ...apis);

// PAGE ROUTING
const indexRoute = require('./routes/index');

if (config.enableSSO) {
    app.use('/login', require('./routes/login'));
    app.use('/', authChecker.forPage);
}
app.use('/', indexRoute);

// ERROR-HANDLING
app.use((err, req, res, next) => {
    // eslint-disable-line no-unused-vars
    errorLogger.error(err);
    next(err);
});

module.exports = app;
