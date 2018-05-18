---
title: Getting Started
sidebar_label: Getting Started
---

All of Haystack's backend components are released as [Docker images](./deployment/sub_systems.html) on the ExpediaDotCom Docker Hub.
If you need to package the components yourself, fat jars are available from the Maven Central Repository.
Haystack is designed so that all of its components can be deployed selectively. 

We have automated deployment of Haystack components using [Kubernetes](https://github.com/jaegertracing/jaeger-kubernetes).
The entire Haystack server runs locally on Minikube (k8s), and with a 1 click deployment on other environments.
The deployment scripts are not tied up with Minikube for local development.
You can use the same script to deploy in production (and that is how we deploy at Expedia.)

## To install the local server

To get a feel of Haystack you can run Haystack locally inside Minikube.
To do so, clone the `ExpediaDotCom/haystack` repository and run the installer script, as described below.

### Install pre-requisites

1. Install [Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) on your box.
2. Start Minikube, optionally increasing its memory and/or CPUs if necessary:
```shell
minikube start --memory 4096 --cpus 2
```

### Install the software

From the root of the location to which `ExpediaDotCom/haystack` has been cloned:
```shell
cd deployment/terraform
./apply-compose.sh -r install-all
```
This will install required third party software, then start the Minikube and install all Haystack components in dev mode.

Run the following commands to add a valid local resolver record for your Minikube server:
```shell
grep -v 'haystack\.local' /etc/hosts | sudo tee /etc/hosts
echo "$(minikube ip) haystack.local" | sudo tee -a /etc/hosts
```
Once the record for the Minikube appears in `/etc/hosts`, you can access the Haystack ui at `http://haystack.local:32300`.

### Installed components list

The list of components that were installed can be seen in the Minikube dashboard, inside the `haystack-apps` namespace.
To open the Minikube dashboard type `minikube dashboard`.

### Uninstall the software
From the root of the location to which `ExpediatDotCom/haystack` has been cloned:
```shell
cd deployment/terraform
./apply-compose.sh -r uninstall-all
```

this will uninstall all Haystack components, but will leave Minikube running. To bring down Minikube:
```shell
minikube stop
``` 


### Troubleshooting deployment errors

If Minikube is returning errors during the install process it could be due to inconsistent terraform state. To fix this issue run the following commands in order of sequence.

1. Delete Deployment state
    ```shell
    ./apply-compose.sh -r delete-state
    ```
2. Recreate the Minikube VM
    ```shell
    minikube delete
    minikube start --memory 4096 --cpus 2
    ```
3. Retrigger Deployment
    ```shell
    ./apply-compose.sh -r install-all
    ```

## How to send spans

A *span* is one unit of telemetry data. A span typically represents a service call or a block of code.
A span for a service call starts from the time a client sends a request, ends at the time that the client receives a response, and includes metadata associated with the service call.

### Creating test data in kafka with fakespans

`fakespans` is a simple app written in the Go language, which can generate random spans and push them to the the Haystack messge bus, Kafka.
You can find the source for `fakespans` [in the haystack-idl repository](https://github.com/ExpediaDotCom/haystack-idl/tree/master/fakespans).

#### Using fakespans

Run the following commands on your terminal to start using fake spans. You will need to have the Go language installed in order to run `fake_spans`.

 ```shell
export $GOPATH=location where you want your go binaries (should end in /bin)
export $GOBIN=$GOPATH
go get github.com/Shopify/sarama
go get github.com/codeskyblue/go-uuid
go get github.com/golang/protobuf/proto
cd fakespans
go install
$GOPATH/fakespans
```
#### fakespans command line options
```
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

For details, see [ExpediaDotCom/haystack-idl](https://github.com/ExpediaDotCom/haystack-idl).

### How to view span data

You can see span data in the Haystack UI at `https://haystack.local:32300`.
See the [UI](https://expediadotcom.github.io/haystack/ui/ui.html) page for more information about how the data is presented and what you can do with the UI.
