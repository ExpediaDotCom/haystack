## AWS Deployment 
We support out of the box deployment in aws for haystack. The script uses terraform to create a kubernetes cluster and the rest of the dependent-infrastructure for haystack in aws in a single zone at the moment.
Here are the list of components we install : 
1. Kubernetes cluster - version 1.8 (using kops)
2. Apache Cassandra
3. AWS Elastic Search
4. Apache Kafka
5. Kubernetes Addons (Traefik, Fluentd, ElasticSearch, Kibana, Heapster, Influxdb, Graphite)
6. Haystack apps inside kubernetes clusters

#### Pre-requisite 
Haystack-AWS deployment requires you to create the following resources in aws and pass it as a parameter before you can trigger a deploy and pass it as an overrides.json file to the script. -
1. AWS VPC
2. AWS Subnet
3. Hosted Zone Id
4. S3 Bucket
5. IAM User


Make sure the machine where you are running this script has an [IAM USER](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html) configured as a system property and has Admin Privilages in the AWS account.

#### Start
From the root of the location to which haystack has been cloned:
```
cd deployment/terraform
./apply-compose.sh -r install-all -c aws
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
./apply-compose.sh -r install-all -c aws
```
This will apply only the delta on existing cluster, without affect the existing components.

#### Stop
To teardown your AWS cluster, from the root of the location to which haystack has been cloned run:
```
cd deployment/terraform
./apply-compose.sh -r uninstall-all -c aws
```
this will uninstall all haystack components, along with the infrastructure created in aws
