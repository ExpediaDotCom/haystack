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
  logging_backend = "${var.logging_backend}"
}

module "curator-addon" {
  source = "curator"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  monitoring-node_selecter_label = "${var.node_selecter_label}"
  elasticsearch_host = "${module.elasticsearch-addon.host}"
  logging_backend = "${var.logging_backend}"
  curator_image = "${var.curator_image}"
}

module "fluentd-addon" {
  source = "fluentd"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  container_log_path = "${var.container_log_path}"
  elasticsearch_host = "${module.elasticsearch-addon.host}"
  elasticsearch_port = "${module.elasticsearch-addon.port}"
  logging_backend = "${var.logging_backend}"
}
module "kibana-addon" {
  source = "kibana"
  kubectl_context_name = "${var.kubectl_context_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  enabled = "${var.enabled}"
  elasticsearch_http_endpoint = "${module.elasticsearch-addon.http_endpoint}"
  monitoring-node_selecter_label = "${var.node_selecter_label}"
  logs_cname = "${var.logs_cname}"
  logging_backend = "${var.logging_backend}"
  kibana_logging_image = "${var.kibana_logging_image}"
}

module "splunkforwarder" {
  source = "splunkforwarder"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  enabled = "${var.enabled}"
  splunk_deployment_server = "${var.splunk_deployment_server}"
  monitoring-node_selecter_label = "${var.node_selecter_label}"
  logging_backend = "${var.logging_backend}"
  cluster_name = "${var.cluster_name}"
  splunk_index = "${var.splunk_index}"
  splunk_forwarder_image = "${var.splunk_forwarder_image}"
}