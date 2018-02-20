locals {
  app-node_selecter_label = {
    "kops.k8s.io/instancegroup" = "app-nodes"
  }
}
data "terraform_remote_state" "haystack_inrastructure" {
  backend = "s3"
  config {
    bucket = "${var.s3_bucket_name}"
    key = "terraform/${var.haystack_cluster_name}-infrastructure"
    region = "us-west-2"
  }
}

module "haystack-apps" {
  source = "../../../modules/haystack-apps/kubernetes"
  elasticsearch_hostname = "${data.terraform_remote_state.haystack_inrastructure.elasticsearch_hostname}"
  elasticsearch_port = "${data.terraform_remote_state.haystack_inrastructure.elasticsearch_port}"
  kubectl_context_name = "${data.terraform_remote_state.haystack_inrastructure.k8s_cluster_name}"
  cassandra_hostname = "${data.terraform_remote_state.haystack_inrastructure.cassandra_hostname}"
  kafka_hostname = "${data.terraform_remote_state.haystack_inrastructure.kafka_hostname}"
  kafka_port = "${data.terraform_remote_state.haystack_inrastructure.kafka_port}"
  cassandra_port = "${data.terraform_remote_state.haystack_inrastructure.cassandra_port}"
  graphite_hostname = "${data.terraform_remote_state.haystack_inrastructure.graphite_hostname}"
  graphite_port = "${data.terraform_remote_state.haystack_inrastructure.graphite_port}"
  k8s_app_namespace = "${data.terraform_remote_state.haystack_inrastructure.k8s_app_namespace}"
  haystack_cluster_name = "${var.haystack_cluster_name}"

  pipes_json_transformer_enabled = "${var.pipes_json_transformer_enabled}"
  pipes_kafka_producer_enabled = "${var.pipes_kafka_producer_enabled}"
  pipes_http_poster_enabled = "${var.pipes_http_poster_enabled}"
  pipes_json_transformer_instances = "${var.pipes_json_transformer_instances}"
  pipes_kafka_producer_instances = "${var.pipes_kafka_producer_instances}"
  pipes_http_poster_instances = "${var.pipes_http_poster_instances}"
  pipes_version = "${var.pipes_version}"

  traces_enabled = "${var.traces_enabled}"
  traces_version = "${var.traces_version}"
  traces_indexer_instances = "${var.traces_indexer_instances}"
  traces_reader_instances = "${var.traces_reader_instances}"

  trends_enabled = "${var.trends_enabled}"
  trends_version = "${var.trends_version}"
  span_timeseries_transformer_instances = "${var.span_timeseries_transformer_instances}"
  timeseries_aggregator_instances = "${var.timeseries_aggregator_instances}"

  kinesis_span_collector_instances = "${var.kinesis_span_collector_instances}"
  kinesis_span_collector_enabled = "${var.kinesis_span_collector_enabled}"
  kinesis_span_collector_version = "${var.kinesis_span_collector_version}"
  kinesis_stream_region = "${var.kinesis_stream_region}"
  kinesis_stream_name = "${var.kinesis_stream_name}"
  kinesis_span_collector_sts_role_arn = "${var.kinesis_span_collector_sts_role_arn}"

  ui_version = "${var.ui_version}"
  haystack_ui_instances = "${var.haystack_ui_instances}"
  app-node_selecter_label = "${local.app-node_selecter_label}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  pipes_http_poster_httppost_pollpercent = "${var.pipes_http_poster_httppost_pollpercent}"
  pipes_http_poster_httppost_url = "${var.pipes_http_poster_httppost_url}"

  external_metric_tank_kafka_broker_port = "${var.external_metric_tank_kafka_broker_port}"
  external_metric_tank_hostname = "${var.external_metric_tank_hostname}"
  external_metric_tank_port = "${var.external_metric_tank_port}"
  external_metric_tank_kafka_broker_hostname = "${var.external_metric_tank_kafka_broker_hostname}"
  metric_tank_instances = "${var.metric_tank_instances}"
  metric_tank_memory_limit = "${var.metric_tank_memory_limit}"
}
