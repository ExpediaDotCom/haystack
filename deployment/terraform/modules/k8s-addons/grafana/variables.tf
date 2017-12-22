variable "kubectl_executable_name" {}

variable "k8s_grafana_storage" {
  default = "100Mi"
}

variable "k8s_grafana_storage_class" {
  default = "standard"
}

variable "k8s_grafana_image" {
  default = "gcr.io/google_containers/heapster-grafana-amd64:v4.4.3"
}
variable "k8s_grafana_root_path" {
  default = "/metrics"
}

