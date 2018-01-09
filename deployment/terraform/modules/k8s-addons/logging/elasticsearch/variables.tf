variable "kubectl_executable_name" {}
variable "k8s_cluster_name" {}
variable "minimum_masters" {}
variable "storage_volume" {}
variable "storage_class" {}

variable "k8s_fluentd_image" {
  default = "cheungpat/fluentd-elasticsearch-aws:1.22"
}
variable "enabled" {}
