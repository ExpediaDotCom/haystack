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
variable "app-node_selector_label"{}

variable "default_memory_limit"{}
variable "default_cpu_limit"{}

# traces config
variable "traces_enabled" {}
variable "traces_indexer_instances" {}
variable "traces_reader_instances" {}
variable "traces_version" {}
variable "trace_indexer_environment_overrides" {}
variable "trace_reader_environment_overrides" {}

# trends config
variable "trends_enabled" {}
variable "span_timeseries_transformer_instances" {}
variable "timeseries_aggregator_instances" {}
variable "trends_version" {}
variable "timeseries_aggregator_environment_overrides" {}
variable "span_timeseries_transformer_environment_overrides" {}
variable "timeseries_aggregator_memory_limit" {}
variable "timeseries_aggregator_java_memory_limit" {}


# pipes config
variable "pipes_json_transformer_enabled" {}
variable "pipes_kafka_producer_enabled" {}
variable "pipes_http_poster_enabled" {}
variable "pipes_firehose_writer_enabled" {}
variable "pipes_secret_detector_enabled" {}
variable "pipes_json_transformer_instances" {}
variable "pipes_kafka_producer_instances" {}
variable "pipes_http_poster_instances" {}
variable "pipes_http_poster_httppost_url" {}
variable "pipes_http_poster_httppost_pollpercent" {}
variable "pipes_firehose_writer_instances" {}
variable "pipes_secret_detector_instances" {}
variable "pipes_firehose_writer_firehose_url" {}
variable "pipes_firehose_writer_firehose_streamname" {}
variable "firehose_kafka_threadcount" {}
variable "pipes_firehose_writer_firehose_signingregion" {}
variable "pipes_firehose_writer_firehose_initialretrysleep" {}
variable "pipes_firehose_writer_firehose_maxretrysleep" {}
variable "pipes_version" {}
variable "pipes_firehose_writer_environment_overrides" {}
variable "pipes_http_poster_environment_overrides" {}
variable "pipes_kafka_producer_environment_overrides" {}
variable "pipes_json_transformer_environment_overrides" {}
variable "pipes_secret_detector_secretsnotifications_email_from" {}
variable "pipes_secret_detector_secretsnotifications_email_tos" {}
variable "pipes_secret_detector_secretsnotifications_email_host" {}
variable "pipes_secret_detector_secretsnotifications_email_subject" {}
variable "pipes_secret_detector_environment_overrides" {}


# collectors config
variable "kinesis_span_collector_instances" {}
variable "kinesis_span_collector_enabled" {}
variable "kinesis_span_collector_version" {}
variable "kinesis_stream_region" {}
variable "kinesis_stream_name" {}
variable "kinesis_span_collector_sts_role_arn" {}
variable "kinesis_span_collector_environment_overrides" {}


# ui config
variable "haystack_ui_instances" {}
variable "ui_version" {}
variable "whitelisted_fields" {}


#metrictank
variable "metric_tank_instances" {}
variable "metric_tank_memory_limit" {}
# external kafka broker and metric tank endpoint
variable "external_metric_tank_kafka_broker_hostname" {}
variable "external_metric_tank_kafka_broker_port" {}
variable "external_metric_tank_hostname" {}
variable "external_metric_tank_port" {}
variable "metrictank_environment_overrides" {}

