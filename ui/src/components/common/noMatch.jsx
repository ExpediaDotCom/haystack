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

import React from 'react';
import {Link} from 'react-router-dom';

export default () => (
    <div className="primary-content__spacer container nomatch-panel">
        <div className="jumbotron">
            <h1>Sorry!</h1>
            <p>Page not found</p>
            <p>
                <Link to="/">Return to home</Link>
            </p>
        </div>
    </div>
);