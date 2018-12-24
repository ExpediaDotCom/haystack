locals {
  graphite_hostname = "monitoring-influxdb-graphite.kube-system.svc"
  graphite_port = 2003
  haystack_ui_cname = "${var.cluster["name"]}.${var.cluster["domain_name"]}"
  logs_cname = "logs.${var.cluster["name"]}.${var.cluster["domain_name"]}"
}

module "alerting-addon" {
  source = "alerting"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  cluster = "${var.cluster}"
  kubectl_context_name = "${var.kubectl_context_name}"
  alerting_addons = "${var.alerting_addons}"
}

module "monitoring-addons" {
  source = "monitoring"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  datastores_heap_memory_in_mb = "${var.datastores_heap_memory_in_mb}"
  cluster = "${var.cluster}"
  monitoring_addons = "${var.monitoring_addons}"
}

module "logging-addons" {
  source = "logging"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  enabled = "${var.logging_addons["enabled"]}"
  container_log_path = "${var.logging_addons["container_log_path"]}"
  es_nodes = "${var.logging_addons["es_nodes"]}"
  k8s_storage_class = "${var.cluster["storage_class"]}"
  es_storage_volume = "${var.logging_addons["es_storage_volume"]}"
  datastores_heap_memory_in_mb = "${var.datastores_heap_memory_in_mb}"
  logs_cname = "${local.logs_cname}"
  node_selecter_label = "${var.cluster["monitoring-node_selecter_label"]}"
}

module "traefik-addon" {
  source = "traefik"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  haystack_ui_cname = "${local.haystack_ui_cname}"
  traefik_node_port = "${var.cluster["reverse_proxy_port"]}"
  app-node_selecter_label = "${var.cluster["app-node_selecter_label"]}"
}

module "aa_apps_resource_limits" {
  source = "aa_apps_resource_limits"
  enabled = "${var.aa_apps_resource_limits["enabled"]}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  cpu_limit = "${var.aa_apps_resource_limits["cpu_limit"]}"
  memory_limit = "${var.aa_apps_resource_limits["memory_limit"]}"
  aa_app_namespace = "${module.traefik-addon.aa_app_namespace}"
}
