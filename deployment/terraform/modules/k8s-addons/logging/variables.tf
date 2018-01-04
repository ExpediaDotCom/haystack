variable "k8s_cluster_name" {}
variable "k8s_app_namespace" {}
variable "kubectl_executable_name" {}
variable "container_log_path" {}
variable "es_nodes" {}

variable "enabled" {
  default = false
}
