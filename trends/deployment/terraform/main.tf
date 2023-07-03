locals {
  external_metric_tank_enabled = "${var.metrictank["external_hostname"] != "" && var.metrictank["external_kafka_broker_hostname"] != ""? "true" : "false"}"
}

//metrictank for haystack-apps
module "metrictank" {
  source = "metrictank"
  replicas = "${var.metrictank["instances"]}"
  cassandra_address = "${var.cassandra_hostname}:${var.cassandra_port}"
  tag_support = "${var.metrictank["tag_support"]}"
  kafka_address = "${var.kafka_hostname}:${var.kafka_port}"
  namespace = "${var.app_namespace}"
  graphite_address = "${var.graphite_hostname}:${var.graphite_port}"
  enabled = "${local.external_metric_tank_enabled == "true" ? "false" : "true" }"
  memory_limit = "${var.metrictank["memory_limit"]}"
  memory_request = "${var.metrictank["memory_request"]}"
  cpu_limit = "${var.metrictank["cpu_limit"]}"
  cpu_request = "${var.metrictank["cpu_request"]}"
  node_selecter_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  env_vars = "${var.metrictank["environment_overrides"]}"

}
module "span-timeseries-transformer" {
  source = "span-timeseries-transformer"
  image = "expediadotcom/haystack-span-timeseries-transformer:${var.trends["version"]}"
  replicas = "${var.trends["span_timeseries_transformer_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  node_selecter_label = "${var.node_selector_label}"
  enabled = "${var.trends["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.trends["span_timeseries_transformer_cpu_limit"]}"
  cpu_request = "${var.trends["span_timeseries_transformer_cpu_request"]}"
  memory_limit = "${var.trends["span_timeseries_transformer_memory_limit"]}"
  memory_request = "${var.trends["span_timeseries_transformer_memory_request"]}"
  jvm_memory_limit = "${var.trends["span_timeseries_transformer_jvm_memory_limit"]}"
  env_vars = "${var.trends["span_timeseries_transformer_environment_overrides"]}"
  kafka_num_stream_threads = "${var.trends["span_timeseries_transformer_kafka_num_stream_threads"]}"
  metricpoint_encoder_type = "${var.trends["metricpoint_encoder_type"]}"
}
module "timeseries-aggregator" {
  source = "timeseries-aggregator"
  image = "expediadotcom/haystack-timeseries-aggregator:${var.trends["version"]}"
  replicas = "${var.trends["timeseries_aggregator_instances"]}"
  namespace = "${var.app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  graphite_enabled = "${var.graphite_enabled}"
  enable_external_kafka_producer = "${local.external_metric_tank_enabled}"
  enable_metrics_sink = "${var.trends["timeseries_aggregator_enable_metrics_sink"]}"
  external_kafka_producer_endpoint = "${var.metrictank["external_kafka_broker_hostname"]}:${var.metrictank["external_kafka_broker_port"]}"
  node_selecter_label = "${var.node_selector_label}"
  enabled = "${var.trends["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.trends["timeseries_aggregator_cpu_limit"]}"
  cpu_request = "${var.trends["timeseries_aggregator_cpu_request"]}"
  memory_limit = "${var.trends["timeseries_aggregator_memory_limit"]}"
  memory_request = "${var.trends["timeseries_aggregator_memory_request"]}"
  jvm_memory_limit = "${var.trends["timeseries_aggregator_jvm_memory_limit"]}"
  env_vars = "${var.trends["timeseries_aggregator_environment_overrides"]}"
  metricpoint_encoder_type = "${var.trends["metricpoint_encoder_type"]}"
  histogram_max_value = "${var.trends["timeseries_aggregator_histogram_max_value"]}"
  histogram_precision = "${var.trends["timeseries_aggregator_histogram_precision"]}"
  histogram_value_unit = "${var.trends["timeseries_aggregator_histogram_value_unit"]}"
  additionalTags = "${var.trends["timeseries_aggregator_additional_tags"]}"
}
