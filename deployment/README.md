# haystack-deployment

We use terraform to create infrastructure, we currently support creation of infrastructure in [aws](terraform/cluster/aws/main.tf) and [local](terraform/cluster/local/main.tf). 

AWS infrastructure creation creates aws resources to spin up components which haystack requires, whereas local deployment deploys the ifrastructure as docker containers inside a minikube kubernetes cluster which needs to be installed.

Haystack infrastructure consists of the following four components 
1. Creation of a kubernetes cluster (only for aws, when run in local mode it expects k8s to be deployed)
2. Deploying kubernetes addons for reverse proxying, logging and monitoring of the haystack apps
3. creation of open source softwares which haystack depends on
    1. cassandra 
    2. kafka
    3. elasticsearch
    4. metrictank
     
4. Deployment of the haystack apps in the kubernetes cluster under the haystack-apps namespace


## Local Deployment

To get a feel of haystack you can start haystack locally inside minikube 


###Pre-requisite 
1. install [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) on your box

###Start
From the root of the location to which haystack has been cloned:
```
cd deployment/terraform
./apply-compose.sh -a install
```
This will install required third party software, start the minikube and install all haystack components in dev mode.
###### Note : not all logging and monitoring addons are not deployed when run locally due to resource constraints in the local minikube cluster.

###Verify

#### How to access haystack-ui
The script sets the 'haystack.local' domain to your minikube ip.
use the below link access haystack at
```
 https://haystack.local:32300
```

Once scripts gets completed run the following command to see all the apps which are deployed
```
minikube dashboard
```

This should open up the kubernetes dashboard and you can find your apps under the haystack-apps namespace.

###Stop

From the root of the location to which haystack has been cloned:
```
cd deployment/terraform
./apply-compose.sh -a uninstall
```
this will uninstall all haystack components, but will leave minikube running. To bring down minikube:

```
minikube stop
``` 

Taking down minikube before running `./apply-compose.sh -a install` may prove helpful if minikube is returning errors
during the install process.


## AWS Deployment

We support out of the box deployment in aws for haystack. The script uses teraform to create a kubernetes cluster and the rest of the dependent-infrastructure for haystack in aws

Here are the list of components we install : 

1. Kubernetes cluster - version 1.8 (using kops)
2. Cassandra
3. AWS Elastic Search
4. Confluent-Platform Opensource
5. Kubernetes Addons
6. Haystack-Apps inside kubernetes


###Pre-requisite 
Haystack-AWS deployment requires you to create the following resources in aws and pass it as a parameter before you can trigger a deploy
1. AWS VPC
2. AWS Subnet
3. Hosted Zone Id
4. S3 Bucket
5. IAM User

###Start
From the root of the location to which haystack has been cloned:
```
cd deployment/terraform
./apply-compose.sh -a install - c aws
```
We install required third party software, and terraform opens up an interactive console to suggest the components its going to create in AWS

###Verify

#### How to access haystack-ui

We create an AWS Route53 entry with your haystack-k8s.<hosted_domain_name> use the below link access haystack at

```
 http://haystack-k8s.<hosted_domain_name>
```



#### Logging and monitoring 

to check the health of the k8s cluster all the haystack app logs and metrics can be seen at 

```
 haystack-k8s.<hosted_domain_name>/logs
```

```
 haystack-k8s.<hosted_domain_name>/metrics
```


#### See running components

Once scripts gets completed run the following command to see all the apps which are deployed from the machine running the script
```
kubectl config use-context haystack-k8s.<hosted_domain_name>
kubectl get deployments --namespace=haystack-apps
```

###Stop

From the root of the location to which haystack has been cloned:
```
cd deployment/terraform
./apply-compose.sh -a -c aws uninstall
```
This will uninstall all haystack components, along with the infrastructure created in aws
