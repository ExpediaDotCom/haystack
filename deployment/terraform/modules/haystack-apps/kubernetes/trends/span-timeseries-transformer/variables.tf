variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_endpoint" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}

variable "enabled" {}

variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_limit"{}
variable "memory_request"{}
variable "metricpoint_encoder_type" {}
variable "jvm_memory_limit"{}
variable "cpu_limit"{}
variable "cpu_request"{}
variable "env_vars" {}
variable "termination_grace_period" {
  default = 30
}
