module "influxdb-addon" {
  source = "influxdb"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
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
}