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

const OS = require('os');
const cluster = require('cluster');
const logger = require('./logger').withIdentifier('server');
const https = require('https');
const fs = require('fs');
const config = require('../config/config');

class Server {
    constructor(expressApp) {
        this.expressApp = expressApp;
    }

    startInStandaloneMode() {
        const activeApp = this.expressApp;
        const port = this.expressApp.get('port');

        if (config.https && config.https.keyFile && config.https.certFile) {
            https.createServer({
                key: fs.readFileSync(config.https.keyFile),
                cert: fs.readFileSync(config.https.certFile)
            }, activeApp)
            .listen(port, () => logger.info(`Express server listening on: ${port}`));
        } else {
            activeApp
            .listen(port, () => logger.info(`Express server listening : ${port}`));
        }
    }

    startInClusterMode() {
        const cpuCount = OS.cpus().length;
        if (cpuCount < 2) {
            this.startInStandaloneMode();
        } else if (cluster.isMaster) {
            logger.info(`Launching in cluster mode across ${cpuCount} CPUs`);
            for (let i = 0; i < cpuCount; i += 1) {
                cluster.fork();
            }
            cluster.on('exit', (worker) => {
                logger.info(`Worker ${worker.id} exited. Launching again...`);
                cluster.fork();
            });
            cluster.on('listening', (worker, address) => {
                logger.info(`Worker ${worker.id} is now connected to ${address.address || 'localhost'}:${address.port}`);
            });
        } else {
            this.startInStandaloneMode();
        }
    }
}

module.exports = Server;
