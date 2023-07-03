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

const server = require('../../../server/app.js');
const request = require('supertest');

describe('routes.alertsApi', () => {
    it('returns http 200 for /api/alerts', (done) => {
        request(server)
            .get('/api/alerts/service')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /api/alerts', (done) => {
        request(server)
            .get('/api/alerts/service/unhealthyCount')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /api/alert/:serviceName', (done) => {
        request(server)
            .get('/api/alert/service/operation/count/history')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for GET /api/alert/:serviceName/:operationName/:alertType/subscriptions', (done) => {
        request(server)
            .get('/api/alert/service/operation/count/subscriptions')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for POST /api/alert/:serviceName/:operationName/:alertType/subscriptions', (done) => {
        request(server)
            .post('/api/alert/service/operation/count/subscriptions')
            .type('form')
            .send({user: 'haystack', subscription: {}})
            .set('Content-Type', 'application/json')
            .set('Accept', 'application/json')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for PUT /api/alert/subscriptions/:subscriptionId', (done) => {
        request(server)
            .put('/api/alert/subscriptions/1')
            .type('form')
            .send({subscriptions: {old: {}, modified: {}}})
            .set('Content-Type', 'application/json')
            .set('Accept', 'application/json')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for DELETE /api/alert/subscriptions:subscriptionId', (done) => {
        request(server)
            .delete('/api/alert/subscriptions/1')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });
});
