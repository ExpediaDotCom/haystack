locals {
  external_metric_tank_enabled = "${var.metrictank["external_hostname"] != "" && var.metrictank["external_kafka_broker_hostname"] != ""? "true" : "false"}"
}

//metrictank for haystack-apps
module "metrictank" {
  source = "metrictank"
  replicas = "${var.metrictank["instances"]}"
  cassandra_address = "${var.cassandra_hostname}:${var.cassandra_port}"
  kafka_address = "${var.kafka_hostname}:${var.kafka_port}"
  namespace = "${var.app_namespace}"
  graphite_address = "${var.graphite_hostname}:${var.graphite_port}"
  enabled = "${local.external_metric_tank_enabled == "true" ? "false" : "true" }"
  memory_limit = "${var.metrictank["memory_limit"]}"

  node_selecter_label = "${var.node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
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
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.trends["span_timeseries_transformer_environment_overrides"]}"
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
  external_kafka_producer_endpoint = "${var.metrictank["external_kafka_broker_hostname"]}:${var.metrictank["external_kafka_broker_port"]}"
  node_selecter_label = "${var.node_selector_label}"
  enabled = "${var.trends["enabled"]}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.trends["timeseries_aggregator_environment_overrides"]}"
}
