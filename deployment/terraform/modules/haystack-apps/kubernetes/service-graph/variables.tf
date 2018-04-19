variable "kafka_hostname" {}
variable "kafka_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "namespace" {}
variable "node_selector_label"{}
variable "default_memory_limit"{}
variable "default_cpu_limit"{}

# service-graph config
variable "service-graph" {
  type = "map"
}
