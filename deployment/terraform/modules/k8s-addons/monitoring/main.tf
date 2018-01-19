module "influxdb-addon" {
  source = "influxdb"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  kubectl_context_name = "${var.kubectl_context_name}"
  storage_volume = "${var.influxdb_storage_volume}"
  storage_class = "${var.k8s_storage_class}"
}


module "heapster-addon" {
  source = "heapster"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  kubectl_context_name = "${var.kubectl_context_name}"
}


module "grafana-addon" {
  source = "grafana"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  kubectl_context_name = "${var.kubectl_context_name}"
  storage_class = "${var.k8s_storage_class}"
  storage_volume = "${var.grafana_storage_volume}"
  metrics_cname = "${var.metrics_cname}"
}