locals {
  metrics_cname = "metrics.${var.cluster["name"]}.${var.cluster["domain_name"]}"
  k8s_dashboard_cname = "k8s.${var.cluster["name"]}.${var.cluster["domain_name"]}"

}
module "influxdb-addon" {
  source = "influxdb"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.monitoring_addons["enabled"]}"
  kubectl_context_name = "${var.kubectl_context_name}"
  storage_volume = "${var.monitoring_addons["influxdb_storage_volume"]}"
  storage_class = "${var.cluster["storage_class"]}"
  graphite_node_port = "${var.monitoring_addons["graphite_node_port"]}"
  node_selecter_label = "${var.cluster["monitoring-node_selecter_label"]}"
  influxdb_memory_limit = "${var.monitoring_addons["influxdb_memory_limit"]}"
  influxdb_cpu_limit = "${var.monitoring_addons["influxdb_cpu_limit"]}"
}


module "heapster-addon" {
  source = "heapster"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.monitoring_addons["enabled"]}"
  kubectl_context_name = "${var.kubectl_context_name}"
  node_selecter_label = "${var.cluster["monitoring-node_selecter_label"]}"
}


module "grafana-addon" {
  source = "grafana"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.monitoring_addons["enabled"]}"
  kubectl_context_name = "${var.kubectl_context_name}"
  storage_class = "${var.cluster["storage_class"]}"
  storage_volume = "${var.monitoring_addons["grafana_storage_volume"]}"
  metrics_cname = "${local.metrics_cname}"
  root_url = "${var.monitoring_addons["grafana_root_url"] == "" ? local.metrics_cname : var.monitoring_addons["grafana_root_url"]}"
  node_selecter_label = "${var.cluster["monitoring-node_selecter_label"]}"
}


module "dashboard-addon" {
  source = "dashboard"
  enabled = "${var.monitoring_addons["enabled"]}"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_dashboard_cname = "${local.k8s_dashboard_cname}"
  node_selecter_label = "${var.cluster["monitoring-node_selecter_label"]}"
}

module "grafana-dashboards-addon" {
  source = "grafana_dashboards"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.monitoring_addons["enabled"]}"
  kubectl_context_name = "${var.kubectl_context_name}"
}