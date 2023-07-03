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

import PropTypes from 'prop-types';
import React, {Component} from 'react';
import 'particles.js';

import './login.less';

export default class Login extends Component {
    static propTypes = {
        location: PropTypes.object.isRequired
    };

    componentDidMount() {
        // eslint-disable-next-line no-undef
        particlesJS.load('particles-js', 'scripts/particles.json');
    }

    render() {
        const redirectUrl = this.props.location.search || '?redirectUrl=/';
        const ssoAdfsDomain = window.haystackUiConfig.ssoAdfsDomain;

        return (
            <div id="particles-js" className="login-cover text-center">
                <div className="login-box text-center">
                    <div className="login-box_jumbotron">
                        <div>
                            <img src="/images/logo.png" alt="Logo" className="login-box_logo" />
                            <span className="h1 login-box_title">Haystack</span>
                        </div>
                        <a href={`/auth/login${redirectUrl}`} className="login-box_btn btn btn-primary btn-lg">
                            Sign in
                            {ssoAdfsDomain && ` with ${ssoAdfsDomain} credentials`}
                        </a>
                    </div>
                </div>
            </div>
        );
    }
}
