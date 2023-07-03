variable "elasticsearch_hostname" {}
variable "elasticsearch_port" {}
variable "kafka_hostname" {}
variable "kafka_port" {}
variable "cassandra_hostname" {}
variable "cassandra_port" {}
variable "graphite_hostname" {}
variable "graphite_port" {}
variable "graphite_enabled" {}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "namespace" {}
variable "node_selector_label"{}


# traces config
variable "traces" {
  type = "map"
}
