variable "image" {}
variable "replicas" {}
variable "enabled"{}
variable "namespace" {}
variable "kinesis_stream_region" {}
variable "kinesis_stream_name" {}
variable "sts_role_arn" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "kafka_endpoint" {}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_request"{}
variable "memory_limit"{}
variable "jvm_memory_limit"{}
variable "cpu_request"{}
variable "cpu_limit"{}
variable "app_name"{ default = "kinesis-span-collector" }
variable "env_vars" {}
variable "max_spansize_validation_enabled" {}
variable "max_spansize_log_only" {}
variable "max_spansize_limit" {}
variable "message_tag_key" {}
variable "message_tag_value" {}
variable "max_spansize_skip_tags" {}
variable "max_spansize_skip_services" {}

variable "termination_grace_period" {
  default = 30
}
variable "haystack_cluster_name" {}
