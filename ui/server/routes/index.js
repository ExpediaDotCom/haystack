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

const express = require('express');
const onFinished = require('finished');
const config = require('../config/config');
const metrics = require('../utils/metrics');
const assets = require('../../public/assets.json');

const router = express.Router();

const subsystems = Object.keys(config.connectors).filter((connector) => {
    if (connector === 'serviceInsights') {
        return config.connectors[connector].enableServiceInsights !== 'disabled';
    }
    return config.connectors[connector].connectorName !== 'disabled';
});

router.get('*', (req, res) => {
    const timer = metrics.timer('index').start();

    res.render('index', {
        bundleAppJsPath: assets.app.js,
        bundleAppCssPath: assets.app.css,
        bundleCommonsJsPath: assets.commons.js,
        subsystems,
        gaTrackingID: config.gaTrackingID,
        usbPrimary: config.usbPrimary,
        enableServicePerformance: config.connectors.trends && config.connectors.trends.enableServicePerformance,
        enableServiceLevelTrends: config.connectors.trends && config.connectors.trends.enableServiceLevelTrends,
        enableServiceInsights: config.connectors.serviceInsights && config.connectors.serviceInsights.enableServiceInsights,
        enableSSO: config.enableSSO,
        refreshInterval: config.refreshInterval,
        enableAlertSubscriptions: config.connectors.alerts && config.connectors.alerts.subscriptions,
        tracesTimePresetOptions: config.connectors.traces && config.connectors.traces.timePresetOptions,
        timeWindowPresetOptions: config.timeWindowPresetOptions,
        tracesTTL: config.connectors.traces && config.connectors.traces.ttl,
        trendsTTL: config.connectors.trends && config.connectors.trends.ttl,
        relatedTracesOptions: config.relatedTracesOptions,
        externalLinking: config.externalLinking,
        usingZipkinConnector: config.connectors.traces && config.connectors.traces.connectorName === 'zipkin',
        enableBlobs: config.connectors.blobs && config.connectors.blobs.enableBlobs,
        blobsUrl: config.connectors.blobs && config.connectors.blobs.blobsUrl
    });

    onFinished(res, () => {
        timer.end();
    });
});

module.exports = router;
