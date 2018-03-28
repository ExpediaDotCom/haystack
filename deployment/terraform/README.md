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

```
 echo "$(minikube ip) haystack.local" | sudo tee -a /etc/hosts
```
Once you have cname record to minikube, access traefik dashboard at
```
 https://haystack.local:32300
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
during the install process. If you choose to take down minikube, you should delete the tfstate files by running, from 
the directory containing apply-compose.sh, the following command:
```
find . -name "*tfstate*" -exec rm {} \;
```
This command removes the files that Terraform uses to [keep track of state.](https://www.terraform.io/docs/state/)
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
We create an AWS Route53 entry with your <haystack-cluster-name>.<hosted_domain_name>. You can access haystack at -
```
 http://<haystack-cluster-name>.<hosted_domain_name>
```

To check the health of the k8s cluster you can use grafana at -
```
 http://<haystack-cluster-name>-metrics.<hosted_domain_name>
```

To check the logs of the k8s cluster you can use kibana at -
```
 http://<haystack-cluster-name>-logs.<hosted_domain_name>
```


To see the apps running in the kubernetes cluster you can use the kubernetes dashboard at -
```
 http://<haystack-cluster-name>-k8s.<hosted_domain_name>
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
