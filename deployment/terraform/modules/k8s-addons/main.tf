locals {
  graphite_hostname = "monitoring-influxdb-graphite.kube-system.svc"
  graphite_port = 2003
}

module "kubewatch-addon" {
   source = "kubewatch"
   kubectl_executable_name = "${var.kubectl_executable_name}"
   enabled = "${var.add_kubewatch_addon}"
   kubectl_context_name = "${var.kubectl_context_name}"
   node_selecter_label = "${var.monitoring-node_selecter_label}"
   kubewatch_config_yaml_base64 = "${var.kubewatch_config_yaml_base64}"
}

module "monitoring-addons" {
  source = "monitoring"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.add_monitoring_addons}"
  kubectl_context_name = "${var.kubectl_context_name}"
  grafana_storage_volume = "${var.grafana_storage_volume}"
  k8s_storage_class = "${var.k8s_storage_class}"
  influxdb_storage_volume = "${var.influxdb_storage_volume}"
  datastores_heap_memory_in_mb = "${var.datastores_heap_memory_in_mb}"
  metrics_cname = "${var.metrics_cname}"
  graphite_node_port = "${var.graphite_node_port}"
  monitoring-node_selecter_label = "${var.monitoring-node_selecter_label}"
}

module "logging-addons" {
  source = "logging"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  enabled = "${var.add_logging_addons}"
  container_log_path = "${var.container_log_path}"
  es_nodes = "${var.logging_es_nodes}"
  k8s_storage_class = "${var.k8s_storage_class}"
  datastores_heap_memory_in_mb = "${var.datastores_heap_memory_in_mb}"
  es_storage_volume = "${var.es_storage_volume}"
  logs_cname = "${var.logs_cname}"
  monitoring-node_selecter_label = "${var.monitoring-node_selecter_label}"
}

module "traefik-addon" {
  source = "traefik"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  haystack_ui_cname = "${var.haystack_ui_cname}"
  traefik_node_port = "${var.traefik_node_port}"
  app-node_selecter_label = "${var.app-node_selecter_label}"
}

module "dashboard-addon" {
  source = "dashboard"
  enabled = "${var.add_k8s_dashboard_addons}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_dashboard_cname = "${var.k8s_dashboard_cname}"
  monitoring-node_selecter_label = "${var.monitoring-node_selecter_label}"
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
