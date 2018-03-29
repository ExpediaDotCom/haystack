---
layout: default
title: Collector
---
# Collector

Collector is the Haystack subsystem that ingests [spans](https://github.com/ExpediaDotCom/haystack-idl) from data sources other than those that report trace data directly to Haystack. Its purpose is to make it easy to integrate existing data streams of spans with Haystack.

Most organizations already have infrastructure for their streaming and offline data. The idea behind the Collector subsystem is to simplify the integration of such existing data ecosystems with Haystack. Your microservices or applications may continue using their existing method for publishing trace spans (such as AWS Kinesis or MySQL). The Collector subsystem components can optionally be deployed as part of Haystack, and support reading the spans from these external data sources and writing them to Kafka in a format compatible with the rest of Haystack. Collectors support a pull based ingestion model, which allows you to integrate Haystack without introducing major changes to your existing infrastructure.

Note that you only need a Collector module if you're getting span data from outside sources. If your services report span data directly to the Haystack [Agent](../clients.md) subsystem, it dispatches the data appropriately to the rest of Haystack. Collector modules operate on a pull-based model (pulling from external data sources), while Agent operates on a push model (your services push trace data to Haystack).

## Modules
The Collector subsystem has been designed to provide modular components, where each component manages a particular data source. At this time, we provide modules that ingest data from:
* **AWS Kinesis**: AWS Kinesis is a highly popular streaming solution provided by AWS for building real time applications. The Haystack [Kinesis collector](https://github.com/ExpediaDotCom/haystack-collector/tree/master/kinesis) provides an easy integration channel by reading the spans data from Kinesis and writing to Haystack's Kafka instance. 

Each collector type is published as a separate Docker image under the ExpediaDotCom account on the Docker Hub. 

We want to highlight the fact that we are not limited to importing data only from AWS Kinesis -- that's just where we started producing Collector modules. We will be adding modules in the future, and we solicit your Open Source contributions here if you choose to write and share more Connector modules for improving the integration capabilities of Haystack. Check out our [contributing](../contributing.html) guidelines for more details. 

