//when running locally we expect the machine to have a local k8s cluster using minikube
locals {
  app-node_selecter_label = "kubernetes.io/hostname: minikube"
  default_cpu_limit = "100m"
  memory_limit_in_mb = "768"
  jvm_memory_limit = "512"
  k8s_datastores_heap_memory_in_mb = "1024"
  k8s_datastores_memory_limit_in_mb = "1224"
}
module "k8s-addons" {
  source = "../../../modules/k8s-addons"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  datastores_heap_memory_in_mb = "${local.k8s_datastores_heap_memory_in_mb}"
  aa_apps_resource_limits = "${var.aa_apps_resource_limits}"
  monitoring_addons = "${var.monitoring_addons}"
  alerting_addons = "${var.alerting_addons}"
  logging_addons = "${var.logging_addons}"
  traefik_addon = "${var.traefik_addon}"
  cluster = "${var.cluster}"

}
module "haystack-infrastructure" {
  source = "../../../modules/haystack-datastores/kubernetes"
  k8s_app_name_space = "${module.k8s-addons.k8s_app_namespace}"
  k8s_cluster_name = "${var.kubectl_context_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  node_selecter_label = "${local.app-node_selecter_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  cpu_limit = "${local.default_cpu_limit}"
  memory_limit = "${local.k8s_datastores_memory_limit_in_mb}"
  jvm_memory_limit = "${local.jvm_memory_limit}"
  docker_host_ip = "${var.docker_host_ip}"
}
