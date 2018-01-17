# Haystack Deployment
This is a deployment module which uses terraform to automates deployment of Haystack components using [Kubernetes](https://en.wikipedia.org/wiki/Kubernetes) and [aws](https://aws.amazon.com/). You can use this module to setup haystack cluster on your local(Mac/Linux Machine) or on AWS.


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

The script sets the 'haystack.local' domain point to your local minikube ip. Use the below link access to haystack UI -
```
http://haystack.local:32300
```

Run the following command to see all the kubernetes pods which are deployed -
```
kubectl get pods --namespace=haystack-apps
```

#### Stop
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



## AWS Cluster
We support out of the box deployment in aws for haystack. The script uses terraform to create a kubernetes cluster and the rest of the dependent-infrastructure for haystack in aws.
Here are the list of components we install : 
1. Kubernetes cluster - version 1.8 (using kops)
2. Apache Cassandra
3. AWS Elastic Search
4. Apache Kafka
5. Kubernetes Addons (Traefik, Fluentd, ElasticSearch, Kibbana, Heapster, Influxdb, Graphite)
6. Haystack apps inside kubernetes clusters

#### Pre-requisite 
Haystack-AWS deployment requires you to create the following resources in aws and pass it as a parameter before you can trigger a deploy -
1. AWS VPC
2. AWS Subnet
3. Hosted Zone Id
4. S3 Bucket
5. IAM User
Make sure the machine where you are running this script has sufficient permissions in AWS to create/manage above mentioned resources.

#### Start
From the root of the location to which haystack has been cloned:
```
cd deployment/terraform
./apply-compose.sh -a install - c aws
```
We install required third party software, and terraform opens up an interactive console to suggest the components its going to create in AWS

#### Verify
We create an AWS Route53 entry with your haystack-k8s.<hosted_domain_name>. You can access haystack at -
```
 http://haystack-k8s.<hosted_domain_name>
```

To check the health of the k8s cluster you can use grafana at -
```
 haystack-k8s.<hosted_domain_name>/grafana
```

To see components deployed in kuberenets -
```
kubectl config use-context haystack-k8s.<hosted_domain_name>
kubectl get pods --namespace=haystack-apps
```

#### Update
Our terraform deployments are declarative. Thus, updation(eg. to deploy a new version of any haystack-app or increase number of instances in kubernetes cluster), uses the same command as create. After making changes in your terraform configs for updating cluster, run: 
```
cd deployment/terraform
./apply-compose.sh -a install - c aws
```
This will apply only the delta on existing cluster, without affect the existing components.

#### Stop
To teardown your AWS cluster, from the root of the location to which haystack has been cloned run:
```
cd deployment/terraform
./apply-compose.sh -a -c aws uninstall
```
this will uninstall all haystack components, along with the infrastructure created in aws
