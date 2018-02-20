variable "elasticsearch_hostname" {}
variable "elasticsearch_port" {}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "cassandra_hostname" {}
variable "cassandra_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "haystack_cluster_name" {}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "k8s_app_namespace" {}
variable "app-node_selecter_label" {
  type = "map"
}

# traces config
variable "traces_enabled" {}
variable "traces_indexer_instances" {}
variable "traces_reader_instances" {}
variable "traces_version" {}

# trends config
variable "trends_enabled" {}
variable "span_timeseries_transformer_instances" {}
variable "timeseries_aggregator_instances" {}
variable "trends_version" {}

# pipes config
variable "pipes_json_transformer_enabled" {}
variable "pipes_kafka_producer_enabled" {}
variable "pipes_http_poster_enabled" {}
variable "pipes_json_transformer_instances" {}
variable "pipes_kafka_producer_instances" {}
variable "pipes_http_poster_instances" {}
variable "pipes_http_poster_httppost_url" {}
variable "pipes_http_poster_httppost_pollpercent" {}
variable "pipes_version" {}

# collectors config
variable "kinesis_span_collector_instances" {}
variable "kinesis_span_collector_enabled" {}
variable "kinesis_span_collector_version" {}
variable "kinesis_stream_region" {}
variable "kinesis_stream_name" {}
variable "kinesis_span_collector_sts_role_arn" {}

# ui config
variable "haystack_ui_instances" {}
variable "ui_version" {}

# external kafka broker and metric tank endpoint
variable "external_metric_tank_kafka_broker_hostname" {}
variable "external_metric_tank_kafka_broker_port" {}
variable "external_metric_tank_hostname" {}
variable "external_metric_tank_port" {}
