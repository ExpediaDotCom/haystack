# Haystack Span Timeseries Transformer

Haystack-span-timeseries-transformer is the module  which reads the spans from kafka and converts them to timeseries metricPoints based on transformers and writes out the time-series metricPoints back to kafka.

Haystack's has another app [timeseries-aggregator](https://github.com/ExpediaDotCom/haystack-trends/tree/master/timeseries-aggregator) which consumes these metric points 
and aggregates them based on predefined rules which can be visualized on the [haystack ui](https://github.com/ExpediaDotCom/haystack-ui)

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