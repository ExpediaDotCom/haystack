# haystack
Repository for public open source Haystack

## Overview
[Haystack](https://github.com/ExpediaDotCom/haystack) is an Expedia-backed open source project to facilitate detection 
and remediation of problems with enterprise-level web services and websites.

There was a version 1 of Haystack, internal to Expedia, used by different teams. Version 1 leveraged existing software 
([Kibana],(https://en.wikipedia.org/wiki/Kibana) Expedia's Doppler, [Zipkin](http://zipkin.io/)) without the need to 
build significant pieces of UI. Version 2 adds new UI and features, but has similar use cases and goals.
  
### The Problem
Expedia customers rely on hundreds of services to book their travel. These services are spread across many platforms, 
data centers and teams. Each service logs information. There are different types of logs (they can be categorized in 
different ways; the list below is one such way) 
1. Metrics (typically counts and durations), 
2. Application logs (interesting information emitted by the service, usually via log4j or a similar system)
3. Transaction logs (key/value pairs of significant events in the system)
4. Request/response logs (the XML, JSON, etc. sent to and from the service)

Historically, Expedia services sent metrics to [graphite](https://graphiteapp.org/), but most applications also dumped
logs into Splunk so that we could troubleshoot issues and see trends. Splunk was an expensive place to store terabytes
of logs, and debugging was still difficult: 

1. We were unable to correlate and trace the service calls invoked by a customer request across the entire stack, making
it both difficult and time-consuming to resolve live site issues and improve system performance. 
2. Splunk required knowledge of 
    * the right instance to use (there was too much data for one Splunk installation to consume!) 
    * the Splunk query language
    * the logging formats used by the particular service. 
3. Some of the request/response pairs logged were too big for Splunk. Solutions for this problem included:
    * [ELK](https://www.elastic.co/webinars/introduction-elk-stack)
    * stashing data in S3
    * a homegrown CRSLogs system
    
### The Solution
![High Level Block Diagram](documentation/diagrams/Haystack%20Components.png)

Below find information about components in the block diagram above.

#### Clients and Kinesis
Clients of Expedia Haystack send data to a [Kinesis](https://aws.amazon.com/kinesis/) stream. Using Kinesis facilitates
communication across different [VPCs](https://aws.amazon.com/vpc/). The data sent to Kinesis must conform to the Expedia
[Interface Description Language (IDL)](https://en.wikipedia.org/wiki/IDL_specification_language) whose details can be 
found in [the haystack-idl github repository](https://github.com/ExpediaDotCom/haystack-idl).
#### Infrastructure
The Haystack system includes an easy-to-use "one click" deployment mechanism, based on 
[Kubernetes](https://en.wikipedia.org/wiki/Kubernetes), that deploys a working development environment with working
implementations of all of the services in the block diagram above. This same mechanism, with different configurations, 
deploys to test and production environments as well. See the collection of scripts, CloudFormation templates, and YAML 
files in the [haystack-deployment](https://github.com/ExpediaInc/haystack-deployment) repository for details.
#### Collector
#### Kafka
#### Trends
The Trends unit detects anomalies in metrics, based on user-defined "trend templates" that define the levels at which
metrics should be considered "out of trend." A single anomalous metric does not necessarily merit alarming as out of
trend, so the Trends unit aggregates as directed by the trend templates. For example, a particular service call might
have a typically TP99 value of 100 milliseconds. ("TP" means "top percentile" and TP99 refers to the minimum time under 
which 99% of the calls to the service have finished.) The trend template for such a service might declare that the TP99
metric is out of trend when it exceeds 150 milliseconds, a value that was chosen to be low enough to notify interested
parties of a potential problem but high enough to minimize false positive alarms. The Trends unit contains the following
packages:
1. [haystack-span-timeseries-mapper](https://github.com/ExpediaDotCom/haystack-span-timeseries-mapper): reads spans from
Kafka and aggregates them by service name and span name. The aggregated values are written to another Kafka topic.
2. [haystack-timeseries-aggregator](https://github.com/ExpediaDotCom/haystack-timeseries-aggregator)
#### Stitcher
#### Spans
#### Dependencies
#### External
The External unit delivers a human-friendly version of Haystack messages to zero or more "durable" locations for more
permanent storage. Current "plug in" candidates for such storage include:
1. [Amazon Kinesis Firehose](https://aws.amazon.com/kinesis/firehose/) is an AWS service that facilitates loading 
streaming data into AWS. Note that its 
[PutRecordBatch API](http://docs.aws.amazon.com/firehose/latest/APIReference/API_PutRecordBatch.html) accepts up to
500 records, with a maximum size of 4 MB for each put request. The plug in will batch the records appropriately.
Kinesis Firehose can be configured to deliver the data to
    * [Amazon S3](https://aws.amazon.com/s3/)
    * [Amazon Redshift](https://aws.amazon.com/redshift/)
    * [Amazon Elasticsearch Service](https://aws.amazon.com/elasticsearch-service/)
2. Doppler Time Series (DTS) is an Expedia Time Series Database (TSDB) written on top of Cassandra and ElasticSearch. 

The [haystack-external-json-transformer](https://github.com/ExpediaDotCom/haystack-external-json-transformer) package is
a lightweight service that uses [Kafka Streams](https://kafka.apache.org/documentation/streams/) to read the protobuf 
records from Kafka, transform them to JSON, and write them to another topic in Kafka. The plugins will then consume
from the latter topic, and then write the JSON records just consumed to their destinations.
#### User Interface
The mock UI is visible at http://haystack-web.test.expedia.com/.
Packages in the Haystack UI System:
1. [haystack-ui](https://github.com/ExpediaDotCom/haystack-ui)
2. [haystack-trace-query](https://github.com/ExpediaDotCom/haystack-trace-query)
