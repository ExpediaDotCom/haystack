provider "null" {}
provider "template" {}
provider "kubernetes" {
  config_context = "${var.k8s_minikube_cluster_name}"
}