---
title: Traces
sidebar_label: Traces
---

Traces is a subsystem included in Haystack that provides a distributed tracing system to troubleshoot problems in microservice architectures. Its design is based on the [Google Dapper](http://research.google.com/pubs/pub36356.html) paper.

In a modern microservice architecture, requests often span multiple services. Each service handles a request by performing one or more operations, such as invoking downstream services over HTTP or GRPC, querying databases, or publishing to an event bus. To understand behavior and troubleshoot problems in a system that deploys tens or hundreds of microservices is a complex task. Application-level monitoring and log entries scattered across multiple servers may not be useful enough to detect a problem, let alone help pinpoint the specific server or service that's the point of failure. 

Haystack addresses this problem in the Traces subsystem, by reading the trace [spans](https://github.com/ExpediaDotCom/haystack-idl/blob/master/proto/span.proto) emitted by microservices and stitching them together based on data that identifies spans and their parent-child relationships. The Traces subsystem also provides a search capability on the metadata attached with every span and presents a view showing all the services and their operations as a time vector. This helps a developer or system administrator to understand the flow of calls and diagnose latencies across a set of services. 

### Architecture
The traces subsystem is composed of the following components:

![Trace subsystem architecture diagram](/haystack/img/traces-architecture.png)

#### Indexer
The role of the Indexer is to read spans from Kafka and group them together based on their unique traceId. It writes the resulting [grouped data structure](https://github.com/ExpediaDotCom/haystack-idl/blob/master/proto/spanBuffer.proto) to [Cassandra](http://cassandra.apache.org/) and [ElasticSearch](https://aws.amazon.com/elasticsearch-service/). Cassandra is used as a raw data store where all the spans are inserted with TraceId as a primary key. ElasticSearch is used to build an index on the metadata, serviceName and operationName associated with every span. This helps the Reader service to query ElasticSearch for contextual searches such as "fetch all TraceId(requests) that have any span with serviceName=xyz and a metadata tag success=false". 

It's not used yet, but in addition to storing the grouped span data in Cassandra and ElasticSearch, the Indexer publishes them to a different Kafka topic that may help solving newer use cases in future with this enriched data.  

The Indexer also controls the attributes to be indexed, through a whitelist configuration. The Indexer reads the whitelist from an external store and applies the list dynamically. The goal is to help organizations in controlling  and scaling their infrastructure spend (especially ElasticSearch) more proactively. 

#### Reader
The Reader runs as a GRPC server and serves Haystack UI to fetch traces directly from Cassandra for a TraceId or use ElasticSearch for contextual searches. For a search, the Reader component queries ElasticSearch, which returns a set of TraceIds. Reader then pulls from Cassandra all the spans for each unique TraceId, and applies the following transformations on the spans associated with a TraceId to build a complete representation:

* **TraceValidation**: Validates that required fields in the span exist (for example `serviceName` and `operationName`), and that only one span (the root span) has a ParentMessageId of `null`.

* **Partial Span Merge**: The Indexer component can miss grouping all spans together for every TraceID in one go, and can emit multiple partial span-groups. This can happen for many reason, including outages or if the in-memory cache of the Indexer forces an early eviction. This transformation merges partial span-groups properly.

* **De-duplication**: Duplicate spans can exist due to Kafka sending multiple times to meet its at-least-once guarantee. This transformer removes duplicates with the same SpanID.
    
* **ClockSkew**: This fixes the clock skew between parent and child span. If any child span reports a startTime earlier then the parent span's startTime, then the corresponding delta gets added in the subtree with the child span as root. We are evaluating better strategies to fix the clock skew problem in a distributed system.
     
The [Trace View](../ui/ui_traces.html) in the user interface reads the stitched view of all the span and sub-span data for a given TraceID, and renders it:

<img src="/haystack/img/trace_timeline.png" style="width: 800px;"/>
