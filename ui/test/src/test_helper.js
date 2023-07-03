/*
 * Copyright 2018 Expedia Group
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */

const {JSDOM} = require('jsdom');
const Enzyme = require('enzyme');
const Adapter = require('enzyme-adapter-react-16');

Enzyme.configure({adapter: new Adapter()});

const jsdom = new JSDOM('<!doctype html><html><body><div id="root"></div></body></html>');
const {window} = jsdom;

function copyProps(src, target) {
    Object.defineProperties(target, {
        ...Object.getOwnPropertyDescriptors(src),
        ...Object.getOwnPropertyDescriptors(target)
    });
}

// stubbing animation
window.cancelAnimationFrame = () => {};

global.window = window;

// Polyfill `global.URLSearchParams` in cases where older nodeJS runtimes are being used
// TODO: remove when Docker image has been upgraded to nodeJS >= 10.x
if (!global.URLSearchParams) {
    global.URLSearchParams = require('whatwg-url').URLSearchParams; // eslint-disable-line
}

global.document = window.document;
global.navigator = {
    userAgent: 'node.js'
};
global.window.haystackUiConfig = {
    subsystems: ['traces', 'trends', 'alerts'],
    tracesTimePresetOptions: [
        {shortName: '5m', value: 5 * 60 * 1000},
        {shortName: '15m', value: 15 * 60 * 1000},
        {shortName: '1h', value: 60 * 60 * 1000},
        {shortName: '4h', value: 4 * 60 * 60 * 1000},
        {shortName: '12h', value: 12 * 60 * 60 * 1000},
        {shortName: '24h', value: 24 * 60 * 60 * 1000},
        {shortName: '3d', value: 3 * 24 * 60 * 60 * 1000}
    ],
    timeWindowPresetOptions: [
        {shortName: '5m', longName: '5 minutes', value: 5 * 60 * 1000},
        {shortName: '15m', longName: '15 minutes', value: 15 * 60 * 1000},
        {shortName: '1h', longName: '1 hour', value: 60 * 60 * 1000},
        {shortName: '6h', longName: '6 hours', value: 6 * 60 * 60 * 1000},
        {shortName: '12h', longName: '12 hours', value: 12 * 60 * 60 * 1000},
        {shortName: '24h', longName: '24 hours', value: 24 * 60 * 60 * 1000},
        {shortName: '3d', longName: '3 days', value: 3 * 24 * 60 * 60 * 1000},
        {shortName: '7d', longName: '7 days', value: 7 * 24 * 60 * 60 * 1000},
        {shortName: '30d', longName: '30 days', value: 30 * 24 * 60 * 60 * 1000}
    ],
    relatedTracesOptions: [
        {
            fieldTag: 'success',
            propertyToMatch: 'success',
            fieldDescription: 'success status'
        },
        {
            fieldTag: 'id',
            propertyToMatch: 'id',
            fieldDescription: 'customer identity'
        }
    ],
    externalLinking: [
        {
            key: 'tag',
            tagKey: 'url2',
            url: 'https://www.google.com/search?q=#{tagValue}',
            label: '[Google]'
        }
    ],
    tracesTTL: -1,
    trendsTTL: -1
};
global.window.haystackUiConfig.enableAlertSubscriptions = true;
copyProps(window, global);
