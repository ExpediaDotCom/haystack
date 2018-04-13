variable "namespace" {}
variable "replicas" {}
variable "cassandra_address" {}
variable "kafka_address" {}
variable "graphite_address" {}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_limit"{}
variable "cpu_limit"{}
variable "env_vars" {}

variable "termination_grace_period" {
  default = 30
}
variable "enabled" {}


