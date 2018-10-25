variable "image" {}
variable "replicas" {}
variable "enabled"{}
variable "namespace" {}
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
variable "env_vars" {}
variable "service_port" {
  default = 80
}
variable "container_port" {
  default = 8080
}

variable "termination_grace_period" {
  default = 30
}
variable "haystack_cluster_name" {}
