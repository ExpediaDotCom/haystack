variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_endpoint" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}

variable "enabled" {}
variable "enable_external_kafka_producer" {}
variable "enable_metrics_sink" {
  default = true
}
variable "external_kafka_producer_endpoint" {}
variable "metricpoint_encoder_type" {}
variable "env_vars" {}

variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label" {}
variable "memory_request" {}
variable "memory_limit" {}
variable "jvm_memory_limit"{}

variable "cpu_request" {}
variable "cpu_limit" {}

variable "histogram_max_value" {}
variable "histogram_precision" {}
variable "histogram_value_unit" {}

variable "additionalTags" {}

variable "termination_grace_period" {
  default = 30
}
