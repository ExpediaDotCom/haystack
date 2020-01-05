variable "kubectl_executable_name" {}
variable "enabled" {}
variable "kubectl_context_name" {}
variable "graphite_node_port" {}
variable "storage_volume" {}
variable "node_selecter_label" {}
variable "storage_class" {}
variable "influxdb_memory_limit" {}
variable "influxdb_cpu_limit" {}

variable "k8s_influxdb_image" {
  default = "influxdb:1.6"
}

