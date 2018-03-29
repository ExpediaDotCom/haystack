# Haystack Deployment
This is a deployment module which uses terraform to automates deployment of Haystack components using [Kubernetes](https://en.wikipedia.org/wiki/Kubernetes) and [aws](https://aws.amazon.com/). You can use this module to setup haystack cluster on your local(Mac/Linux Machine) or on AWS.

We currently support for deployment of haystack on a local sandbox and aws

## Local Cluster
To get a feel of haystack you can run haystack locally inside minikube. please refer to the getting started guide [here](https://expediadotcom.github.io/haystack/src/getting_started.html)


## AWS Cluster

We support out of the box deployment in aws for haystack. Refer to the following [wiki](./docs/aws-deployment.md) for the details on deploying haystack on aws