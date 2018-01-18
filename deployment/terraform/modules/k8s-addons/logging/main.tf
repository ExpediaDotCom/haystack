module "elasticsearch-addon" {
  source = "elasticsearch"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  minimum_masters = "${var.es_nodes}"
  storage_volume = "${var.es_storage_volume}"
  storage_class = "${var.k8s_storage_class}"
}
module "fluentd-addon" {
  source = "fluentd"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  container_log_path = "${var.container_log_path}"
  elasticsearch_host = "${module.elasticsearch-addon.host}"
  elasticsearch_port = "${module.elasticsearch-addon.port}"
}
module "kibana-addon" {
  source = "kibana"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  elasticsearch_http_endpoint = "${module.elasticsearch-addon.http_endpoint}"
}