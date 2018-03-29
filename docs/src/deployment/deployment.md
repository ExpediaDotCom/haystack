---
layout: default
title: Deployment
---
# Deployment
Haystack uses terraform to automates deployment of Haystack components using [Kubernetes](https://en.wikipedia.org/wiki/Kubernetes) and [aws](https://aws.amazon.com/). You can use this module to setup haystack cluster on your local(Mac/Linux Machine) or on AWS.


## Local Cluster

To get a feel of haystack you can run haystack locally inside minikube. please refer to the getting started guide [here](https://expediadotcom.github.io/haystack/src/getting_started.html)


## AWS Cluster
We support out of the box deployment in aws for haystack. The script uses terraform to create a kubernetes cluster and the rest of the dependent-infrastructure for haystack in aws in a single zone at the moment.
Here are the list of components we install : 
1. Kubernetes cluster - version 1.8 (using kops)
2. Apache Cassandra
3. AWS Elastic Search
4. Apache Kafka
5. Kubernetes Addons (Traefik, Fluentd, ElasticSearch, Kibbana, Heapster, Influxdb, Graphite)
6. Haystack apps inside kubernetes clusters

Refer to the following [wiki](https://github.com/ExpediaDotCom/haystack/tree/master/deployment/docs/aws-deployment.md) for the details on deploying haystack on aws