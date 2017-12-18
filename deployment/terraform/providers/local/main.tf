locals {
  traefik_node_port = 32300
}

variable "haystack_domain_name" {
  default = "haystack.local"
}
module "haystack-deployments" {
  source = "../../modules/kubernetes"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
  enable_docker_infrastructure = true
  traefik_node_port = "${local.traefik_node_port}"
  haystack_domain_name = "${var.haystack_domain_name}"
}

