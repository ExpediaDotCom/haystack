variable "kubectl_executable_name" {}
variable "enabled" {}
variable "kubectl_context_name" {}

variable "storage_volume" {}

variable "storage_class" {}

variable "k8s_grafana_image" {
  default = "gcr.io/google_containers/heapster-grafana-amd64:v4.4.3"
}
variable "k8s_grafana_root_path" {
  default = "/metrics"
}

