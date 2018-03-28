---
layout: default
title: Client Deployment
---
# Clients Deployment

We have an OpenTracing compliant client to facilitate easy integrations with the ecosystem. Currently, we only support clients directly integrated with the OpenTracing API and don't have compatibility built-in to deal with other tracing systems.

### Local discoveralbility

/etc/host can have haystack agent running on 127.0.0.1. We can call it by simply using haystack-agent. We need this dicoverability for docker, if we write localhost in code, then it wont be able to talk to it. 

### Ports
