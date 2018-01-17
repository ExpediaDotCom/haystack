//when running locally we expect the machine to have a local k8s cluster using minikube
locals {
  container_log_path = "/mnt/sda1/var/lib/docker/containers"
}
module "k8s-addons" {
  source = "../../../modules/k8s-addons"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  traefik_node_port = "${var.reverse_proxy_port}"
  haystack_domain_name = "${var.haystack_domain_name}"
  add_logging_addons = true
  add_monitoring_addons = false
  container_log_path = "${local.container_log_path}"
  logging_es_nodes = "2"
}

module "haystack-infrastructure" {
  source = "../../../modules/haystack-datastores/kubernetes"
  k8s_app_name_space = "${module.k8s-addons.k8s_app_namespace}"
  k8s_cluster_name = "${var.k8s_minikube_cluster_name}"
}
