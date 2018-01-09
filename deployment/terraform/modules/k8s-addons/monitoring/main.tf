module "influxdb-addon" {
  source = "influxdb"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
  storage_volume = "${var.influxdb_storage_volume}"
  storage_class = "${var.k8s_storage_class}"
}


module "heapster-addon" {
  source = "heapster"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
}


module "grafana-addon" {
  source = "grafana"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
  storage_class = "${var.k8s_storage_class}"
  storage_volume = "${var.grafana_storage_volume}"
}