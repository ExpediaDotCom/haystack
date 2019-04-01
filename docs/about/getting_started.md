---
title: Getting Started
sidebar_label: Getting Started
---

All of Haystack's backend components are released as [Docker images](../subsystems/subsystems.html) on the ExpediaDotCom Docker Hub.

If you need to package the components yourself, fat jars are available from the Maven Central Repository.
Haystack is designed so that all of its components can be deployed selectively. 

We have automated deployment of Haystack components using [Kubernetes](https://github.com/kubernetes/kubernetes)
The entire Haystack server runs locally on Minikube (k8s), and with a 1 click deployment on other environments.
The deployment scripts are not tied up with Minikube for local development. One can use the same script to deploy in production.

Also, Minikube is not the only option to run Haystack server component locally. One can use docker-compose as well. 

## Starting Haystack components 

### Using docker-compose

Using docker-compose is the easiest option to get Haystack server running locally. 

Clone [ExpediaDotCom/haystack-docker](https://github.com/ExpediaDotCom/haystack-docker) repository and run docker-compose as described below

1. Allocate memory to docker

To run all of haystack and its components, it is suggested to change the default in docker settings from `2GiB` to `4GiB`. Please check this [Stackoverflow](https://stackoverflow.com/questions/44533319/how-to-assign-more-memory-to-docker-container) post on changing docker memory allocation.

2. To start Haystack's traces, trends and service graph

```shell
docker-compose -f docker-compose.yml \
               -f traces/docker-compose.yml \
               -f trends/docker-compose.yml \
               -f service-graph/docker-compose.yml \
               -f agent/docker-compose.yml up
```
The command above starts haystack-agent as well. Give a minute or two for the containers to come up and connect with each other. Haystack's UI will be available at http://localhost:8080

Finally, one can find a sample spring boot application @ https://github.com/ExpediaDotCom/opentracing-spring-haystack-example to send data to Haystack via haystack-agent listening in port 34000.

### MiniKube (Kubernetes) with Terraform Approach 

To get a feel of Haystack running in Kubernetes, one can run Haystack locally inside Minikube. To do so, clone the [ExpediaDotCom/haystack](https://github.com/ExpediaDotCom/haystack/) repository and run the installer script, as described below.

Note: Terraform to deploy Haystack into minikube described below can be used for deploying Haystack into large kubernetes clusters as well.

1. Install [Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) on your box.
2. Start Minikube, optionally increasing its memory and/or CPUs if necessary:
```shell
minikube start --memory 8192 --cpus 4
```

NOTE: if minikube has been previously started without these resource parameters, you may need to convince it to forget it's previous settings. 
You can use one of the following 2 methods:

* Method 1 : 
    1. Stop minikube
        ```shell
        minikube stop
        ``` 
    2. Manually change the memory and cpu settings in your Virtual Machine software,
    3. Restart minikube
        ```shell
        minikube start
        ```
* Method 2 :
    1. Delete minikube
        ```shell 
        minikube stop 
        minikube delete 
        ``` 
    2. Run the command to start minikube with desired configuration :
        ```shell
        minikube start --memory 8192 --cpus 4
        ```

#### Install the software

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

You can access the Kubernetes console at `http://haystack.local:30000`.

#### Installed components list

The list of components that were installed can be seen in the Minikube dashboard, inside the `haystack-apps` namespace.
To open the Minikube dashboard type `minikube dashboard`.

#### Custom Installations
By default deployment scripts are configured to deploy only the traces and trends components. However we can pass an overrides config file to the deployment script to specify the exact subsystems and their configurations you want to deploy
```shell
cd deployment/terraform
./apply-compose.sh -r install-all -o <path of your overrides file>
```

Here's a sample overrides file : 

```json
{
  "trends": {
    "enabled": false
  },
  "traces": {
    "enabled": true,
    "indexer_memory_request": "1024",
    "indexer_memory_limit" : "1024"

  },
  "service_graph": {
    "enabled": false
  }
} 
```

You can choose to override any of the values mentioned in the folowing [file](https://github.com/ExpediaDotCom/haystack/blob/master/deployment/terraform/cluster/local/apps/variables.tf) 

#### Uninstall the software
From the root of the location to which `ExpediatDotCom/haystack` has been cloned:
```shell
cd deployment/terraform
./apply-compose.sh -r uninstall-all
```

this will uninstall all Haystack components, but will leave Minikube running. To bring down Minikube:
```shell
minikube stop
``` 

#### Troubleshooting deployment errors

If Minikube is returning errors during the install process it could be due to inconsistent terraform state. To fix this issue run the following commands in order of sequence.

1. Delete Deployment state
    ```shell
    ./apply-compose.sh -r delete-state
    ```
2. Recreate the Minikube VM
    ```shell
    minikube delete
    minikube start --memory 8192 --cpus 4
    ```
3. Retrigger Deployment
    ```shell
    ./apply-compose.sh -r install-all
    ```

#### How to send spans

A *span* is one unit of telemetry data. A span typically represents a service call or a block of code.
A span for a service call starts from the time a client sends a request, ends at the time that the client receives a response, and includes metadata associated with the service call.

#### Creating test data in kafka with fakespans

`fakespans` is a simple app written in the Go language, which can generate random spans and push them to the the Haystack messge bus, Kafka.
You can find the source for `fakespans` [in the haystack-idl repository](https://github.com/ExpediaDotCom/haystack-idl/tree/master/fakespans).

#### Using fakespans

##### Using the prebuilt binaries
Choose the binary corresponding to Operating System and architecture from [here](https://github.com/ExpediaDotCom/haystack-idl/releases).
Alternatively, you can use the fakespans-downloader script to download fakespans binary, if your operating system is either Mac or Linux.


##### Building fakespans
Run the following commands on your terminal to start using fake spans. You will need to have the Go language installed in order to run `fakespans`.

 ```shell
export GOPATH= $(go env GOPATH)
export GOBIN=  your GOPATH + `/bin` (location where you want your go binaries)

cd fakespans
go get github.com/Shopify/sarama
go get github.com/codeskyblue/go-uuid
go get github.com/golang/protobuf/proto
go install
cd $GOBIN
./fakespans
```
##### fakespans command line options
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

#### How to view span data

You can see span data in the Haystack UI at `https://haystack.local:32300`.
See the [UI](https://expediadotcom.github.io/haystack/docs/ui/ui.html) page for more information about how the data is presented and what you can do with the UI.

## Searchable Keys

### Why do you need this?
By default, haystack allows you to search for traces with a traceId, serviceName and operationName. To make search more convenient, haystack  auto suggests the observed values only for serviceName and all its operationNames. Since UI web app cache this data (5 minutes TTL) to reduce the load on backend systems, hence you may see a delay in the auto suggestion for a new serviceName. 

These default search keys are good for basic usecase, but definitely you may want to search or filter traces(or spans) with custom keys that are part of a span tag. Here are few example scenarios:

```
a) Filter all the error spans from a booking service 'B' in last 1 hour
b) Look for all failed transactions for a given userId in last 24 hours
c) Search for spans with 4xx http response from Content serving app 'C' in last 15 min  
```

Haystack does not index every span tag key primarily to avoid producers(here micro services) from flooding indexing subsystem accidentally. The producers can still send any key-value pair as a tag that is visibile in the span data but it is not searchable. The team that manages haystack infrastructure can add or remove the searchable keys depending upon their business usecases.

### How to whitelist a key for search?

Haystack uses ElasticSearch(ES) for twin purpose. It stores the whitelisted keys that needs to be indexed(searched) and then index the stream of spans for those searchable keys in ElasticSearch.

Regarding the whitelist field configuration, haystack-indexer reloads it every few min and you can control the refresh frequency [here](https://github.com/ExpediaDotCom/haystack-traces/blob/master/deployment/terraform/trace-indexer/templates/trace-indexer.conf#L136). The haystack-reader provides the list of searchable keys through a grpc endpoint(`getFieldNames`) that is consumed by UI application. Please note that search keys are normalized to lowercase at the time of indexing and searching from ElasticSearch.  

In order to whitelist a tag key, you require to update a single document(docId 1) in the ElasticSearch `index/type` called `reload-configs/indexing-fields`. We provide a sample kubernetes job spec [here](https://github.com/ExpediaDotCom/haystack-traces/blob/master/deployment/terraform/es-indices/whitelisted-fields/templates/whitelisted-fields-pod-yaml.tpl) that you can run as a cron or on-demand whenever a new key needs to be whitelisted or blacklisted(if enabled before). If you are not using kubernetes, you can still refer the job that shows how the whitelisted keys are structured as a json in ElasticSearch. Please note that `searchContext` attribute in the field [here](https://github.com/ExpediaDotCom/haystack-traces/blob/master/deployment/terraform/es-indices/whitelisted-fields/templates/whitelisted-fields-pod-yaml.tpl#L13) has been deprecated and ignored to prefer the [nested search](https://expediadotcom.github.io/haystack/docs/ui/ui_universal_search.html#complex-workflow) functionality on universal search bar.

