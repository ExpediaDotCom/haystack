
locals {
  external_metric_tank_enabled = "${var.external_metric_tank_hostname != "" && var.external_metric_tank_port != "" && var.external_metric_tank_kafka_broker_hostname != "" && var.external_metric_tank_kafka_broker_port != "" ? "true" : "false"}"
}

# tracing apps


module "trace-indexer" {
  source = "trace-indexer"
  image = "expediadotcom/haystack-trace-indexer:${var.traces_version}"
  replicas = "${var.traces_indexer_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  elasticsearch_port = "${var.elasticsearch_port}"
  elasticsearch_hostname = "${var.elasticsearch_hostname}"
  cassandra_hostname = "${var.cassandra_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selecter_label = "${var.app-node_selector_label}"
  enabled = "${var.traces_enabled}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.trace_indexer_environment_overrides}"
}

module "trace-reader" {
  source = "trace-reader"
  image = "expediadotcom/haystack-trace-reader:${var.traces_version}"
  replicas = "${var.traces_reader_instances}"
  namespace = "${var.k8s_app_namespace}"
  elasticsearch_endpoint = "${var.elasticsearch_hostname}:${var.elasticsearch_port}"
  cassandra_hostname = "${var.cassandra_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  enabled = "${var.traces_enabled}"
  node_selecter_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.trace_reader_environment_overrides}"

}

# trends apps

//metrictank for haystack-apps
module "metrictank" {
  source = "metrictank"
  replicas = "${var.metric_tank_instances}"
  cassandra_address = "${var.cassandra_hostname}:${var.cassandra_port}"
  kafka_address = "${var.kafka_hostname}:${var.kafka_port}"
  namespace = "${var.k8s_app_namespace}"
  graphite_address = "${var.graphite_hostname}:${var.graphite_port}"
  enabled = "${local.external_metric_tank_enabled == "true" ? "false" : "true" }"
  memory_limit = "${var.metric_tank_memory_limit}"

  node_selecter_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  env_vars = "${var.metrictank_environment_overrides}"

}
module "span-timeseries-transformer" {
  source = "span-timeseries-transformer"
  image = "expediadotcom/haystack-span-timeseries-transformer:${var.trends_version}"
  replicas = "${var.span_timeseries_transformer_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selecter_label = "${var.app-node_selector_label}"
  enabled = "${var.trends_enabled}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.span_timeseries_transformer_environment_overrides}"
}
module "timeseries-aggregator" {
  source = "timeseries-aggregator"
  image = "expediadotcom/haystack-timeseries-aggregator:${var.trends_version}"
  replicas = "${var.timeseries_aggregator_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  enable_external_kafka_producer = "${local.external_metric_tank_enabled}"
  external_kafka_producer_endpoint = "${var.external_metric_tank_kafka_broker_hostname}:${var.external_metric_tank_kafka_broker_port}"
  node_selecter_label = "${var.app-node_selector_label}"
  enabled = "${var.trends_enabled}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.timeseries_aggregator_environment_overrides}"
}

# pipe apps
module "pipes-json-transformer" {
  source = "pipes-json-transformer"
  image = "expediadotcom/haystack-pipes-json-transformer:${var.pipes_version}"
  replicas = "${var.pipes_json_transformer_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selecter_label = "${var.app-node_selector_label}"
  enabled = "${var.pipes_json_transformer_enabled}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.pipes_json_transformer_environment_overrides}"

}

module "pipes-kafka-producer" {
  source = "pipes-kafka-producer"
  image = "expediadotcom/haystack-pipes-kafka-producer:${var.pipes_version}"
  replicas = "${var.pipes_kafka_producer_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selecter_label = "${var.app-node_selector_label}"
  enabled = "${var.pipes_kafka_producer_enabled}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.pipes_kafka_producer_environment_overrides}"

}

module "pipes-http-poster" {
  source = "pipes-http-poster"
  image = "expediadotcom/haystack-pipes-http-poster:${var.pipes_version}"
  replicas = "${var.pipes_http_poster_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  httppost_url = "${var.pipes_http_poster_httppost_url}"
  httppost_pollpercent = "${var.pipes_http_poster_httppost_pollpercent}"

  node_selecter_label = "${var.app-node_selector_label}"
  enabled = "${var.pipes_http_poster_enabled}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.pipes_http_poster_environment_overrides}"
}

module "pipes-firehose-writer" {
  source = "pipes-firehose-writer"
  image = "expediadotcom/haystack-pipes-firehose-writer:${var.pipes_version}"
  replicas = "${var.pipes_firehose_writer_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  firehose_url = "${var.pipes_firehose_writer_firehose_url}"
  firehose_streamname = "${var.pipes_firehose_writer_firehose_streamname}"
  firehose_signingregion = "${var.pipes_firehose_writer_firehose_signingregion}"
  node_selecter_label = "${var.app-node_selector_label}"
  enabled = "${var.pipes_firehose_writer_enabled}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.pipes_firehose_writer_environment_overrides}"
}

# collectors

module "kinesis-span-collector" {
  source = "kinesis-span-collector"
  image = "expediadotcom/haystack-kinesis-span-collector:${var.kinesis_span_collector_version}"
  replicas = "${var.kinesis_span_collector_instances}"
  namespace = "${var.k8s_app_namespace}"
  kinesis_stream_name = "${var.kinesis_stream_name}"
  kinesis_stream_region = "${var.kinesis_stream_region}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  sts_role_arn = "${var.kinesis_span_collector_sts_role_arn}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  node_selecter_label = "${var.app-node_selector_label}"
  enabled = "${var.kinesis_span_collector_enabled}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  env_vars = "${var.kinesis_span_collector_environment_overrides}"

}

# web ui
module "ui" {
  source = "ui"
  image = "expediadotcom/haystack-ui:${var.ui_version}"
  replicas = "${var.haystack_ui_instances}"
  namespace = "${var.k8s_app_namespace}"
  k8s_cluster_name = "${var.kubectl_context_name}"
  trace_reader_hostname = "${module.trace-reader.trace_reader_hostname}"
  trace_reader_service_port = "${module.trace-reader.trace_reader_service_port}"
  metrictank_hostname = "${local.external_metric_tank_enabled == "true" ? var.external_metric_tank_hostname : module.metrictank.metrictank_hostname}"
  metrictank_port = "${local.external_metric_tank_enabled == "true" ? var.external_metric_tank_port : module.metrictank.metrictank_port}"
  node_selecter_label = "${var.app-node_selector_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  cpu_limit = "${var.default_cpu_limit}"
  memory_limit = "${var.default_memory_limit}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
}