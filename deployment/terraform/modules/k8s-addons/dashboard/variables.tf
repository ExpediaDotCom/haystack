variable "enabled" {}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "k8s_dashboard_cname" {}
variable "monitoring-node_selecter_label" {}

variable "k8s_dashboard_image" {
  default = "k8s.gcr.io/kubernetes-dashboard-amd64:v1.8.2"
}
