variable "replicas" {}
variable "namespace" {}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label" {}
variable "memory_limit" {}
variable "jvm_memory_limit"{}
variable "cpu_limit" {}
variable "docker_host_ip" {}
variable "termination_grace_period" {
  default = 30
}

