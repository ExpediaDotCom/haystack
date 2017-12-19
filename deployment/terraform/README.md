# Haystack Deployment
This is a deployment module with uses terraform to automates deployment of Haystack components using [Kubernetes](https://en.wikipedia.org/wiki/Kubernetes) and [aws](https://aws.amazon.com/)


## How to start haystack on your dev machine?
To get a feel of haystack you can start haystack locally inside minikube

###Pre-requisite 
1. install [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) on your box

###Start
From the root of the location to which haystack has been cloned:
```
cd deployment/terraform
./apply-compose.sh -a install
```
will install required third party software, start the minikube and install all haystack components in dev mode.
###Verify

#### How to access haystack-ui and grafana
The script sets the 'haystack.local' domain to your minikube ip.
use the below link access haystack at
```
 https://haystack.local:32300
```
to check the health of the k8s cluster you can use the grafana link shared below
```
 https://haystack.local:32300/grafana
```

#### Verify haystack components
Once scripts gets completed run the following command to see all the apps which are deployed
```
kubectl get deployments --namespace=haystack-apps
```

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

## How to deploy haystack on AWS?
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

#### How to access haystack-ui and grafana
We create an AWS Route53 entry with your haystack-k8s.<hosted_domain_name>
use the below link access haystack at
```
 http://haystack-k8s.<hosted_domain_name>
```
to check the health of the k8s cluster you can use the grafana link shared below
```
 haystack-k8s.<hosted_domain_name>/grafana
```

#### Verify haystack components
Once scripts gets completed run the following command to see all the apps which are deployed
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
this will uninstall all haystack components, along with the infrastructure created in aws
