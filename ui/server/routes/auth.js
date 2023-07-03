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

const express = require('express');
const authenticatorWithRedirect = require('../sso/samlSsoAuthenticator').authenticatorWithRedirect;
const authChecker = require('../sso/authChecker');

const router = express.Router();

const loggedOutHome = '/login';

const authenticate = req => authenticatorWithRedirect(req);

const extractFullRedirectUrl = req => req.originalUrl.split('/auth/login?redirectUrl=').pop();

router.get('/login', (req, res, next) => {
    const redirectUrl = extractFullRedirectUrl(req);
    return authenticate(redirectUrl)(redirectUrl, res, next);
});

// check for active login session and then renew user
router.get('/renewlogin', authChecker.forApi, (req, res, next) => {
    req.login({...req.user, timestamp: Date.now()}, (err) => {
        if (err) {
            next(err);
        } else {
            res.send(200);
        }
    });
});

router.get('/logout', (req, res) => {
    req.logout();
    req.session = null;
    res.redirect(loggedOutHome);
});

module.exports = router;
