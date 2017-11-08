<img src="images/logo.png" style="width: 200px;"/>

## [Haystack Deployment](https://github.com/ExpediaInc/haystack-deployment)

Automates deployment of Haystack components using [Kubernetes](https://en.wikipedia.org/wiki/Kubernetes)
### Installation
Clone [this](https://github.com/ExpediaInc/haystack-deployment) repository and run the script, as documented in the next section.
### Usage
From the root of the location to which haystack-deployment has been cloned:
```
./k8s/apply-compose.sh -a install
```
will install required third party software, start the minikube and install all haystack components in dev mode.

### What components get installed ?
The list of components that get installed in dev mode can be found at k8s/compose/dev.yaml. 'dev' is a logic name of an environment,
one can create compose files for different environments namely staging, test, prod etc. The script understands the environment name
with '-e' option. 'dev' is used as default.


### Haystack Namespace
The apply-compose script deploys all haystack components under different namespace. We use following convention to name this 'namespace'
```
haystack-<environment name>
```

### How to deploy haystack on AWS?
This script does not create/delete the kubernetes cluster whether local(minikube) or cloud. We recommend to use open source tools like [kops](https://github.com/kubernetes/kops)to manage your cluster on AWS. Once you have your cluster up and running, configure the 'kubectl' to point to your cluster.
If using kops, the command:
```
kops export kubecfg <name of your cluster> --state s3://<state_store_bucket_name>   
```
will update the kubectl config.json with your cluster context, after that the command:
```
kubectl config get-contexts
```
list all the available contexts. Choose your cluster context, and deploy the haystack with:
```
./k8s/apply-compose.sh -a install -e test --use-context <context-name>
```
Please note the default context for all environments will be minikube. This is done intentionally to safeguard developers
from pushing their local dev changes to other environments.

### Addons
By default, we install addons for [monitoring](https://github.com/kubernetes/heapster), [logging](https://github.com/kubernetes/kubernetes/tree/master/cluster/addons/fluentd-elasticsearch) and reverse proxy called [Traefik](https://github.com/containous/traefik)
The reverse proxy helps us to bind external loadbalancer (ELB on AWS) to just one nodePort assigned to Traefik. All other components like haystack-ui, grafana and kibana can be mounted within Traefik.

Please note that a UI component can be mounted on Traefik with [Host](https://docs.traefik.io/basics/) rules, where you need to provide a different CNAME for each UI component.
However you can avoid Traefik completely and deploy UI components as a service in Kubernetes with 'LoadBalancer' or 'NodePort' type

#### How to access traefik dashboard and grafana locally
```
 echo "$(minikube ip) haystack.local" | sudo tee -a /etc/hosts
```
Once you have cname record to minikube, access traefik dashboard at
```
 http://haystack.local:32300
```
and grafana at
```
 http://haystack.local:32300/grafana
```

### App Configs
Every haystack service/component will be bundled with some default configurations to run on kubernetes. However we can provide 'overrides' depending upon environment it runs in.
We can provide these overrides as configuration files for instance 'configs/test/span-stitcher.yaml' and provide the settings in 'compose' file.
Then we mount them inside the container under say /configs/span-stitcher.yaml using Kubernetes' [ConfigMaps](https://kubernetes.io/docs/tasks/configure-pod-container/configure-pod-configmap)

### Scheduled Jobs
We run scheduled jobs to remove the old indices created on ElasticSearch for logs collected by Fluentd. Howeever, we don not setup one in dev envivonment.

### Verify haystack components
Once scripts gets completed,  
```
kubectl get deployments --namespace=haystack-<env>
```
will show the components that are running. '--namespace' is not required if you are deploying on minikube. We create a new context
minikube-haystack-<env> configured with haystack-<env> namespace. This may help developers to avoid typing namespace with each
kubectl command.


One of the haystack components is Kafka, and you can interact with it in the usual
ways. For example, if you have [kafkacat](https://github.com/edenhill/kafkacat) installed, you can start a command line
producer with
```
kafkacat -P -b $(minikube ip):9092 -t test
```
From that command line producer, type some text, followed by the "Return" key.
Then, in another terminal, start a command line consumer:
```
kafkacat -C -b $(minikube ip):9092 -t test
```
to see the text you gave to the producer.

To uninstall all haystack components:
```
./k8s/apply-compose.sh -a uninstall
```
To learn more about `apply-compose.sh`, type :
```
./k8s/apply-compose.sh --help
```
and by looking at the `apply-compose.sh` [source](k8s/apply-compose.sh).
