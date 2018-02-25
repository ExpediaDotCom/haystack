variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_limit"{}
variable "cpu_limit"{}

variable "termination_grace_period" {
  default = 30
}
variable "service_port" {
  default = 80
}
variable "container_port" {
  default = 8080
}

variable "k8s_cluster_name" {}

variable "trace_reader_hostname" {}

variable "trace_reader_service_port" {}

variable "metrictank_hostname" {}

variable "metrictank_port" {}