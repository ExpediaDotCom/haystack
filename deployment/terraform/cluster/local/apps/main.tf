locals {
  app-node_selecter_label = "kubernetes.io/hostname: minikube"
  default_cpu_limit = "100m"
  default_memory_limit = "100Mi"
}

data "terraform_remote_state" "haystack_inrastructure" {
  backend = "local"
  config {
    path = "../infrastructure/terraform-infra.tfstate"
  }
}
module "haystack-apps" {
  source = "../../../modules/haystack-apps/kubernetes"

  default_cpu_limit = "${local.default_cpu_limit}"
  default_memory_limit = "${local.default_memory_limit}"
  elasticsearch_hostname = "${data.terraform_remote_state.haystack_inrastructure.elasticsearch_hostname}"
  elasticsearch_port = "${data.terraform_remote_state.haystack_inrastructure.elasticsearch_port}"
  kubectl_context_name = "${data.terraform_remote_state.haystack_inrastructure.k8s_cluster_name}"
  cassandra_hostname = "${data.terraform_remote_state.haystack_inrastructure.cassandra_hostname}"
  cassandra_port = "${data.terraform_remote_state.haystack_inrastructure.cassandra_port}"
  kafka_hostname = "${data.terraform_remote_state.haystack_inrastructure.kafka_hostname}"
  kafka_port = "${data.terraform_remote_state.haystack_inrastructure.kafka_port}"
  graphite_hostname = "${data.terraform_remote_state.haystack_inrastructure.graphite_hostname}"
  graphite_port = "${data.terraform_remote_state.haystack_inrastructure.graphite_port}"
  k8s_app_namespace = "${data.terraform_remote_state.haystack_inrastructure.k8s_app_namespace}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  app-node_selector_label = "${local.app-node_selecter_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"

  #pipes configuration_overrides
  pipes_json_transformer_enabled = "${var.pipes_json_transformer_enabled}"
  pipes_kafka_producer_enabled = "${var.pipes_kafka_producer_enabled}"
  pipes_http_poster_enabled = "${var.pipes_http_poster_enabled}"
  pipes_firehose_writer_enabled = "${var.pipes_firehose_writer_enabled}"
  pipes_json_transformer_instances = "${var.pipes_json_transformer_instances}"
  pipes_kafka_producer_instances = "${var.pipes_kafka_producer_instances}"
  pipes_http_poster_instances = "${var.pipes_http_poster_instances}"
  pipes_http_poster_httppost_url = "${var.pipes_http_poster_httppost_url}"
  pipes_http_poster_httppost_pollpercent = "${var.pipes_http_poster_httppost_pollpercent}"
  pipes_firehose_writer_instances = "${var.pipes_firehose_writer_instances}"
  pipes_firehose_writer_firehose_url = "${var.pipes_firehose_writer_firehose_url}"
  pipes_firehose_writer_firehose_streamname = "${var.pipes_firehose_writer_firehose_streamname}"
  pipes_firehose_writer_firehose_signingregion = "${var.pipes_firehose_writer_firehose_signingregion}"
  pipes_version = "${var.pipes_version}"
  pipes_kafka_producer_environment_overrides = "${var.pipes_kafka_producer_environment_overrides}"
  pipes_json_transformer_environment_overrides = "${var.pipes_json_transformer_environment_overrides}"
  pipes_firehose_writer_environment_overrides = "${var.pipes_firehose_writer_environment_overrides}"
  pipes_http_poster_environment_overrides = "${var.pipes_http_poster_environment_overrides}"

  #trace configuration_overrides
  traces_enabled = "${var.traces_enabled}"
  traces_version = "${var.traces_version}"
  traces_indexer_instances = "${var.traces_indexer_instances}"
  traces_reader_instances = "${var.traces_reader_instances}"
  trace_reader_environment_overrides = "${var.trace_reader_environment_overrides}"
  trace_indexer_environment_overrides = "${var.trace_indexer_environment_overrides}"

  #trends configuration_overrides
  trends_enabled = "${var.trends_enabled}"
  trends_version = "${var.trends_version}"
  span_timeseries_transformer_instances = "${var.span_timeseries_transformer_instances}"
  timeseries_aggregator_instances = "${var.timeseries_aggregator_instances}"
  timeseries_aggregator_environment_overrides = "${var.timeseries_aggregator_environment_overrides}"
  span_timeseries_transformer_environment_overrides = "${var.span_timeseries_transformer_environment_overrides}"
  timeseries_aggregator_java_memory_limit = "${var.timeseries_aggregator_java_memory_limit}"
  timeseries_aggregator_memory_limit = "${var.timeseries_aggregator_memory_limit}"

  #collector configuration_overrides
  kinesis_span_collector_instances = "${var.kinesis_span_collector_instances}"
  kinesis_span_collector_enabled = "${var.kinesis_span_collector_enabled}"
  kinesis_span_collector_version = "${var.kinesis_span_collector_version}"
  kinesis_stream_region = "${var.kinesis_stream_region}"
  kinesis_stream_name = "${var.kinesis_stream_name}"
  kinesis_span_collector_sts_role_arn = "${var.kinesis_span_collector_sts_role_arn}"
  kinesis_span_collector_environment_overrides = "${var.kinesis_span_collector_environment_overrides}"


  #ui configuration_overrides
  ui_version = "${var.ui_version}"
  haystack_ui_instances = "${var.haystack_ui_instances}"

  #metrictank configuration_overrides
  external_metric_tank_kafka_broker_port = "${var.external_metric_tank_kafka_broker_port}"
  external_metric_tank_hostname = "${var.external_metric_tank_hostname}"
  external_metric_tank_port = "${var.external_metric_tank_port}"
  external_metric_tank_kafka_broker_hostname = "${var.external_metric_tank_kafka_broker_hostname}"
  metric_tank_instances = "${var.metric_tank_instances}"
  metric_tank_memory_limit = "${var.metric_tank_memory_limit}"
  metrictank_environment_overrides = "${var.metrictank_environment_overrides}"
  whitelisted_fields = "${var.whitelisted_fields}"

}
