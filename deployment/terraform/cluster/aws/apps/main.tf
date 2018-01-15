data "terraform_remote_state" "haystack_inrastructure" {
  backend = "s3"
  config {
    bucket = "${var.infra_backend_s3_bucket_name}"
    key = "${var.infra_backend_s3_path}"
    region = "${var.infra_backend_s3_region}"
  }
}
//metrictank for haystack-apps
module "metrictank" {
  source = "../kubernetes/metrictank"
  replicas = "1"
  cassandra_address = "${data.terraform_remote_state.haystack_inrastructure.cassandra_hostname}:${data.terraform_remote_state.haystack_inrastructure.cassandra_port}"
  kafka_address = "${data.terraform_remote_state.haystack_inrastructure.kafka_hostname}:${data.terraform_remote_state.haystack_inrastructure.kafka_port}"
  namespace = "${data.terraform_remote_state.haystack_inrastructure.k8s_app_namespace}"
  graphite_address = "${data.terraform_remote_state.haystack_inrastructure.graphite_hostname}:${data.terraform_remote_state.haystack_inrastructure.graphite_port}"
}


module "haystack-apps" {
  source = "../../../modules/haystack-apps/kubernetes"
  elasticsearch_hostname = "${data.terraform_remote_state.haystack_inrastructure.elasticsearch_hostname}"
  elasticsearch_port = "${data.terraform_remote_state.haystack_inrastructure.elasticsearch_port}"
  k8s_cluster_name = "${data.terraform_remote_state.haystack_inrastructure.k8s_cluster_name}"
  cassandra_hostname = "${data.terraform_remote_state.haystack_inrastructure.cassandra_hostname}"
  kafka_hostname = "${data.terraform_remote_state.haystack_inrastructure.kafka_hostname}"
  kafka_port = "${data.terraform_remote_state.haystack_inrastructure.kafka_port}"
  cassandra_port = "${data.terraform_remote_state.haystack_inrastructure.cassandra_port}"
  metrictank_hostname = "${data.terraform_remote_state.haystack_inrastructure.metrictank_hostname}"
  metrictank_port = "${data.terraform_remote_state.haystack_inrastructure.metrictank_port}"
  graphite_hostname = "${data.terraform_remote_state.haystack_inrastructure.graphite_hostname}"
  k8s_app_namespace = "${data.terraform_remote_state.haystack_inrastructure.k8s_app_namespace}"

  pipes_enabled = "${var.pipes_enabled}"
  pipes_json_transformer_instances = "${var.pipes_json_transformer_instances}"
  pipes_kafka_producer_instances = "${var.pipes_kafka_producer_instances}"
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

  ui_version = "${var.ui_version}"
  haystack_ui_instances = "${var.haystack_ui_instances}"
}
