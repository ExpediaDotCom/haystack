variable "kubectl_executable_name" {}

variable "k8s_minikube_cluster_name" {
  default = "minikube"
}
variable "k8s_app_name_space" {
  default = "haystack-apps"
}

variable "haystack_domain_name" {
  default = "haystack.local"
}
variable "reverse_proxy_port" {
  default = "32300"
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
variable "pipes_enabled" {
  default = true
}
variable "pipes_json_transformer_instances" {
  default = "1"
}
variable "pipes_kafka_producer_instances" {
  default = "1"
}
variable "pipes_version" {
  default = "5c09d1162a17e7fc815493c6be888122a5372bd0"
}

# collectors config
variable "kinesis_span_collector_instances" {
  default = "1"
}
variable "kinesis_span_collector_enabled" {
  default = "true"
}
variable "kinesis_span_collector_version" {
  default = "e1d967e30a9a87122d8c332700cc4a3152db7f8a"
}
variable "kinesis_stream_region" {
  default = "us-west-2"
}
variable "kinesis_stream_name" {
  default = "haystack-test-proto"
}

# ui config
variable "haystack_ui_instances" {
  default = "1"
}
variable "ui_version" {
  default = "459278787c9979855c653c53d66bd181af8aedaa"
}
