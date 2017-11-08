<img src="../images/logo.png" style="width: 200px;"/>

# [Haystack Trends](https://github.com/ExpediaDotCom/haystack-trends)

haystack-trends contains the required modules for trending the spans pushed to haystack. We currently plan to compute four trends for each
combination `service_name` and `operation_name` contained in the spans (refer to the [span schema](https://github.com/ExpediaDotCom/haystack-idl/blob/master/proto/span.proto) for details of the fields in the span  )

1. total_count `[1min, 5min, 15min, 1hour]`
2. success_count `[1min, 5min, 15min, 1hour]`
3. failure_count `[1min, 5min, 15min, 1hour]`
4. duration `[mean, median, std-dev, 99 percentile, 95 percentile]`

> 2 and 3 would only work if the error tag is present in the span.

More trends can be computed by adding a [transformer](https://github.com/ExpediaDotCom/haystack-trends/tree/master/span-timeseries-transformer/src/main/scala/com/expedia/www/haystack/metricpoints/transformer)
to create the metric point and adding an [aggregation-rule](https://github.com/ExpediaDotCom/haystack-trends/tree/master/timeseries-aggregator/src/main/scala/com/expedia/www/haystack/metricpoints/aggregation/rules) for it

## Required Reading

In order to understand the haystack-trends one must be familiar with the [haystack](https://github.com/ExpediaDotCom/haystack) project. Its written in kafka-streams(http://docs.confluent.io/current/streams/index.html)
and hence some prior knowledge of kafka-streams would be useful.

## Technical Details
![High Level Block Diagram](documents/diagrams/haystack_trends.png)


Haystack trends is a collection of modules which reads spans and pushes aggregated metric points to kafka, each module runs as individual apps and talk to each other via kafka.

* [span-timeseries-transformer](https://github.com/ExpediaDotCom/haystack-trends/tree/master/span-timeseries-transformer) - this app is responsible
for reading spans, converting them to metric points and pushing raw metric points to kafka partitioned by metric-key

* [timeseries-aggregator](https://github.com/ExpediaDotCom/haystack-trends/tree/master/timeseries-aggregator) - this app is responsible
for reading metric points, aggregating them based on rules and pushing the aggregated metric points to kafka

The timeseries metric points are opentsdb complient and can be directly consumed by opentsdb kafka [plugin](https://github.com/OpenTSDB/opentsdb-rpc-kafka)

Sample MetricPoint :
```json
{
	"type": "Metric",
	"metric": "duration",
	"tags": {
		"client": "expweb",
		"operationName": "getOffers"
	},
	"timestamp": 1492641000,
	"value": 420
}
```

The raw and aggregated metric points are of the same json schema but are pushed to different kafka topics

## Building

#### Prerequisite:

* Make sure you have Java 1.8
* Make sure you have maven 3.3.9 or higher
* Make sure you have docker 1.13 or higher

#### Build

You can choose to build the individual subdirectories if you're working on any specific sub-app but in case you are making changes to the contract
such as span or metric point which would effect multiple modules you should run

```
make all
```
This would build all the individual apps and including unit tests, jar + docker image build and run integration tests for haystack-trends.


#### Integration Test

If you are developing and just want to run integration tests
```
make integration_test

```
