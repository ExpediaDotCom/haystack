variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "elasticsearch_endpoint" {}
variable "cassandra_hostname" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "enabled"{}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selector_label"{}
variable "memory_request"{}
variable "memory_limit"{}
variable "cpu_request"{}
variable "cpu_limit"{}
variable "jvm_memory_limit"{}
variable "env_vars" {}

variable "termination_grace_period" {
  default = 30
}

variable "service_port" {
  default = 8080
}
variable "container_port" {
  default = 8080
}
