module "influxdb-addon" {
  source = "influxdb"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
}

module "grafana-addon" {
  source = "grafana"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
}

module "heapster-addon" {
  source = "heapster"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
}