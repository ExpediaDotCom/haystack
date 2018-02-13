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
  node_selecter_label = "${var.app-node_selecter_label}"
  enabled = "${var.traces_enabled}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
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
  node_selecter_label = "${var.app-node_selecter_label}"
  enabled = "${var.traces_enabled}"
}

# trends apps

//metrictank for haystack-apps
module "metrictank" {
  source = "metrictank"
  replicas = "1"
  cassandra_address = "${var.cassandra_hostname}:${var.cassandra_port}"
  kafka_address = "${var.kafka_hostname}:${var.kafka_port}"
  namespace = "${var.k8s_app_namespace}"
  graphite_address = "${var.graphite_hostname}:${var.graphite_port}"
  node_selecter_label = "${var.app-node_selecter_label}"
  enabled = "true"
}
module "span-timeseries-transformer" {
  source = "span-timeseries-transformer"
  image = "expediadotcom/haystack-span-timeseries-transformer:${var.trends_version}"
  replicas = "${var.span_timeseries_transformer_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selecter_label = "${var.app-node_selecter_label}"
  enabled = "${var.trends_enabled}"
}
module "timeseries-aggregator" {
  source = "timeseries-aggregator"
  image = "expediadotcom/haystack-timeseries-aggregator:${var.trends_version}"
  replicas = "${var.timeseries_aggregator_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selecter_label = "${var.app-node_selecter_label}"
  enabled = "${var.trends_enabled}"
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
  node_selecter_label = "${var.app-node_selecter_label}"
  enabled = "${var.pipes_json_transformer_enabled}"
}

module "pipes-kafka-producer" {
  source = "pipes-kafka-producer"
  image = "expediadotcom/haystack-pipes-kafka-producer:${var.pipes_version}"
  replicas = "${var.pipes_kafka_producer_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_hostname = "${var.kafka_hostname}"
  graphite_hostname = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  node_selecter_label = "${var.app-node_selecter_label}"
  enabled = "${var.pipes_kafka_producer_enabled}"
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
  node_selecter_label = "${var.app-node_selecter_label}"
  enabled = "${var.kinesis_span_collector_enabled}"
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
  metrictank_hostname = "${module.metrictank.metrictank_hostname}"
  metrictank_port = "${module.metrictank.metrictank_port}"
  node_selecter_label = "${var.app-node_selecter_label}"
}