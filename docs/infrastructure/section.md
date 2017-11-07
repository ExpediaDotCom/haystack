# ![Haystack](../images/logo_small.png)

## Subsystems
The Haystack system includes an easy-to-use ["one click" deployment mechanism](../deployment/section.md), based on
[Kubernetes](https://en.wikipedia.org/wiki/Kubernetes), that deploys a working development environment with working
implementations of all of the services in the block diagram above. This same mechanism, with different configurations,
deploys to test and production environments as well. See the collection of scripts, CloudFormation templates, and YAML
files in the [haystack-deployment](https://github.com/ExpediaDotCom/haystack-deployment) repository for details.

#### Kafka
[Kafka](https://en.wikipedia.org/wiki/Apache_Kafka) is the Haystack message bus. The messages that enter the
Haystack system are [Span](https://github.com/ExpediaDotCom/haystack-idl/blob/master/proto/span.proto) objects in
[protobuf](https://en.wikipedia.org/wiki/Protocol_Buffers) format, and the modules below usually communicate with
each other via this message bus.

#### Trends
The Trends module detects anomalies in metrics, based on user-defined "trend templates" that define the levels at which
metrics should be considered "out of trend." We have built this system out of the spans we have received containing the right attributes to do this. A single anomalous metric does not necessarily merit alarming as out of
trend, so the Trends unit aggregates as directed by the trend templates. For example, a particular service call might
have a typical TP99 value of 100 milliseconds. ("TP" means "top percentile" and TP99 refers to the minimum time under
which 99% of the calls to the service have finished.) The trend template for such a service might declare that the TP99
metric is out of trend when it exceeds 150 milliseconds, a value that was chosen to be low enough to notify interested
parties of a potential problem before it becomes serious but high enough to minimize false positive alarms. The Trends
module stores its data in a Metric Tank, which stores Time Series metrics.

#### Pipes
The Pipes module delivers a human-friendly JSON version of Haystack spans to zero or more "durable" locations for more
permanent storage. The haystack-pipes module is used to send data to an external source. In our case, we will be sending our data into Athena, which will enable users to create tables to run map reduce and run reports. As part of our implementation, we provide a connector which transforms data into a JSON format to send to internal tools like Doppler. Current "plug in" candidates for such storage include:
* [Amazon Kinesis Firehose](https://aws.amazon.com/kinesis/firehose/) is an AWS service that facilitates loading
streaming data into AWS. Note that its
[PutRecordBatch API](http://docs.aws.amazon.com/firehose/latest/APIReference/API_PutRecordBatch.html) accepts up to
500 records, with a maximum size of 4 MB for each put request. The plug in will batch the records appropriately, to
minimize AWS costs. Kinesis Firehose can be configured to deliver the data to
    * [Amazon S3](https://aws.amazon.com/s3/)
    * [Amazon Redshift](https://aws.amazon.com/redshift/)
    * [Amazon Elasticsearch Service](https://aws.amazon.com/elasticsearch-service/)


#### Traces
The Spans module writes Span objects into a persistent store. That persistent store consists of two pieces: the Span
data is stored in Cassandra, and the Span metadata is stored in ElasticSearch. Sampling, with the ability to force
storing a particular Span, will be available (under configuration) to keep the size of the Cassandra and ElasticSearch
stores reasonable, given the large volume of Span objects in a production system.

#### Dependencies
The Dependencies module uses the parent/child relationships of Span objects to create dependency graphs for each
service, stored in a [Metric Tank](https://github.com/grafana/metrictank) time series metrics database.

#### UI
The UI (User Interface) module exposes (through a website) the Traces, Trends, Alerts and Dependencies created
by the other modules. To simplify the API from the UI module to the Span databases, a small Query module bundles the
data from the Cassandra store with the metadata from the ElasticSearch store.
