variable "k8s_cluster_name" {}
variable "k8s_logs_es_url" {
  default = "elasticsearch"
}
variable "kubectl_executable_name" {
  default = "kubectl"
}
variable "k8s_app_name_space" {
  default = "haystack-apps"
}

