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

describe('routes.tracesApi', () => {
    it('returns http 200 for /api/traces', (done) => {
        request(server)
            .get('/api/traces')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /api/trace/:traceid', (done) => {
        request(server)
            .get('/api/trace/traceid')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns searchable keys for /api/trace/searchableKeys', (done) => {
        request(server)
            .get('/api/trace/searchableKeys')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /api/traces/raw', (done) => {
        request(server)
            .get('/api/traces/raw')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /api/traces/raw/:traceId', (done) => {
        request(server)
            .get('/api/traces/raw/100')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /trace/raw/:traceId', (done) => {
        request(server)
            .get('/api/trace/raw/100')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /trace/raw/:traceId/:spanId', (done) => {
        request(server)
            .get('/api/trace/raw/100/100')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /trace/:traceId/latencyCost', (done) => {
        request(server)
            .get('/api/trace/100/latencyCost')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /api/traces/timeline', (done) => {
        request(server)
            .get('/api/traces/timeline')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });

    it('returns http 200 for /api/traces/searchableKeys', (done) => {
        request(server)
            .get('/api/traces/searchableKeys')
            .expect(200)
            .end((err) => {
                if (err) {
                    return done(err);
                }
                return done();
            });
    });
});
