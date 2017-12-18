locals {
  fluentdSource = "fluentd"
}

module "fluentd-addon" {
  source = "fluentd"
  k8s_cluster_name = "${var.k8s_cluster_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_logs_es_url = "${var.k8s_logs_es_url}"
}