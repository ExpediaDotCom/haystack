module "elasticsearch-addon" {
  source = "elasticsearch"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  minimum_masters = "${var.es_nodes}"
  storage_volume = "${var.es_storage_volume}"
  storage_class = "${var.k8s_storage_class}"
  heap_memory_in_mb = "${var.datastores_heap_memory_in_mb}"
  monitoring-node_selecter_label = "${var.node_selecter_label}"
}

module "curator-addon" {
  source = "curator"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  monitoring-node_selecter_label = "${var.node_selecter_label}"
  elasticsearch_host = "${module.elasticsearch-addon.host}"
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
  monitoring-node_selecter_label = "${var.node_selecter_label}"
  logs_cname = "${var.logs_cname}"
}