variable "kubectl_executable_name" {}
variable "enabled" {}
variable "kubectl_context_name" {}
variable "metrics_cname" {}
variable "root_url" {}
variable "storage_volume" {}
variable "node_selecter_label" {}

variable "storage_class" {}

variable "k8s_grafana_image" {
  default = "gcr.io/google_containers/heapster-grafana-amd64:v4.4.3"
}


