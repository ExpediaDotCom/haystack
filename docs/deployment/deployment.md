---
title: Deployment
sidebar_label: Deployment
---

Haystack uses [Terraform](https://www.terraform.io/intro/) to automate deployment of Haystack components using [Kubernetes](https://kubernetes.io/) and [Amazon Web Services (AWS)](https://aws.amazon.com/). You can use this module to set up a Haystack cluster on your local Mac or Linux machine, or on AWS.

Deployment tools and scripts can be found in the `ExpediaDotCom/haystack` repository on GitHub.

## Versioning
Haystack follows Semantic Versioning (MAJOR.MINOR.PATCH). All the modules are compatible with each other as long as the MAJOR version is same.

## Local Cluster

To get a feel for Haystack you can run it locally inside minikube. Please refer to the [Getting Started guide](https://expediadotcom.github.io/haystack/docs/about/getting_started.html) for installation instructions.

## AWS Cluster

We support out of the box deployment in AWS for Haystack. The current version of the apply_compose script uses Terraform to create a Kubernetes cluster and the rest of the dependent infrastructure for Haystack in AWS in a single zone.
We install the following components: 
* Kubernetes cluster - version 1.8 (using kops)
* Apache Cassandra
* AWS Elastic Search
* Apache Kafka
* Kubernetes Addons (Traefik, Fluentd, ElasticSearch, Kibana, Heapster, Influxdb, Graphite)
* Haystack apps inside Kubernetes clusters

### Pre-requisite 
Haystack deployment on AWS requires that you create the following resources in AWS and pass information about them in an overrides.json file passed as a parameter on the deployment command line.
1. AWS VPC
2. AWS Subnet
3. Hosted Zone Id
4. S3 Bucket
5. IAM User

Make sure the machine where you are running this script has an [IAM USER](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html) configured as a system property and has Admin privileges in the AWS account.

### Building AMI 
Builder script for creating Cassandra and Kafka golden image using Packer. There is still instance specific configuration that must be applied before Cassandra and Kafka nodes can be started. 

```shell
cd deployment/terraform/packer/
```

Update required configurations in variable.json and use the below command to create the AMI. Script installs Packer if its not available already. It will output AMI id once done successfully, please save that for later use.

```shell
./build-image.sh
```

### Start deployment
From the root of the location to which `ExpediaDotCom/haystack` has been cloned:
```shell
cd deployment/terraform
./apply-compose.sh -r install-all -c aws
```
This installs required third party software, and then Terraform opens up an interactive console to suggest the components it's going to create in AWS.

### Verify deployment was successful
We create an AWS Route53 entry with your `<haystack-cluster-name>.<hosted_domain_name>`. You can access the Haystack UI at:
```
 http://<haystack-cluster-name>.<hosted_domain_name>
```

To check the health of the Kubernetes cluster, you can use Grafana at:
```
 http://<haystack-cluster-name>-metrics.<hosted_domain_name>
```

To check the logs of the Kubernetes cluster you can use Kibana at:
```
 http://<haystack-cluster-name>-logs.<hosted_domain_name>
```


To see the apps running in the kubernetes cluster you can use the kubernetes dashboard at:
```
 http://<haystack-cluster-name>-k8s.<hosted_domain_name>
```

To see components deployed in Kuberenetes:
```shell
kubectl config use-context haystack-k8s.<hosted_domain_name>
kubectl get pods --namespace=haystack-apps
```

### Updating Haystack components
Our Terraform deployments are declarative. Thus, updating (deploying a new version of any Haystack app or increasing the number of instances in a Kubernetes cluster) uses the same command as create. After making changes in your Terraform configs for updating the cluster, run: 
```shell
cd deployment/terraform
./apply-compose.sh -r install-all -c aws
```
This will apply only the delta to the existing cluster, without affecting the existing components.

### Stop Haystack
To teardown your AWS cluster, from the root of the location to which `ExpediaDotCom/haystack` has been cloned, run:
```shell
cd deployment/terraform
./apply-compose.sh -r uninstall-all -c aws
```
This will uninstall all Haystack components, along with the infrastructure created in AWS when you installed Haystack.
