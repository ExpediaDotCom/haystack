# Getting Started

### Where are the components?

All of Haystack's backend components are released as Docker images on **ExpediaDotCom Docker Hub**:

Component | Repository 
----------- | ---- |
haystack-kinesis-span-collector | https://hub.docker.com/u/expediadotcom/haystack-kinesis-span-collector 
haystack-ui | https://hub.docker.com/u/expediadotcom/haystack-ui 
haystack-agent | https://hub.docker.com/u/expediadotcom/haystack-agent 
haystack-trace-indexer | https://hub.docker.com/u/expediadotcom/haystack-trace-indexer 
haystack-trace-reader | https://hub.docker.com/u/expediadotcom/haystack-trace-reader 
haystack-trace-provider | https://hub.docker.com/u/expediadotcom/haystack-trace-provider
haystack-timeseries-aggregator | https://hub.docker.com/u/expediadotcom/haystack-timeseries-aggregator 
haystack-span-timeseries-transformer | https://hub.docker.com/u/expediadotcom/haystack-span-timeseries-transformer 
haystack-pipes-kafka-producer | https://hub.docker.com/u/expediadotcom/haystack-pipes-kafka-producer
haystack-pipes-json-transformer | https://hub.docker.com/u/expediadotcom/haystack-pipes-json-transformer 


### How to run these components?

Entire haystack runs locally on minikube(k8s), after 1 click deployment and Kubernetes on the rest of the environments. We have automated deployment of Haystack components using [Kubernetes](github.com/jaegertracing/jaeger-kubernetes). 

#### Installation

Clone this repository and run the script, as documented in the next section.

#### Usage

From the root of the location to which haystack has been cloned:

```
cd deployment/k8s./apply-compose.sh -a install
```
will install required third party software, start the minikube and install all haystack components in dev mode.


#### What components get installed ?

The list of components that get installed in dev mode can be found at k8s/compose/dev.yaml. 'dev' is a logic name of an environment, one can create compose files for different environments namely staging, test, prod etc. The script understands the environment name with '-e' option. 'dev' is used as default.


#### How to deploy haystack on AWS?

This script does not create/delete the kubernetes cluster whether local(minikube) or cloud. We recommend to use open source tools like kopsto manage your cluster on AWS. Once you have your cluster up and running, configure the 'kubectl' to point to your cluster. 

Please note the default context for all environments will be minikube. In other words, --use-context will always point to minikube. This is done intentionally to safeguard developers from pushing their local dev changes to other environments.

For details, go [here](https://github.com/ExpediaDotCom/haystack/tree/master/deployment)

### How to send spans?

Span is the unit of telemetry data. A span typically represents a service call or a block of code. Lets look at the former for now, it starts from the time client sent a request to the time client received a response along with metadata associated with the service call.

#### Creating test data in kafka

fakespans is a simple go app which can generate random spans and push to kafka

#### Using fakespans

Run the following commands on your terminal to start using fake spans you should have golang installed on your box
```
export $GOPATH=location where you want your go binaries
export $GOBIN=$GOPATH/bin
cd fakespans
go install
$GOPATH/bin/fakespans
##fakespans options

./fake_metrics -h
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

For details, click [here](https://github.com/ExpediaDotCom/haystack-idl)

### How to see on UI?

UI component can be mounted on Traefik with Host rules, where you need to provide a different CNAME for each UI component. However you can avoid Traefik completely and deploy UI components as a service in Kubernetes with 'LoadBalancer' or 'NodePort' type

#### How to access traefik dashboard and grafana locally

```
echo "$(minikube ip) haystack.local" | sudo tee -a /etc/hosts
```

Once you have cname record to minikube, access traefik dashboard at

 ```
 https://haystack.local:32300
 ```
 
and grafana at

```
https://haystack.local:32300/grafana
```
