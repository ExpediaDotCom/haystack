variable "kubectl_executable_name" {}
variable "k8s_cluster_name" {}
variable "grafana_storage_volume" {}
variable "k8s_storage_class" {}
variable "influxdb_storage_volume" {}

variable "enabled" {
  default = false
}