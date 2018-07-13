module "node-finder" {
  source = "node-finder"
  image = "expediadotcom/haystack-service-graph-node-finder:${var.service-graph["version"]}"
  replicas = "${var.service-graph["node_finder_instances"]}"
  namespace = "${var.namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"

  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selecter_label = "${var.node_selector_label}"
  enabled = "${var.service-graph["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.service-graph["node_finder_cpu_limit"]}"
  cpu_request = "${var.service-graph["node_finder_cpu_request"]}"
  memory_limit = "${var.service-graph["node_finder_memory_limit"]}"
  memory_request = "${var.service-graph["node_finder_memory_request"]}"
  jvm_memory_limit = "${var.service-graph["node_finder_jvm_memory_limit"]}"
  env_vars = "${var.service-graph["node_finder_environment_overrides"]}"
  metricpoint_encoder_type = "${var.service-graph["metricpoint_encoder_type"]}"
  collect_tags = "${var.service-graph["collect_tags"]}"
}

module "graph-builder" {
  source = "graph-builder"
  image = "expediadotcom/haystack-service-graph-graph-builder:${var.service-graph["version"]}"
  replicas = "${var.service-graph["graph_builder_instances"]}"
  namespace = "${var.namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selecter_label = "${var.node_selector_label}"
  enabled = "${var.service-graph["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.service-graph["graph_builder_cpu_limit"]}"
  cpu_request = "${var.service-graph["graph_builder_cpu_request"]}"
  memory_limit = "${var.service-graph["graph_builder_memory_limit"]}"
  memory_request = "${var.service-graph["graph_builder_memory_request"]}"
  jvm_memory_limit = "${var.service-graph["graph_builder_jvm_memory_limit"]}"
  env_vars = "${var.service-graph["graph_builder_environment_overrides"]}"
}
