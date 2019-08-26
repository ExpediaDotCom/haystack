variable "kubectl_executable_name" {}
variable "kubectl_context_name" {}
variable "minimum_masters" {}
variable "storage_volume" {}
variable "storage_class" {}
variable "enabled" {}
variable "monitoring-node_selecter_label" {}
variable "heap_memory_in_mb" {}
variable "logging_backend" {}

variable "k8s_fluentd_image" {
  default = "cheungpat/fluentd-elasticsearch-aws:1.22"
}