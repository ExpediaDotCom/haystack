---
layout: default
title: Client Deployment
---
# Clients Deployment

We have an [OpenTracing](http://opentracing.io/)-compliant client library to facilitate easy integration of Haystack with your ecosystem. Your application or microservice code uses the [Haystack client library](https://github.com/ExpediaDotCom/haystack-client-java) to push spans to the agent. Currently, we only support clients that use this library. We do not have compatibility built-in to deal with trace data sent using tracing systems. If you have existing client code that is written to send trace data to other systems, investigate using the [Collector subsystem](../subsystems/collectors.html) to import that data into Haystack.

### Local discoverability

`/etc/host` can have haystack agent running on 127.0.0.1. We can call it by simply using haystack-agent. We need this discoverability for Docker, if we write localhost in code, then it wont be able to talk to it. 
