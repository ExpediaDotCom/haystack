# tracing apps
module "trace-indexer" {
  source = "trace-indexer"
  image = "expediadotcom/haystack-trace-indexer:${var.traces_version}"
  replicas = "${var.traces_indexer_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  elasticsearch_endpoint = "${var.elasticsearch_hostname}:${var.elasticsearch_port}"
  cassandra_hostname = "${var.cassandra_hostname}"
  enabled = "${var.traces_enabled}"
}
module "trace-reader" {
  source = "trace-reader"
  image = "expediadotcom/haystack-trace-reader:${var.traces_version}"
  replicas = "${var.traces_reader_instances}"
  namespace = "${var.k8s_app_namespace}"
  enabled = "${var.traces_enabled}"
}

# trends apps
module "span-timeseries-transformer" {
  source = "span-timeseries-transformer"
  image = "expediadotcom/haystack-span-timeseries-transformer:${var.trends_version}"
  replicas = "${var.span_timeseries_transformer_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  enabled = "${var.trends_enabled}"
}
module "timeseries-aggregator" {
  source = "timeseries-aggregator"
  image = "expediadotcom/haystack-timeseries-aggregator:${var.trends_version}"
  replicas = "${var.timeseries_aggregator_instances}"
  namespace = "${var.k8s_app_namespace}"
  kafka_endpoint = "${var.kafka_hostname}:${var.kafka_port}"
  enabled = "${var.trends_enabled}"
}

# pipe apps
module "pipes-json-transformer" {
  source = "pipes-json-transformer"
  image = "expediadotcom/haystack-pipes-json-transformer:${var.pipes_version}"
  replicas = "${var.pipes_json_transformer_instances}"
  namespace = "${var.k8s_app_namespace}"
  graphite_hostname = "${var.kafka_hostname}"
  kafka_hostname = "${var.kafka_hostname}"
  enabled = "${var.pipes_enabled}"

}
module "pipes-kafka-producer" {
  source = "pipes-kafka-producer"
  image = "expediadotcom/haystack-pipes-kafka-producer:${var.pipes_version}"
  replicas = "${var.pipes_kafka_producer_instances}"
  namespace = "${var.k8s_app_namespace}"
  graphite_hostname = "${var.graphite_hostname}"
  kafka_hostname = "${var.kafka_hostname}"
  enabled = "${var.pipes_enabled}"
}

# web ui
module "ui" {
  source = "ui"
  image = "expediadotcom/haystack-ui:${var.ui_version}"
  replicas = "${var.haystack_ui_instances}"
  namespace = "${var.k8s_app_namespace}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
  trace_reader_hostname = "${module.trace-reader.trace_reader_hostname}"
  trace_reader_service_port = "${module.trace-reader.trace_reader_service_port}"
  metrictank_hostname = "${var.metrictank_hostname}"
  metrictank_port = "${var.metrictank_port}"
}