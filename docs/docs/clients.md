---
layout: default
title: Client Libraries
---
# Haystack Client

## Why OpenTracing?

Instrumenting a system for tracing has long been a labor-intensive and complicated task. Tracing brings the benefits of visibility into an application as it grows in number of processes, starts seeing increased concurrency, or non-trivial interactions between mobile/web clients and servers. But setting up instrumentation and deciding which tracer to use can add up to a large project. The OpenTracing standard changes that, making it possible to instrument applications for distributed tracing with minimal effort providing standardization across the globe.

## Architecture


# Haystack Agent

## Why Agent?


For pull based model, refer to [Collectors](https://expediadotcom.github.io/haystack/subsystems/collectors.html).


## What is Haystack Agent?

This repo contains haystack-agent that can be run as a side-car container or a standalone agent on the same host of micro service application. One need to add the haystack [client library](https://github.com/ExpediaDotCom/haystack-client-java) in the application to push the [spans](https://github.com/ExpediaDotCom/haystack-idl) to this agent running locally(or a side car container).

The haystack span-agent runs as a grpc server that accepts the spans. It collects the spans and dispatch them to one or more sinks e.g. kafka, aws kinesis etc. depending upon the configuration.

We provide couple of dispatchers out of the box for e.g kafka and aws kinesis. We strongly encourage open source community to contribute dispatchers in this repo. However one is free to use these agent libraries published in maven central to write custom agents or dispatchers in a private repo.

## Architecture

The haystack-agent uses the [SPI](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html) design architecture. The fat jar that gets built of this code contains single agent providers with two dispatchers (kinesis and kafka) discussed in more detail below. However design allows to add more agent and dispatcher providers. The agents are loaded depending upon the configuration that can be provided via a http endpoint or a local file like

java -jar target/haystack-agent.jar --config-provider file --file-path docker/dev-config.yaml
The main method in AgentLoader class loads and initializes the agents using ServiceLoader.load(). Each agent further loads the configured dispatchers using the same ServiceLoader.load() mechanism and everything is controlled through configuration.

### Haystack Agent Configuration

The configuration readers are also implemented using the SPI design model. For now, we are only using file config provider that is implemented [here](https://github.com/ExpediaDotCom/haystack-agent/tree/master/config-providers/file).

Following is an example configuration that loads a single agent providers that reads the spans(proto) over grpc. This span agent spins up a grpc server listening on different ports 34000 and publish them to kinesis and kafka as per configuration below.

The app or microservice need to use a grpc client to send messages to this haystack-agent.

agents:
- name: "spans"
  props:
    port: 34000
  dispatchers:
    kinesis:
      Region: us-west-2
      StreamName: spans
      OutstandingRecordsLimit: 10000
      MetricsLevel: none
    kafka:
      bootstrap.servers: kafka-svc:9092
      producer.topic: spans

## Agent Providers

We have one agent provider today that is loaded depending upon the configuration as above.

### Span Proto Agent

This agent listens as a grpc server on a configurable port and accepts the [span proto](https://github.com/ExpediaDotCom/haystack-idl/tree/master/proto/agent) from the clients. The span agent is already implemented in the open source repo and it supports both kinesis and kafka dispatchers. Please note we only bundle this span-agent and kinesis dispatcher in our fat jar.

## Dispatchers

### Kinesis Dispatcher

Kinesis dispatcher uses [KPL](https://github.com/awslabs/amazon-kinesis-producer) and we require following mandatory configuration properties for it to work.

a. Region - aws region for e.g. us-west-2
b. StreamName - name of kinesis stream where spans will be published
c. OutstandingRecordsLimit - maximum pending records that are still not published to kinesis. If agent receives more dispatch requests, then it sends back 'RATE_LIMIT_ERROR' in grpc response.

You need to provide AWS_ACCESS_KEY and AWS_SECRET_KEY as java system property or environment variable or use the IAM role for connecting to Kinesis.
Kinesis dispatcher can be configured with other [KPL properties](https://github.com/awslabs/amazon-kinesis-producer/blob/master/java/amazon-kinesis-producer-sample/default_config.properties) in the same way as we do with 'Region'

### Kafka Dispatcher

Kafka dispatcher uses high level kafka producer to write the spans to kafka topic. The dispatcher expects a partition key and the span-agent uses the [TraceId](https://github.com/ExpediaDotCom/haystack-idl/blob/master/proto/span.proto) in span proto object for the same.

a. producer.topic - kafka topic
b. bootstrap.servers - set of bootstrap servers

Kafka dispatcher can be configured with other kafka producer properties in the same way as we do with bootstrap.servers.

