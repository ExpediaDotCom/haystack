variable "kubectl_executable_name" {}
variable "haystack_cluster_name" {
  default = "haystack"
}
# traces config
variable "traces_enabled" {
  default = true
}
variable "traces_indexer_instances" {
  default = "1"
}
variable "traces_reader_instances" {
  default = "1"
}
variable "traces_version" {
  default = "aa62f556c8d28c371a1bdd0b40a6ec115820101a"
}

variable "trace_indexer_environment_overrides" {
  default = ""
}
variable "trace_reader_environment_overrides" {
  default = ""
}

# trends config
variable "trends_enabled" {
  default = true
}
variable "span_timeseries_transformer_instances" {
  default = "1"
}
variable "timeseries_aggregator_instances" {
  default = "1"
}

variable "trends_version" {
  default = "4ddaf49005a4a90baf4abd1637d397dd0dfd552b"
}

variable "timeseries_aggregator_environment_overrides" {
  default = ""
}


variable "timeseries_aggregator_memory_limit" {
  default = "250Mi"
}
variable "timeseries_aggregator_java_memory_limit" {
  default = "175m"
}

variable "span_timeseries_transformer_environment_overrides" {
  default = ""
}


# pipes config
variable "pipes_json_transformer_enabled" {
  default = false
}
variable "pipes_kafka_producer_enabled" {
  default = false
}
variable "pipes_http_poster_enabled" {
  default = false
}
variable "pipes_firehose_writer_enabled" {
  default = false
}
variable "pipes_secret_detector_enabled" {
  default = false
}
variable "pipes_json_transformer_instances" {
  default = "1"
}
variable "pipes_kafka_producer_instances" {
  default = "1"
}
variable "pipes_http_poster_instances" {
  default = "1"
}
variable "pipes_firehose_writer_instances" {
  default = "1"
}
variable "pipes_secret_detector_instances" {
  default = "1"
}
variable "pipes_http_poster_httppost_url" {
  default = ""
}
variable "pipes_http_poster_httppost_pollpercent" {
  default = "1"
}
variable "pipes_firehose_writer_firehose_url" {
  default = ""
}
variable "pipes_secret_detector_secretsnotifications_email_from" {
  default = ""
}
variable "pipes_secret_detector_environment_overrides" {
  default = ""
}
variable "pipes_firehose_writer_firehose_streamname" {
  default = ""
}
variable "pipes_secret_detector_secretsnotifications_email_host" {
  default = ""
}
variable "firehose_kafka_threadcount" {
  default = ""
}
variable "pipes_secret_detector_secretsnotifications_email_subject" {
  default = ""
}
variable "pipes_firehose_writer_firehose_signingregion" {
  default = ""
}
variable "pipes_secret_detector_secretsnotifications_email_tos" {
  default = ""
}
variable "pipes_firehose_writer_firehose_initialretrysleep" {
  default = 10
}
variable "pipes_firehose_writer_firehose_maxretrysleep" {
  default = 5000
}
variable "pipes_version" {
  default = "f48a026554636555fc3cb20ac760e4315857f949"
}

variable "pipes_firehose_writer_environment_overrides" {
  default = ""
}
variable "pipes_http_poster_environment_overrides" {
  default = ""
}
variable "pipes_kafka_producer_environment_overrides" {
  default = ""
}
variable "pipes_json_transformer_environment_overrides" {
  default = ""
}

# collectors config
variable "kinesis_span_collector_instances" {
  default = "1"
}
variable "kinesis_span_collector_enabled" {
  default = false
}
variable "kinesis_span_collector_version" {
  default = "e1d967e30a9a87122d8c332700cc4a3152db7f8a"
}
variable "kinesis_stream_region" {
  default = ""
}
variable "kinesis_stream_name" {
  default = ""
}
variable "kinesis_span_collector_sts_role_arn" {
  default = ""
}
variable "kinesis_span_collector_environment_overrides" {
  default = ""
}


# ui config
variable "haystack_ui_instances" {
  default = "1"
}
variable "ui_version" {
  default = "c7be950888aef83fa1709c40a62b9ce68066b85b"
}
variable "whitelisted_fields" {
  default = ""
}

#metrictank
variable "external_metric_tank_kafka_broker_hostname" {
  default = ""
}
variable "external_metric_tank_kafka_broker_port" {
  default = ""
}
variable "external_metric_tank_hostname" {
  default = ""
}
variable "external_metric_tank_port" {
  default = ""
}

variable "metric_tank_instances" {
  default = 1
}

variable "metric_tank_memory_limit" {
  default = "250Mi"
}
variable "metrictank_environment_overrides" {
  default = ""
}
