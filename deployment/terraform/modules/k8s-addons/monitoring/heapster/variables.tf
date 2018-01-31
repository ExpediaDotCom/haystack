variable "kubectl_executable_name" {}
variable "enabled" {}
variable "kubectl_context_name" {}
variable "monitoring-node_selecter_label" {}

variable "k8s_heapster_image" {
  default = "gcr.io/google_containers/heapster-amd64:v1.5.0"
}
variable "addon_resizer_image" {
  default = "gcr.io/google_containers/addon-resizer:1.8.0"
}

variable "influxdb_servicename" {
  default = "monitoring-influxdb"
}