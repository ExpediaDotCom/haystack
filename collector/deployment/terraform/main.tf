locals {
  default_kinesis_stream_name = "${var.kinesis-stream_name}"
  default_kinesis_stream_region = "${var.kinesis-stream_region}"
}

module "kinesis-span-collector" {
  source = "kinesis-span-collector"
  image = "expediadotcom/haystack-kinesis-span-collector:${var.collector["version"]}"
  replicas = "${var.collector["kinesis_span_collector_instances"]}"
  enabled = "${var.collector["kinesis_span_collector_enabled"]}"

  kinesis_stream_name = "${var.collector["kinesis_stream_name"] == "" ? local.default_kinesis_stream_name : var.collector["kinesis_stream_name"]}"
  kinesis_stream_region = "${var.collector["kinesis_stream_region"] == "" ? local.default_kinesis_stream_region : var.collector["kinesis_stream_region"]}"

  sts_role_arn = "${var.collector["kinesis_span_collector_sts_role_arn"]}"
  env_vars = "${var.collector["kinesis_span_collector_environment_overrides"]}"

  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  node_selecter_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.collector["kinesis_span_collector_cpu_limit"]}"
  cpu_request = "${var.collector["kinesis_span_collector_cpu_request"]}"
  memory_request = "${var.collector["kinesis_span_collector_memory_request"]}"
  memory_limit = "${var.collector["kinesis_span_collector_memory_limit"]}"
  jvm_memory_limit = "${var.collector["kinesis_span_collector_jvm_memory_limit"]}"
  app_name = "${var.collector["kinesis_span_collector_app_name"]}"
  max_spansize_validation_enabled = "${var.collector["kinesis_span_collector_max_spansize_validation_enabled"]}"
  max_spansize_log_only = "${var.collector["kinesis_span_collector_max_spansize_log_only"]}"
  max_spansize_limit = "${var.collector["kinesis_span_collector_max_spansize_limit"]}"
  message_tag_key = "${var.collector["kinesis_span_collector_message_tag_key"]}"
  message_tag_value = "${var.collector["kinesis_span_collector_message_tag_value"]}"
  max_spansize_skip_tags = "${var.collector["kinesis_span_collector_max_spansize_skip_tags"]}"
  max_spansize_skip_services = "${var.collector["kinesis_span_collector_max_spansize_skip_services"]}"
}

module "http-span-collector" {
  source = "http-span-collector"
  image = "expediadotcom/haystack-http-span-collector:${var.collector["version"]}"
  replicas = "${var.collector["http_span_collector_instances"]}"
  enabled = "${var.collector["http_span_collector_enabled"]}"
  env_vars = "${var.collector["http_span_collector_environment_overrides"]}"

  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  node_selecter_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.collector["http_span_collector_cpu_limit"]}"
  cpu_request = "${var.collector["http_span_collector_cpu_request"]}"
  memory_request = "${var.collector["http_span_collector_memory_request"]}"
  memory_limit = "${var.collector["http_span_collector_memory_limit"]}"
  jvm_memory_limit = "${var.collector["http_span_collector_jvm_memory_limit"]}"
  app_name = "${var.collector["http_span_collector_app_name"]}"
  max_spansize_validation_enabled = "${var.collector["http_span_collector_max_spansize_validation_enabled"]}"
  max_spansize_log_only = "${var.collector["http_span_collector_max_spansize_log_only"]}"
  max_spansize_limit = "${var.collector["http_span_collector_max_spansize_limit"]}"
  message_tag_key = "${var.collector["http_span_collector_message_tag_key"]}"
  message_tag_value = "${var.collector["http_span_collector_message_tag_value"]}"
  max_spansize_skip_tags = "${var.collector["http_span_collector_max_spansize_skip_tags"]}"
  max_spansize_skip_services = "${var.collector["http_span_collector_max_spansize_skip_services"]}"
}
