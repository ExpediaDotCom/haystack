variable "kubectl_executable_name" {}
variable "enabled" {}
variable "kubectl_context_name" {}
variable "graphite_node_port" {}
variable "storage_volume" {}
variable "monitoring-node_selecter_label" {}
variable "storage_class" {}

variable "k8s_influxdb_image" {
  default = "gcr.io/google_containers/heapster-influxdb-amd64:v1.3.3"
}

