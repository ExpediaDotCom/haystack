[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-collector.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-collector)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)

# haystack-collector
This haystack component collects spans from various sources and publish to kafka. As of today, we support two sources:

1. Kinesis: Kinesis span collector reads proto serialized spans from a kinesis stream, validates it and write the data to configured kafka topic.
2. Http: Http span collector listens on port 8080 for proto or json serialized spans, validate them and write to configured kafka topic. For more detail read [this](./http/README.md)

Spans are validated to ensure they dont't contain an empty service and operation name. The startTime and duration should be non-zero.

## Building

####
Since this repo contains haystack-idl as the submodule, so use the following to clone the repo
* git clone --recursive git@github.com:ExpediaDotCom/haystack-collector.git .

####Prerequisite: 

* Make sure you have Java 1.8
* Make sure you have maven 3.3.9 or higher
* Make sure you have docker 1.13 or higher


Note : For mac users you can download docker for mac to set you up for the last two steps.

####Build

For a full build, including unit tests and integration tests, docker image build, you can run -
```
make all
```

####Integration Test

####Prerequisite:
1. Install docker using Docker Tools or native docker if on mac
2. Verify if docker-compose is installed by running following command else install it.
```
docker-compose

```

Run the build and integration tests for individual components with
```
make kinesis

or

make http

```
