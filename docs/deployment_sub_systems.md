---
title: Subsystems Deployment
sidebar_label: Subsystems
---

AAll the Haystack components are released as Docker images on the Docker Hub:-
 
 Component | Repository 
 ----------- | ---- |
 haystack-ui | https://hub.docker.com/u/expediadotcom/haystack-ui 
 haystack-agent | https://hub.docker.com/u/expediadotcom/haystack-agent 
 haystack-trace-indexer | https://hub.docker.com/u/expediadotcom/haystack-trace-indexer 
 haystack-trace-reader | https://hub.docker.com/u/expediadotcom/haystack-trace-reader 
 haystack-timeseries-aggregator | https://hub.docker.com/u/expediadotcom/haystack-timeseries-aggregator 
 haystack-kinesis-span-collector | https://hub.docker.com/u/expediadotcom/haystack-kinesis-span-collector 
 haystack-span-timeseries-transformer | https://hub.docker.com/u/expediadotcom/haystack-span-timeseries-transformer 
 haystack-pipes-http-poster | https://hub.docker.com/u/expediadotcom/haystack-pipes-http-poster
 haystack-pipes-kafka-producer | https://hub.docker.com/u/expediadotcom/haystack-pipes-kafka-producer
 haystack-pipes-json-transformer | https://hub.docker.com/u/expediadotcom/haystack-pipes-json-transformer 
 
 
 ## Installation
 
 Clone the [ExpediaDotCom/haystack](https://github.com/ExpediaInc/haystack) repository and run the `apply-compose.sh` script, as documented in the next section.
 
 ### Usage
 From the root of the location to which haystack has been cloned:
 ```shell
 ./deployment/terraform/apply-compose.sh -a install
 ```
 will install required third party software, start the minikube and install all haystack components in dev mode.
 
 ### Installed components
 The list of components that get installed in dev mode can be found in `k8s/compose/dev.yaml`. 'dev' is a logic name of an environment;
 you can create addition files in the `compose` subdirectory for different environments such as staging, test, or production. Specify the environment to the install script with the '-e' option. The script uses `dev` as the default environment if none is specified.
 
 ### Haystack Namespace
 The `apply-compose.sh` script deploys the Haystack components for a given environment under a dedicated namespace. Namespaces are named using the pattern, `haystack-<environment name>`.
 
 ### Deploying on AWS
 This script does not create or delete the Kubernetes cluster, whether local (using Minikube) or on AWS. We recommend that you use open source tools like [kops](https://github.com/kubernetes/kops) to manage your cluster on AWS. Once you have your cluster up and running, configure the 'kubectl' to point to your cluster.
 
 If using kops, the command:
 ```
 kops export kubecfg <name of your cluster> --state s3://<state_store_bucket_name>   
 ```
 will update the kubectl config.json with your cluster context. After that, the command:
 ```
 kubectl config get-contexts
 ```
 list all the available contexts. Choose your cluster context, and deploy Haystack with:
 ```
 ./deployment/terraform/apply-compose.sh -a install -e test --use-context <context-name>
 ```
 Please note the default context for all environments will be minikube. This is done intentionally to safeguard developers
 from pushing their local dev changes to other environments.
 
 ### Addons
 By default, we install addons for monitoring with ([heapster](https://github.com/kubernetes/heapster)), logging with the ([Kubernetes ElasticSearch add-on](https://github.com/kubernetes/kubernetes/tree/master/cluster/addons/fluentd-elasticsearch)), and a reverse proxy called [Traefik](https://github.com/containous/traefik).
 The reverse proxy helps us to bind an external load balancer (ELB on AWS) to just one nodePort assigned to Traefik. All other components like haystack-ui, Grafana and Kibana can be mounted within Traefik.
 
 Please note that a UI component can be mounted on Traefik with [Host](https://docs.traefik.io/basics/) rules, where you need to provide a different CNAME for each UI component.
 However you can avoid Traefik completely and deploy UI components as a service in Kubernetes with 'LoadBalancer' or 'NodePort' type
 
 #### How to access Grafana and the Traefik dashboard locally
 Use the following command to discover the CNAME record for the Minikube:
 ```
  echo "$(minikube ip) haystack.local" | sudo tee -a /etc/hosts
 ```
 Use the hostname assigned to the Minikube by that CNAME record to access the Traefik dashboard at:
 ```
  http://haystack.local:32300
 ```
 and Grafana at:
 ```
  http://haystack.local:32300/grafana
 ```
 
 ### App Configs
 Every Haystack service or component will be bundled with some default configurations to run on Kubernetes. 
 You can override the default configurations as follows:
 
 You provide overrides specific to a given environment, by changing the configuration file for the appropriate environment.
 You can also add components for a given environment by editing the files in the `k8s/compose/` directory.
 You then mount them inside the container using Kubernetes' [ConfigMaps](https://kubernetes.io/docs/tasks/configure-pod-container/configure-pod-configmap).
 
 ### Scheduled Jobs
 We run scheduled jobs to remove the old indices created on ElasticSearch for logs collected by Fluentd. 
 However, in order to preserve possible debugging data, we do not set up an index removal job in the dev environment.
 
 ### Verify haystack components
 Once scripts gets completed,  
 ```shell
 kubectl get deployments --namespace=haystack-<env>
 ```
 will show the components that are running. '--namespace' is not required if you are deploying on minikube. We create a new context
 minikube-haystack-<env> configured with haystack-<env> namespace. This may help developers to avoid typing namespace with each
 kubectl command.
 
 One of the Haystack components is Kafka, and you can interact with it in the usual
 ways. For example, if you have [kafkacat](https://github.com/edenhill/kafkacat) installed, you can start a command line
 producer with
 ```shell
 kafkacat -P -b $(minikube ip):9092 -t test
 ```
 From that command line producer, type some text, followed by the "Return" key.
 Then, in another terminal, start a command line consumer:
 ```shell
 kafkacat -C -b $(minikube ip):9092 -t test
 ```
 to see the text you gave to the producer.
 
 To uninstall all haystack components:
 ```shell
 ./deployment/terraform/apply-compose.sh -a uninstall
 ```
 To learn more about `apply-compose.sh`, type :
 ``` shell
 ./deployment/terraform/apply-compose.sh --help
 ```
 and by looking at the `apply-compose.sh` [source](https://github.com/ExpediaDotCom/haystack/blob/master/deployment/terraform/apply-compose.sh).
