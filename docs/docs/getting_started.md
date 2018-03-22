---
layout: default
title: Getting Started
---

All of Haystack's backend components are released as [Docker images](https://expediadotcom.github.io/haystack/deployment/sub_systems.html) on the ExpediaDotCom Docker Hub.

We have automated deployment of Haystack components using [Kubernetes](github.com/jaegertracing/jaeger-kubernetes).
The entire Haystack server runs locally on minikube (k8s), and with a 1 click deployment on the rest of the environments.
The deployment scripts are not tied up with minikube for local development.
We can use the same script to deploy in production and that is what we use in Expedia

# To install the local server

To get a feel of haystack you can run haystack locally inside minikube.
To do so, clone this repository and run the script, as described below.

## Install pre-requisites

Before you install the Haystack Docker images, you must first install [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/). Make sure that Minikube is running, using `minikube start`.

## Install the software

From the root of the location to which haystack has been cloned:

```bash
cd deployment/terraform
./apply-compose.sh -a install
```

this will install required third party software, start the minikube and install all haystack components in dev mode.

Run the following command to verify that a CName record has been created for your minikube server:

```bash
 echo "$(minikube ip) haystack.local" | sudo tee -a /etc/hosts
```

Once the CName record for the minikube appears in `/etc/hosts`, you can access the haystack ui at `http://haystack.local:32300`.

## Installed components list

The list of components that were installed can be seen in the minikube dashboard, inside the `haystack-apps` namespace.
To open the minikube dashboard type `minikube start`.

# To deploy Haystack on AWS

We support out-of-the-box deployment in AWS for Haystack. The script uses Terraform to create a Kubernetes cluster and the rest of the dependent infrastructure for Haystack in AWS in a single zone.
For details about deploying on AWS, see the [haystack-deployment project](https://github.com/ExpediaDotCom/haystack/tree/master/deployment).

# How to send spans

A *span* is one unit of telemetry data. A span typically represents a service call or a block of code.
A span for a service call starts from the time a client sends a request, ends at the time that the client receives a response, and includes metadata associated with the service call.

## Creating test data in kafka

fakespans is a simple app written in the Go language, which can generate random spans and push them to the the Haystack messge bus, Kafka.
You can find the source for fakespans [in the haystack-idl repo](https://github.com/ExpediaDotCom/haystack-idl/tree/master/fakespans).

### Using fakespans

Run the following commands on your terminal to start using fake spans. You will need to have the Go language installed in order to run `fake_spans`.

 ```bash
export $GOPATH=location where you want your go binaries
export $GOBIN=$GOPATH/bin
cd fakespans
go install
$GOPATH/bin/fakespans
##fakespans options

./fakespans -h
Usage of fakespans:
  -interval int
        period in seconds between spans (default 1)
  -kafka-broker string
        kafka TCP address for Span-Proto messages. e.g. localhost:9092 (default "localhost:9092")
  -span-count int
        total number of unique spans you want to generate (default 120)
  -topic string
        Kafka Topic (default "spans")
  -trace-count int
        total number of unique traces you want to generate (default 20)
```

For details, click [here](https://github.com/ExpediaDotCom/haystack-idl).

### How to view span data

You can see span data in the Haystack UI at `https://haystack.local:32300`.
See the [UI](https://expediadotcom.github.io/haystack/ui/ui.html) page for details.