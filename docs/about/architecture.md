---
title: Architecture
sidebar_label: Architecture
---

## Haystack Components Architecture Diagram

The Haystack architecture is designed on the distributed design principles of building decentralized and decoupled systems.
To enable that, we have used [Apache Kafka](http://kafka.apache.org/) as the nervous system that helps us achieve the following architectural principles in the Haystack server:

* Haystack is **Componentized**: Haystack includes all of the necessary subsystems to make the system ready to use. But we have also ensured that the overall system is designed in such a way that you can replace any given subsystem to better meet your own needs. 
* Haystack is **Highly Resilient**: There is no single point of failure. 
* Haystack is **Highly Scalable**: We have completely decentralized our system which helps us to scale every component individually. 

![Haystack architecture diagram](/haystack/img/haystack-architecture.png)

We provide client components for the client applications or microservices that send trace data to be recorded by the `haystack-agent` subsystem:

* [Clients](./clients.html)

We provide the following six subsystems within the server:

* [Traces](../subsystems/subsystems_traces.html)
* [Trends](../subsystems/subsystems_trends.html)
* [Collectors](../subsystems/subsystems_collectors.html)
* [Pipes](../subsystems/subsystems_pipes.html)
* [Dependencies](../subsystems/subsystems_dependencies.html)
* [Anomaly Detection](../subsystems/subsystems_anomaly_detection.html)

Trace data is stored using different services, including:

* [Amazon Kinesis Data Firehose](https://aws.amazon.com/kinesis/data-firehose/) to external durable storage.
* [Grafana MetricTank](https://github.com/grafana/metrictank) for time series metrics.
* [Apache Cassandra](http://cassandra.apache.org/) for raw traces and for trends data.
* [AWS ElasticSearch](https://aws.amazon.com/elasticsearch-service/) is used as a metadata indexer.

And we provide User Interface components for viewing and analyzing trace data:

* [Traces View](../ui/ui_traces.html)
* [Trends View](../ui/ui_trends.html)
* [Alerts View](../ui/ui_alerts.html)

