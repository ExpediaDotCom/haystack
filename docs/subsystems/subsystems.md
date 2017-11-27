# Haystack Subsystems

All our subsystems are included and ready to use. The design, however, is in such a way that makes them replaceable as well. The Haystack system includes an easy-to-use ["one click" deployment mechanism](../deployment/section.md), based on
[Kubernetes](https://en.wikipedia.org/wiki/Kubernetes), that deploys a working development environment with working
implementations of all of the services in the block diagram above. This same mechanism, with different configurations,
deploys to test and production environments as well. See the collection of scripts, CloudFormation templates, and YAML
files in the [haystack-deployment](https://github.com/ExpediaDotCom/haystack-deployment) repository for details.

#### Kafka
[Kafka](https://en.wikipedia.org/wiki/Apache_Kafka) is the Haystack message bus. The messages that enter the
Haystack system are [Span](https://github.com/ExpediaDotCom/haystack-idl/blob/master/proto/span.proto) objects in
[protobuf](https://en.wikipedia.org/wiki/Protocol_Buffers) format, and the modules below usually communicate with
each other via this message bus.

#### Traces
Traces is a subsystem included in Haystack that provides a distributed tracing system to troubleshoot the problems in microservice architectures. Its design is based on the [Google Dapper](https://research.google.com/pubs/pub36356.html) paper. The Spans module writes Span objects into a persistent store. That persistent store consists of two pieces: the Span data is stored in Cassandra, and the Span metadata is stored in ElasticSearch. Sampling, with the ability to force storing a particular Span, will be available (under configuration) to keep the size of the Cassandra and ElasticSearch stores reasonable, given the large volume of Span objects in a production system.

#### Trends
The trends subsystem is responsible for reading the spans and generating the vital service health trends. Haystack collects all the data from the various services and creates the distributed call graph and depicts the time taken by that call across various services. This information can be hard to make sense of unless we have a trend to compare this against. The Trends module stores its data in a Metric Tank, which stores Time Series metrics.

#### Collector
Collector is a subsystem included in Haystack that ingests the spans from other data sources. Its purpose is to make it easy to integrate existing data streams of spans with Haystack. The idea of collectors is to simplify the integration of such existing data ecosystems with Haystack.

#### Pipes
The Pipes module delivers a human-friendly JSON version of Haystack spans to zero or more "durable" locations for more
permanent storage. The haystack-pipes module is used to send data to an external source. In our case, we will be sending our data into Athena, which will enable users to create tables to run map reduce and run reports. As part of our implementation, we provide a connector which transforms data into a JSON format to send to internal tools like Doppler.

#### Dependencies
The Dependencies module uses the parent/child relationships of Span objects to create dependency graphs for each
service, stored in a [Metric Tank](https://github.com/grafana/metrictank) time series metrics database.

#### Anomaly Detection
Anomaly detection intends to find out anomalies in service health and trigger alerts. It used Trends as its data source.

#### UI
The UI (User Interface) module exposes (through a website) the Traces, Trends, Alerts and Dependencies created
by the other modules. To simplify the API from the UI module to the Span databases, a small Query module bundles the
data from the Cassandra store with the metadata from the ElasticSearch store.

#### Client
We have an [OpenTracing][opentracing] compliant client to facilitate
easy integrations with the ecosystem.  Currently, we only support
clients directly integrated with the OpenTracing API and don't have
compatibility built-in to deal with other tracing systems.

[opentracing]: http://opentracing.io/
