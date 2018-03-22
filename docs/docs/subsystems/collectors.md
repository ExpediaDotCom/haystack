---
layout: title-page
title: Collectors
---
# Haystack Collectors

Collector is a subsystem included in Haystack that ingests the [spans](https://github.com/ExpediaDotCom/haystack-idl) from other data sources. Its purpose is to make it easy to integrate existing data streams of spans with Haystack.

### Why Collectors?
Most organizations whether small or big already have infrastructure for their streaming and offline data. The idea of collectors is to simplify the integration of such existing data ecosystems with Haystack. The microservices or applications may continue using the existing data sources for e.g. AWS Kinesis, MySQL and more, for publishing the spans. The collectors, that can optionally be deployed as part of haystack framework, support reading the spans from these external data sources and write them to kafka with the partitioning strategy using the span's TraceId as a key. 


#### Pull Based Model 
The collectors fall under the umbrella of pull based data ingestion model. This is certainly more useful for the organizations that want to integrate haystack without introducing major changes in their existing infrastructure.   

##### Architecture
The collectors have been designed as modular components where each collector manages a particular data source. At the point of writing, we support the data ingestion from:
- **AWS Kinesis**: AWS Kinesis is a highly popular streaming solution provided by AWS for building real time applications. Many companies use AWS Kinesis stream and haystack [kinesis collector](https://github.com/ExpediaDotCom/haystack-collector/tree/master/kinesis) provides an easy integration channel by reading the spans data from kinesis and writing to haystack kafka. 

Each collector type is published as a separate docker image under ExpediaDotCom account of the dockerhub. We want to highlight the fact that we are not limited to AWS Kinesis. We strongly encourage the contributions from open source community to write more connectors for improving the integration strategy of  haystack. Checkout our contributing guidelines for more details. 

#### Push Based Model
Our ambition in haystack is to build plug and play components, and haystack-collector is one of them. For the use cases where an application can directly write spans to haystack kafka, we provide a grpc based agent that can be deployed as a sidecar container or run locally with an application. It collects the spans over grpc and push them directly to kafka. For more reading, go to [agent](https://expediadotcom.github.io/haystack/clients.html)