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

import { convertSearchToUrlQuery } from '../components/universalSearch/utils/urlUtils';

const linkBuilder = {};

linkBuilder.withAbsoluteUrl = relativeUrl => `${window.location.protocol}//${window.location.host}${relativeUrl}`;

linkBuilder.universalSearchLink = search => `/search?${convertSearchToUrlQuery(search)}`;

linkBuilder.universalSearchTracesLink = search => `/search?${convertSearchToUrlQuery(search)}&tabId=traces`;

linkBuilder.universalSearchTrendsLink = search => `/search?${convertSearchToUrlQuery(search)}&tabId=trends`;

linkBuilder.universalSearchAlertsLink = search => `/search?${convertSearchToUrlQuery(search)}&tabId=alerts`;

export default linkBuilder;
