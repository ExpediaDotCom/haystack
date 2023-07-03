variable "namespace" {}
variable "replicas" {}
variable "cassandra_address" {}
variable "tag_support" {}
variable "kafka_address" {}
variable "graphite_address" {}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_request"{}
variable "memory_limit"{}
variable "cpu_request"{}
variable "cpu_limit"{}
variable "env_vars" {}

variable "termination_grace_period" {
  default = 30
}
variable "enabled" {}


