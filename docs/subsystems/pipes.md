# Haystack Pipes

## Why Pipes?
Packages to send ("pipe") Haystack data to external data sources (like AWS S3). The haystack-pipes unit delivers a human-friendly version of Haystack messages to zero or more "durable" locations for more permanent storage. 

# High Level Block Diagram
![High Level Block Diagram](images/haystack_pipes.png)

The **haystack-pipes** module is used to send data to an external source. In our case, we will be sending our data into S3, which will enable users to create tables for adhoc queries using Athena and run mapreduce jobs using Spark. As part of our implementation, we provide a connector which transforms data into a JSON format to send to internal tools like Doppler. Current "plug`in" candidates for such storage include:

1. [Kafka Producer](https://github.com/ExpediaDotCom/haystack-pipes/tree/master/kafka-producer)
2. [Amazon Kinesis Firehose](https://aws.amazon.com/kinesis/firehose/) is an AWS service that facilitates loading streaming data into AWS. Note that its [PutRecordBatch API](http://docs.aws.amazon.com/firehose/latest/APIReference/API_PutRecordBatch.html) accepts up to 500 records, with a maximum size of 4 MB for each put request. The plug in will batch the records appropriately. Kinesis Firehose can be configured to deliver the data to-
* [Amazon S3](https://aws.amazon.com/s3/)
* [Amazon Redshift](https://aws.amazon.com/redshift/)
* [Amazon Elasticsearch Service](https://aws.amazon.com/elasticsearch-service/)

## Common Library

### json-transformer

The json-transformer package is a lightweight service that uses Kafka Streams to read the protobuf records from Kafka, transform them to JSON, and write them to another topic in Kafka. The plugins will then consume from the latter topic, and then write the JSON records just consumed to their destinations.

## Pipes

### kafka-producer

The kafka-producer service uses [Kafka Streams](https://kafka.apache.org/documentation/streams/) to read the protobuf records from Kafka, transform them to tags-flattened JSON, and write the transformed record to another (typically external) Kafka queue and topic. The code is simple and self-explanatory and consists of the following classes:

1. A [transformer](https://github.com/ExpediaDotCom/haystack-pipes/blob/master/kafka-producer/src/main/java/com/expedia/www/haystack/pipes/kafka-producer/ProtobufToJsonTransformer.java) that wires the deserializer and serializer into a [Kafka Streams](https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams) pipeline.
2. [Unit tests](https://github.com/ExpediaDotCom/haystack-pipes/tree/master/kafka-producer/src/test/java/com/expedia/www/haystack/pipes)

### firehose-writer

