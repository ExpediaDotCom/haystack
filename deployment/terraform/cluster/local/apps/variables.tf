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
  default = "92219da46ca3e3ee20f99eafe2939d8e7dfb004e"
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
  default = "df9b59950fb44a8257db1482cc2ae76a3688d12b"
}

# pipes config
variable "pipes_json_transformer_enabled" {
  default = false
}
variable "pipes_kafka_producer_enabled" {
  default = false
}
variable "pipes_http_poster_enabled" {
  default = true
}
variable "pipes_firehose_writer_enabled" {
  default = true
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
variable "pipes_version" {
  default = "e1cc1b6b0bbde427d927869903bcf79d2e29ea4c"
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

# ui config
variable "haystack_ui_instances" {
  default = "1"
}
variable "ui_version" {
  default = "459278787c9979855c653c53d66bd181af8aedaa"
}
