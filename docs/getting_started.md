# Getting Started

### Where are the components?

All of Haystack's backend components are released as [Docker images](https://expediadotcom.github.io/haystack/deployment/sub_systems.html) on **ExpediaDotCom Docker Hub**

### How to run these components?

We have automated deployment of Haystack components using [Kubernetes](github.com/jaegertracing/jaeger-kubernetes). Entire haystack runs locally on minikube(k8s), with a 1 click deployment on the rest of the environments. Deployment scripts are not tied up with minikube(local development),we can use the same script to deploy in production and that is what we use in Expedia

#### Installation

Clone this repository and run the script, as documented in the next section.

## Local Cluster
To get a feel of haystack you can run haystack locally inside minikube.

#### Pre-requisite 
Install [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) on your box and make sure it is running `minikube start`.

#### Start
From the root of the location to which haystack has been cloned:
```
cd deployment/terraform
./apply-compose.sh -a install
```
this will install required third party software, start the minikube and install all haystack components in dev mode.

#### Verify
```
 echo "$(minikube ip) haystack.local" | sudo tee -a /etc/hosts
```
Once you have cname record to minikube, access the haystack ui at 
```
 http://haystack.local:32300
 ```
 
#### What components get installed ?

The list of components that get installed can be seen at the minikube dashboard inside the haystack-apps namespace 
To open minikube dashboard type `minikube start` 


#### How to deploy haystack on AWS?

We support out of the box deployment in aws for haystack. The script uses terraform to create a kubernetes cluster and the rest of the dependent-infrastructure for haystack in aws in a single zone at the moment.
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

Once you have cname record to minikube, access haystack UI at-

 ```
 https://haystack.local:32300
 ```
