# Haystack Timeseries Aggregator

haystack-timeseries-aggregator  is the module  which reads metric points from kafka, aggregates them based on rules and pushes the aggregated metric points to kafka

These aggregated metric points and stored in a time-series database and can be visualized on the [haystack ui](https://github.com/ExpediaDotCom/haystack-ui)


Haystack's has another app [span-timeseries-transformer](https://github.com/ExpediaDotCom/haystack-trends/tree/master/span-timeseries-transformer) 
which is responsible for reading the spans and creating raw metric points for aggregation

This is a simple public static void main application which is written in scala and uses kafka-streams. This is designed to be deployed as a docker containers.


## Building

#### Prerequisite: 

* Make sure you have Java 1.8
* Make sure you have maven 3.3.9 or higher
* Make sure you have docker 1.13 or higher

#### Build

For a full build, including unit tests, jar + docker image build and integration test, you can run -
```
make all
```

#### Integration Test

If you are developing and just want to run integration tests 
```
make integration_test

```