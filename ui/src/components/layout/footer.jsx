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

import './footer.less';

export default () => (
    <footer className="primary-footer">
        <div className="container">
            <ul className="footer-links">
                <li className="footer-links__item">
                    <Link to="/" className="footer-links__link">
                        Haystack
                    </Link>
                </li>
                <li className="footer-links__item">
                    <a className="footer-links__link" href="https://github.com/ExpediaDotCom/haystack/">
                        <span className="ti-github" /> Github
                    </a>
                </li>
                <li className="footer-links__item">
                    <a className="footer-links__link" href="http://expediadotcom.github.io/haystack/">
                        <span className="ti-book" /> Documentation
                    </a>
                </li>
                {window.haystackUiConfig.enableSSO ? (
                    <li className="footer-links__item">
                        <a className="footer-links__link" href="/auth/logout">
                            Logout
                        </a>
                    </li>
                ) : null}
            </ul>
            <p>
                Code licensed under <a href="https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE">Apache 2.0 License</a>
            </p>
        </div>
    </footer>
);
