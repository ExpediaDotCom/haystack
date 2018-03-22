---
layout: title-page
title: Front Page
---

![Haystack Logo](/docs/images/logo.png)


## Resilient, scalable enterprise tracing and analysis 

[Haystack](https://github.com/ExpediaDotCom/haystack) is an Expedia-backed open source project to facilitate detection
and remediation of problems with enterprise-level web services and websites. It combines an [OpenTracing](http://opentracing.io/documentation/)-compliant trace engine with
a componentized back-end architecture designed for high resiliency and high scalability. Haystack also includes analysis tools for visualizing trace data, tracking trends in trace data, and setting alarms when trace data trends exceed limits you set.

With Haystack's alarms, you can automatically detect when your web service or website isn't performing properly. With Haystack's trace visualization tools, you can quickly determine the part of your system that is slowing or failing transactions, reducing the time it takes to get from knowing there's a problem to remedying the problem.

As with any OpenTracing implementation, your services must add code to pass trace data from caller to callee so that the sequence of calls can be reconstructed later. Haystack includes general-purpose Java classes to do this, and integrations for those classes into several popular frameworks.

### The Problem
Modern websites rely on dozens or even hundreds of services to function. These services are often spread across many
platforms, data centers, and teams. Each service logs information of various kinds:

* Telemetry data with tags or key/value pairs associated with the event
* Request/response logs (the XML, JSON, etc. sent to and received from the service)
* Application logs (typically errors logged via log4j or a similar system)
* Metrics

With so much information spread across many different places, it can be challenging and time consuming to figure out where to look for the cause of a failure or slowdown. 

### The Solution
Haystack uses tracing data from a central store to help you locate the source of the problem -- to drill down to the precise part of a service transaction where failures or latency are occurring -- and find the proverbial "needle in a haystack". Once you know specifically where the problem is happening, it's much easier to identify the appropriate diagnostic data, understand the data, find the problem, and fix it.

