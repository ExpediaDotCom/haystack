---
title: Clients Deployment
sidebar_label: Clients
---

We have an [OpenTracing](http://opentracing.io/)-compliant client libraries to facilitate easy integration of Haystack with your ecosystem. Haystack has a number of clients that can be used to push spans to the [Haystack Agent](../about/clients.html#haystack-agent). These include:
 * [Java client](https://github.com/ExpediaDotCom/haystack-client-java) 
 * [Node client](https://github.com/ExpediaDotCom/haystack-client-node)
 * [Python client](https://github.com/ExpediaDotCom/haystack-client-python)
 * [Go client](https://github.com/ExpediaDotCom/haystack-client-go)
 * [Dropwizard integration](https://github.com/ExpediaDotCom/haystack-dropwizard) that utilizes the Java client
 * [Spring starter](https://github.com/ExpediaDotCom/opentracing-spring-haystack-starter)  that utilizes [opentracing-spring-cloud-starter](https://github.com/opentracing-contrib/java-spring-cloud)
  
  
Additionally, with [Pitchfork mode of the Agent](../about/clients.html#pitchfork-agent), you are able to send spans from any Zipkin-compatible client to the agent. If you have existing client code that is written to send trace data to other systems, investigate using the [Collector subsystem](../subsystems/subsystems_collectors.html) to import that data into Haystack.

### Local discoverability

`/etc/host` can have haystack agent running on 127.0.0.1. We can call it by simply using haystack-agent. We need this discoverability for Docker, if we write localhost in code, then it wont be able to talk to it. 
