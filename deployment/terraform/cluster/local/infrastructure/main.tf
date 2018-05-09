//when running locally we expect the machine to have a local k8s cluster using minikube
locals {
  container_log_path = "/mnt/sda1/var/lib/docker/containers"
  haystack_ui_cname = "${var.haystack_cluster_name}.${var.haystack_domain_name}"
  metrics_cname = "${var.haystack_cluster_name}-metrics.${var.haystack_domain_name}"
  logs_cname = "${var.haystack_cluster_name}-logs.${var.haystack_domain_name}"
  k8s_dashboard_cname = "${var.haystack_cluster_name}-k8s.${var.haystack_domain_name}"
  monitoring-node_selecter_label = "kubernetes.io/hostname: minikube"
  app-node_selecter_label = "kubernetes.io/hostname: minikube"
  default_cpu_limit = "100m"
  memory_limit_in_mb = "250"
  k8s_datastores_heap_memory_in_mb = "512"
}
module "k8s-addons" {
  source = "../../../modules/k8s-addons"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  base_domain_name = "${var.haystack_domain_name}"
  traefik_node_port = "${var.reverse_proxy_port}"
  graphite_node_port = "${var.graphite_node_port}"

  add_logging_addons = "${var.logging_addons_enabled}"
  add_monitoring_addons = "${var.monitoring_addons_enabled}"

  add_kubewatch_addon = false
  kubewatch_config_yaml_base64 = ""

  add_k8s_dashboard_addons = false
  container_log_path = "${local.container_log_path}"
  logging_es_nodes = "1"
  metrics_cname = "${local.metrics_cname}"
  haystack_ui_cname = "${local.haystack_ui_cname}"
  k8s_dashboard_cname = "${local.k8s_dashboard_cname}"
  logs_cname = "${local.logs_cname}"
  "app-node_selecter_label" = "${local.app-node_selecter_label}"
  "monitoring-node_selecter_label" = "${local.monitoring-node_selecter_label}"
  "datastores_heap_memory_in_mb" = "${local.k8s_datastores_heap_memory_in_mb}"
}
module "haystack-infrastructure" {
  source = "../../../modules/haystack-datastores/kubernetes"
  k8s_app_name_space = "${module.k8s-addons.k8s_app_namespace}"
  k8s_cluster_name = "${var.kubectl_context_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  node_selecter_label = "${local.app-node_selecter_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  cpu_limit = "${local.default_cpu_limit}"
  memory_limit = "${local.memory_limit_in_mb}"

}
