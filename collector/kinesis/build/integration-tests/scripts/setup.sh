#!/bin/bash

npm install
node create-kinesis-stream.js
node create-dynamo-table.js