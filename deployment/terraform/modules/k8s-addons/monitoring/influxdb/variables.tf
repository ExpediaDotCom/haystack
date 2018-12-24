variable "kubectl_executable_name" {}
variable "enabled" {}
variable "kubectl_context_name" {}
variable "graphite_node_port" {}
variable "storage_volume" {}
variable "node_selecter_label" {}
variable "storage_class" {}
variable "heap_memory_in_mb" {}

variable "k8s_influxdb_image" {
  default = "influxdb:1.6"
}

