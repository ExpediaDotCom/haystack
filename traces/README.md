[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-traces.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-traces)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack/blob/master/LICENSE)

# haystack-traces

This repo contains the haystack components that build the traces, store them in Cassandra and ElasticSearch(for indexing) and provide a grpc endpoint for accessing them


## Building

Since this repo contains haystack-idl as the submodule, so use the following to clone the repo

* git clone --recursive git@github.com:ExpediaDotCom/haystack-traces.git .

#### Prerequisite:

* Make sure you have Java 1.8
* Make sure you have maven 3.3.9 or higher
* Make sure you have docker 1.13 or higher


Note : For mac users you can download docker for mac to set you up for the last two steps.

#### Build

For a full build, including unit tests and integration tests, docker image build, you can run -

```
make all
```

#### Integration Test

#### Prerequisite:
1. Install docker using Docker Tools or native docker if on mac
2. Verify if docker-compose is installed by running following command else install it.

```
docker-compose
```

Run the build and integration tests for individual components with

```
make indexer
```

&&

```
make reader
```
