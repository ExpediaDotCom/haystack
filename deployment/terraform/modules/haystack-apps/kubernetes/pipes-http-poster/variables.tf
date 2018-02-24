variable "image" {}
variable "replicas" {}
variable "namespace" {}
variable "kafka_hostname" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "enabled" {}
variable "httppost_url" {}
variable "httppost_pollpercent" {}

variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "node_selecter_label"{}
variable "memory_limit"{}
variable "cpu_limit"{}

variable "termination_grace_period" {
  default = 30
}
