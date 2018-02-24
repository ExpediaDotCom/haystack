variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_endpoint" {}
variable "elasticsearch_hostname" {}
variable "elasticsearch_port" {}
variable "cassandra_hostname" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "enabled"{}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_limit"{}
variable "cpu_limit"{}
variable "enable_kafka_sink" {
  default = false
}

variable "termination_grace_period" {
  default = 30
}
