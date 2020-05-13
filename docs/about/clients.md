---
title: Haystack Client
sidebar_label: Clients
---

The Haystack server components are designed to provide a resilient, scalable system for gathering, storing, and retrieving tracing data. Your client components must send tracing data (spans) to the Haystack server in order for the data about how your client is working to be stored. This topic describes the server components that gather and store your client's trace data spans.

Haystack implements the [OpenTracing](http://opentracing.io/) standard tracing API for collecting trace data. OpenTracing is a vendor-neutral, open standard for distributed tracing. It's designed from the start for tracing in highly concurrent, distributed systems. OpenTracing sets common terminology and a common, [standard tracing API](http://opentracing.io/documentation/pages/api/) across various back-end products, so that you can add or switch tracing implementations with a simple configuration change rather than having to rewrite your client code to use a different proprietary API for each new tracing implementation.

## Haystack Agent

The Agent is a server that accepts span data from clients and then sends it via *dispatchers* to the rest of the Haystack system for storage or analysis. A dispatcher connects an agent to a particular data sink, with code written specifically to use that data sink. An agent can either run as a standalone service locally with the application (where the application and the service communicate via GRPC), or as a "sidecar container" connected with the application. (A sidecar container is deployed alongside the application, but runs as a separate process. The application communicates with the sidecar via a REST-like API over HTTP. The sidecar takes the burden of discovering and configuring platform services off of every client, and provides a language-agnostic interface to the application.)

In this release, Haystack includes one agent (a local GRPC agent) and three dispatchers: one for Kafka, one for AWS Kinesis, one to a remote collector via HTTP, and a dispatcher to send [blobs](./blobs.html) to S3. Code for the agent and both dispatchers is in the repo. The Haystack team invites you to contribute to this repo any dispatchers that you develop for other services. The Haystack agent libraries are in the Maven Central Repository for your convenience in writing custom agents or dispatchers.

Your application or microservice code uses the [Haystack client library](https://github.com/ExpediaDotCom/haystack-client-java) to push spans to the agent.

Please note that our fat jar only bundles the span agent and the Kinesis dispatcher, not the Kafka dispatcher.

You configure the agent as described in the following section. 

### Haystack Agent Configuration

The configuration file defines which dispatchers the agent will use, and contains the necessary configuration information for those dispatchers in addition to the configuration for the Agent. The configuration can be provided via either a local file or an HTTP endpoint. For example, the following command line invokes the agent using a local configuration file named `myAgentConfigFile`.

```java -jar target/haystack-agent.jar --config-provider myAgentConfigFile --file-path docker/dev-config.yaml```

The following example configuration loads our span agent provider, which accepts spans over GRPC at the configured port. This agent listens for GRPC requests on port 35000. It publishes spans to both Kinesis and Kafka, using the settings specified in the appropriate sections of the config (details below).

```
agents:
- name: "spans"
  props:
    port: 35000
  dispatchers:
    kinesis:
      Region: us-west-2
      StreamName: spans
      OutstandingRecordsLimit: 10000
      MetricsLevel: none
    kafka:
      bootstrap.servers: kafka-svc:9092
      producer.topic: spans
```

#### Kafka dispatcher configuration

 The Apache Kafka dispatcher uses the Kafka Producer API to write span data to a Kafka topic. (A *topic* in Kafka is a stream of *records*. A *record* is a key, a value, and a timestamp.) The Haystack Agent uses the [TraceId](https://github.com/ExpediaDotCom/haystack-idl/blob/master/proto/span.proto) in each span object as the key, and the complete Span as the value.

You must set the following configuration file entries for the Kafka dispatcher in your config file:
* `producer.topic` - The Kafka Topic ID for sharing span data.
* `bootstrap.servers` - A list of host/port pairs for connecting to your Kafka cluster.

You can specify other Kafka producer properties in the config file by placing them in the same section of the config file where the sample above puts the `bootstrap.servers` setting.

#### Kinesis dispatcher configuration
The Kinesis dispatcher uses the Amazon Kinesis Producer Library ([KPL](https://github.com/awslabs/amazon-kinesis-producer)), and requires the following configuration properties be set properly in order to work:

a. `Region` - The AWS region where the Kinesis server is located. For example, 'us-west-2'.
b. `StreamName` - The name of the Kinesis stream where spans will be published.
c. `OutstandingRecordsLimit` - The maximum number of pending records that are allowed to be not yet published to Kinesis by the Agent. If a request to the Agent would cause it have more pending records for Kinesis than this limit, then the Agent sends back 'RATE_LIMIT_ERROR' in its response and does not store the records for publishing to Kinesis.

If you are using Kinesis, then in addition to the configuration file entries above, you also need to provide AWS_ACCESS_KEY and AWS_SECRET_KEY as Java system properties or environment variables, or [use the IAM role](https://docs.aws.amazon.com/streams/latest/dev/controlling-access.html) for connecting to Kinesis.

Kinesis dispatcher can be configured with other [KPL properties](https://github.com/awslabs/amazon-kinesis-producer/blob/master/java/amazon-kinesis-producer-sample/default_config.properties) in addition to `Region` by including them in the same part of the config file where `Region` is set.

#### Http dispatcher configuration
The http dispatcher posts span to a remote collector, commonly Haystack's [http span collector](https://github.com/ExpediaDotCom/haystack-collector/tree/master/http)

The following configuration is used for the http collector:

* `url` - url for the http span collector (eg: http://collector-svc:8080/spans)
* `client.timeout.millis` - timeout in milliseconds for reporting spans to the collector. Defaults to 500 ms.
* `client.connectionpool.idle.max` - number of idle connections to keep in the connection pool. Defaults to 5
* `client.connectionpool.keepalive.minutes` - keep alive duration in minutes for connections in the connection pool. Defaults to 5.

### Pitchfork Agent

Haystack agent can leverage [Pitchfork](https://github.com/ExpediaGroup/pitchfork) to transform Zipkin-style data so that it can be ingested by the Haystack backend. By using the Pitchfork mode of the agent, you can deploy the agent with your zipkin-compatible app as a sidecar or daemon without make any code changes in your application. 

Pitchfork uses the Agent's http server and supports the following endpoints for publishing Zipkin spans: 
* `/api/v1/spans` - accepts v1 spans(json, thrift)
* `/api/v2/spans` - accepts v2 spans(json, proto)

The following configuration is used to enable the Pitchfork agent: 

```
agents {
     pitchfork {
       enabled = true
       port = 9411
       http.threads {
          max = 16
          min = 2
       }
       idle.timeout.ms = 60000
       stop.timeout.ms = 30000
       accept.null.timestamps = false
       max.timestamp.drift.sec = -1
   
       dispatchers {
         kinesis {
           Region = us-west-2
           StreamName = spans
           OutstandingRecordsLimit = 10000
           MetricsLevel = none
         }
         
         // more dispatchers
       }
     }
   }
   ```

## Metrics

The client has a stand-alone metrics interface that is based on [Micrometer's API][micrometer].  We supply a `NoopMetricsRegistry` with the base library that collects nothing.

There is an optional integration for Micrometer's registry, `GlobalMetricsRegistry` and `MicrometerMetricsRegistry`.  Depending on your project's needs you can use the `Metrics.globalRegistry` from Micrometer or supply your own custom registry instance, respectively.

There isn't yet an extension, but Dropwizard registries are supported.  You can see how those are currently being used in the Dropwizard integration and example within this project.

[micrometer]: http://micrometer.io/
