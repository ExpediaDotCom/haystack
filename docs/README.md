<img src="src/images/logo.png" style="width: 600px;"/>

[Haystack](https://github.com/ExpediaDotCom/haystack) is an Expedia-backed open source project to facilitate detection
and remediation of problems with enterprise-level web services and websites.

### The Problem
Modern websites rely on dozens or even hundreds of services to function. These services are often spread across many
platforms, data centers, and teams. Each service logs information. There are different types of diagnostics data (they can be
categorized in different ways; the list below is one such way)
1. Metrics,
2. Application logs (typically errors logged via log4j or a similar system)
3. Transaction logs (telemetry data with tags or key/values associated with the event)
4. Request/response logs (the XML, JSON, etc. sent to and received from the service)

Haystack is intended to help users make sense of the information in these transaction logs, particularly when things are not
working quite right in the cluster of services (increased latency, failing service calls, etc.): to find service and/or network latency that is causing the issue. i.e., "the needle in the haystack" (thus the name).
