variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "grafana_storage_volume" {}
variable "k8s_storage_class" {}
variable "influxdb_storage_volume" {}

variable "enabled" {
  default = false
}