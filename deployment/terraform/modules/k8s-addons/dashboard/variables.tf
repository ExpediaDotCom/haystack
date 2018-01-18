variable "enabled" {}
variable "kubectl_context_name" {}
variable "kubectl_executable_name" {}
variable "base_domain_name" {}
variable "haystack_cluster_name" {}

variable "k8s_dashboard_image" {
  default = "k8s.gcr.io/kubernetes-dashboard-amd64:v1.8.2"
}
variable "k8s_dashboard_root_path" {
  default = "/dashboard"
}

