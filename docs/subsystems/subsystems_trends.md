---
title: Trends
sidebar_label: Trends
---

The Trends subsystem is responsible for reading spans and generating vital service health trends.

Haystack collects trace data from services, creates a distributed call graph from that data, and depicts the time taken by calls across various services.
By identifying the trends of change in the call pattern or the time taken to complete calls, Haystack makes it easier to identify which part of a complex system is responsible for a change in the system's responsiveness.


## What we Trend

We currently  compute four trends for each combination of `service` and `operation` contained in the [span](https://github.com/ExpediaDotCom/haystack-idl/blob/master/proto/span.proto) pushed to Haystack.

* total_count `[count]`
* success_count `[count]`
* failure_count `[count]`
* duration `[mean, median, std-dev, 99 percentile, 95 percentile]`

Each trend is computed for 4 intervals `[1min, 5min, 15min, 1hour]`.

## Architecture

![Trends Architecture](/haystack/img/trends-architecture.png)

The Trends service is a loosely coupled system that uses Kafka as its backbone. It is a collection of modules which reads spans and pushes aggregated metric points to Kafka. Each module runs as an individual app, and the apps talk to each other via Kafka.
    
* [haystack-span-timeseries-transformer](https://github.com/ExpediaDotCom/haystack-trends/tree/master/span-timeseries-transformer) - This app is responsible 
for reading spans, converting them to metric points and pushing raw metric points to Kafka, partitioned by metric-key.

* [haystack-timeseries-aggregator](https://github.com/ExpediaDotCom/haystack-trends/tree/master/timeseries-aggregator) - This app is responsible 
for reading metric points, aggregating them based on rules and pushing the aggregated metric points to Kafka.

The time series metric points are [MetricTank](https://github.com/grafana/metrictank) compliant and can be directly consumed by MetricTank. 

### Extensions
 
 1. Trends - Other than the four default trends more trends can be computed by adding a [transformer](https://github.com/ExpediaDotCom/haystack-trends/tree/master/span-timeseries-transformer/src/main/scala/com/expedia/www/haystack/trends/transformer) to create the metric point and adding an [aggregation-rule](https://github.com/ExpediaDotCom/haystack-trends/tree/master/timeseries-aggregator/src/main/scala/com/expedia/www/haystack/trends/aggregation/rules) for it. 

 2. TimeSeries Database - Haystack supports MetricTank as its time series store by default. If you use another time series database, you can write another [adapter](https://github.com/ExpediaDotCom/haystack-commons/blob/master/src/main/scala/com/expedia/www/haystack/commons/kstreams/serde/metricpoint/MetricTankSerde.scala) for that database, and configure Trends to use it.


The Trends system maintains trend information and provides it for subsequent display to users in the [Trends UI](../ui/ui_trends.html) and analysis by automation like the [Anomaly Detection](./subsystems_anomaly_detection.html) subsystem.

![Trends UI](/haystack/img/trends.png)
