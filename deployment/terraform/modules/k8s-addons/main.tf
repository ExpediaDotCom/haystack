
module "monitoring-addons" {
  source = "monitoring"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.add_monitoring_addons}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
  grafana_storage_volume = "${var.grafana_storage_volume}"
  k8s_storage_class = "${var.k8s_storage_class}"
  influxdb_storage_volume = "${var.influxdb_storage_volume}"
}

module "logging-addongs" {
  source = "logging"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
  enabled = "${var.add_logging_addons}"
  container_log_path = "${var.container_log_path}"
  es_nodes = "${var.logging_es_nodes}"
  k8s_storage_class = "${var.k8s_storage_class}"
  es_storage_volume = "${var.es_storage_volume}"
}

module "traefik-addon" {
  source = "traefik"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  haystack_domain_name = "${var.haystack_domain_name}"
  traefik_node_port = "${var.traefik_node_port}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
}
