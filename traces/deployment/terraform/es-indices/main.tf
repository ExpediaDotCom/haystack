module "curator_trace_index" {
  source = "curator-trace-index"
  kubectl_context_name = "${var.kubectl_context_name}"
  enabled = "${var.enabled}"
  elasticsearch_hostname = "${var.elasticsearch_hostname}"
  elasticsearch_port = "${var.elasticsearch_port}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  namespace = "${var.namespace}"
}

module "curator_service_metadata" {
  source = "curator-service-metadata"
  kubectl_context_name = "${var.kubectl_context_name}"
  enabled = "${var.enabled}"
  elasticsearch_hostname = "${var.elasticsearch_hostname}"
  elasticsearch_port = "${var.elasticsearch_port}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  namespace = "${var.namespace}"
}

module "whitelisted_fields" {
  source = "whitelisted-fields"
  kubectl_context_name = "${var.kubectl_context_name}"
  enabled = "${var.enabled}"
  elasticsearch_hostname = "${var.elasticsearch_hostname}"
  elasticsearch_port = "${var.elasticsearch_port}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  namespace = "${var.namespace}"
}
