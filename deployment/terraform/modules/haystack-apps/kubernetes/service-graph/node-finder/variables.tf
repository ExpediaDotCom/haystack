variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_endpoint" {}
variable "metricpoint_encoder_type" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "enabled"{}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_request"{}
variable "memory_limit"{}
variable "jvm_memory_limit"{}
variable "cpu_request"{}
variable "cpu_limit"{}
variable "env_vars" {}
variable "termination_grace_period" {
  default = 30
}
