---
layout: default
title: Architecture
---
# Architecture

## Haystack Components Architecture Diagram
![Haystack architecture diagram](/docs/images/Haystack_Components.png)

The Haystack architecture is designed on the distributed design principles of building decentralized and decoupled systems.
To enable that, we have used [Apache Kafka](http://kafka.apache.org/) as the nervous system that helps us achieve the following:

* **Componentized, Docker-like Design**: Haystack includes all of the necessary subsystems to make the system ready to use. But we have also ensured that the overall system is designed in such a way that you can replace any given subsystem to better meet your own needs. 
* **Highly Resilient**: There is no single point of failure. 
* **Highly Scalable**: We have completely decentralized our system which helps us to scale every component individually. 

We provide the following six subsystems:

1. [Traces](/docs/subsystems/traces.html)
2. [Trends](/docs/subsystems/trends.html)
3. [Collectors](/docs/subsystems/collectors.html)
4. [Pipes](/docs/subsystems/pipes.html)
5. [Dependencies](/docs/subsystems/dependencies.html)
6. [Anomaly Detection](/docs/subsystems/anomaly_detection.html)

