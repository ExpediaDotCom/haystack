variable "kafka_hostname" {}
variable "kafka_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "namespace" {}
variable "node_selector_label"{}

# service-graph config
variable "service-graph" {
  type = "map"
}
